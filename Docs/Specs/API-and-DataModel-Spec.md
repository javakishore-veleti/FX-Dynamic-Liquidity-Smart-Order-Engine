# **API & Data Model Specification: FX Dynamic Liquidity & Smart Order Routing Engine**

**Repository:** fx-dynamic-liquidity-smart-order-engine  
**Base Package Namespace:** com.jk.apps.banking.fx.sor  
**Architecture Style:** Neal Ford Closed-Layer Microservice Architecture (Zero-Sharing Maven Reactor Monorepo)  
**Target Throughput:** Up to 1,000,000 Aggregate RPS across Kubernetes (50–150+ Pods)

## **1\. Microservice API Contract Matrix**

| Microservice | HTTP Method & Endpoint | Target RPS & Latency SLA | Inbound Payload Summary | Outbound Payload & Business Logic |
| :---- | :---- | :---- | :---- | :---- |
| **fx-quote-service** (com.jk.apps.banking.fx.sor.quote) | GET /api/v1/fx/quotes/stream (SSE / WebSocket) | \~600,000 RPS \< 2 ms (p99) | Query params: ccyPairs, clientTier, tenor | Streams continuous two-way Bid/Ask ticks after applying symmetric client-tier markups and session regime spread guards from Caffeine L1 cache. |
| **fx-quote-service** | POST /api/v1/fx/quotes/indicative | \~150,000 RPS \< 3 ms (p99) | clientId, clientTier, ccyPair, side (BUY/SELL), notional, tenor | Returns executable indicative rate, all-in spread (bps), quote TTL (ms), and quoteId without locking bilateral credit. |
| **fx-qualify-service** (com.jk.apps.banking.fx.sor.qualify) | POST /api/v1/fx/orders/qualify | \~160,000 RPS \< 3 ms (p99) | clientOrderId, clientId, ccyPair, notionalUsd, tenor, valueDate | Runs deterministic knock-out pipeline (pair/tenor support, min/max clip size, CLS settlement cutoffs, stale quote TTL, bilateral credit/NOP check). Returns ranked eligible LP list. |
| **fx-qualify-service** | GET /api/v1/fx/credit/utilization/{clientId} | \~40,000 RPS \< 2 ms (p99) | Path param: clientId; Query: counterpartyLpId | Reads real-time bilateral credit utilization, Net Open Position (NOP) headroom, and reset timestamp directly from Redis L2. |
| **fx-route-exec-service** (com.jk.apps.banking.fx.sor.engine) | POST /api/v1/fx/orders/route-execute | \~40,000 RPS \< 10 ms (p99) | clientOrderId, quoteId, clientId, ccyPair, side, notional, maxSlippageBps | Enforces Redis idempotency lock, evaluates Drools composite LP matrix (spread vs. hold latency & markout toxicity), checks internal principal book matching, executes single-LP or multi-LP sweep, and emits async Kafka audit event. |
| **fx-route-exec-service** | POST /api/v1/fx/orders/{orderId}/failover | \~5,000 RPS \< 8 ms (p99) | Path param: orderId; Body: rejectedLpId, unfilledNotional, reasonCode | Cascades unfilled/last-look rejected remainder to \#2/\#3 ranked backup LP within remaining slippage budget and updates child order splits. |
| **fx-route-exec-service** | GET /api/v1/fx/orders/{clientOrderId}/trace | \~5,000 RPS \< 15 ms (p99) | Path param: clientOrderId | Retrieves immutable regulatory provenance record (FX Global Code / MiFID II RTS-27/28) detailing why winning LP(s) were chosen and why others were knocked out. |

## **2\. Polyglot Data Model Specification (PostgreSQL, Redis L2 & NoSQL)**

| Storage Engine | Owning Microservice | Table / Keyspace Name | Primary / Partition Key & Schema Fields | Access Pattern & Consistency |
| :---- | :---- | :---- | :---- | :---- |
| **PostgreSQL / Aurora** (ACID Reference & Config) | fx-quote-service | client\_tier\_spread\_schedules | **PK:** (tier\_id, ccy\_pair, session\_regime) Fields: markup\_bps, news\_multiplier, effective\_from, approved\_by | Read into Caffeine L1 at startup; refreshed via Redis Pub/Sub invalidation events. Zero hot-path SQL queries. |
| **PostgreSQL / Aurora** (ACID Reference & Config) | fx-qualify-service | settlement\_cutoffs\_and\_policies | **PK:** (ccy\_pair, tenor) Fields: cls\_eligible, cutoff\_time\_utc, min\_clip\_usd, max\_clip\_usd, restricted\_flag | Cached in Caffeine L1 with event-driven invalidation; governs deterministic knock-out rules. |
| **PostgreSQL / Aurora** (Operational Partitioned) | fx-route-exec-service | fx\_execution\_orders & child\_venue\_splits (Range-partitioned by trade\_date) | **PK:** (trade\_date, order\_id); Child FK: order\_id Fields: client\_order\_id, client\_id, ccy\_pair, executed\_rate, lp\_id, fill\_status, slippage\_bps | Written asynchronously off the critical execution path via Kafka consumer micro-batches; queried for T+0/T+1 settlement and audit. |
| **Redis L2** (In-Memory Distributed State) | fx-quote-service | quote:lp:{ccyPair}:{lpId} (Hash with millisecond TTL) | **Key:** quote:lp:EURUSD:LP\_CITI Fields: bid, ask, available\_size, quote\_ts, ttl\_ms | Sub-millisecond pipelined reads to assemble top-of-book depth across active Liquidity Providers. |
| **Redis L2** (Atomic Lua Reservation) | fx-qualify-service | credit:bilateral:{clientId}:{lpId} & nop:desk:{ccyPair} | **Key:** credit:bilateral:CORP\_88:LP\_JPM Fields: max\_credit\_usd, utilized\_usd, reserved\_usd, reset\_epoch | Executed via atomic Redis Lua scripts to check and reserve credit/NOP headroom in a single network round-trip. |
| **Redis L2** (Idempotency Lock) | fx-route-exec-service | idem:order:{clientOrderId} (String/Hash, 24h TTL) | **Key:** idem:order:ORD-20261121-991 Fields: state (IN\_FLIGHT/FILLED), order\_id, locked\_rate | SET NX PX atomic guard preventing duplicate LP execution on client network retries. |
| **NoSQL Wide-Column** (DynamoDB / Cassandra) | fx-route-exec-service | lp\_telemetry\_projections | **Partition Key:** CCY\_PAIR\#SESSION\_BUCKET\#LP\_ID **Sort Key:** WINDOW\_TYPE (5M / 1H / SESSION) Fields: fill\_ratio\_pct, avg\_hold\_ms, last\_look\_reject\_rate, markout\_toxicity\_bps, ttl\_epoch | Pre-computed materialized projection read via single-partition point lookup in **\< 1 ms**; updated via async micro-batches with 7-day TTL. |

## **3\. End-to-End 5-Step Business Logic Pipeline (OrderRoutingFacade)**

| Pipeline Step | Responsible Service | Deterministic Business Rules & Guardrails | Latency Budget |
| :---- | :---- | :---- | :---- |
| **Step 1: Parallel Enrichment** | fx-quote-service & fx-qualify-service | Concurrently fetches live LP order-book depth (Redis L2), current trading session regime (Tokyo, London/NY overlap, NY 5 PM rollover, FOMC/NFP news flags), and client tier schedule (Caffeine L1) using Java 21 Virtual Threads. | \< 1.0 ms |
| **Step 2: Deterministic Knock-Out** | fx-qualify-service | Eliminates ineligible venues: (a) currency pair/tenor unsupported, (b) notional outside min/max clip size, (c) quote age exceeds stale threshold, (d) past CLS settlement cutoff, or (e) insufficient bilateral credit / NOP headroom. Escalates restricted currencies or zero-liquidity exceptions to dealing desk. | \< 1.0 ms |
| **Step 3: Composite LP Scoring** | fx-route-exec-service | Executes Drools/DMN decision table combining raw all-in price with NoSQL LP quality projections (penalizing high last-look rejection rates, slow hold times \> 50 ms, and adverse markout toxicity). | \< 1.5 ms |
| **Step 4: Routing & Splitting** | fx-route-exec-service | Checks if flow can be safely internalized against bank principal inventory. Otherwise routes standard clips to \#1 ranked LP or splits institutional block orders across top N LPs, wrapped in Resilience4j semaphore bulkheads and automatic waterfall failover. | \< 1.5 ms (internal) |
| **Step 5: Async Provenance Audit** | fx-route-exec-service | Publishes non-blocking Avro provenance payload (evaluated LPs, knock-out reasons, Drools rule version, winning spread) to Apache Kafka for regulatory compliance and Airflow warehouse ingestion. | 0 ms (async) |

