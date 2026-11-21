# **Full Microservices, Business Logic, Database Design & Admin Control-Plane Specification**

**Repository:** fx-dynamic-liquidity-smart-order-engine  
**Base Package Namespace:** com.jk.apps.banking.fx.sor  
**Architecture Style:** Neal Ford Closed-Layer Microservice Architecture (Zero-Sharing Maven Reactor Monorepo)  
**Target Runtime & Scale:** Java 21+ Virtual Threads, Generational ZGC, eBPF (Cilium), 50–150+ K8s Pods (\~1,000,000 Aggregate RPS)

## **1\. Zero-Sharing Maven Reactor Monorepo Topology**

The platform is structured as an isolated Maven module reactor with zero shared Java source code across modules. Contracts are maintained strictly via OpenAPI and Avro schemas.

| Maven Module | Base Package | Primary Role & Bounded Context | Target K8s Scale & SLA |
| :---- | :---- | :---- | :---- |
| **fx-quote-service** | com.jk.apps.banking.fx.sor.quote | Hot-path streaming price aggregation, session regime spread guards, and client-tier markups | 30–60 Pods (\~750k RPS w/ edge cache, \< 2 ms p99) |
| **fx-qualify-service** | com.jk.apps.banking.fx.sor.qualify | Deterministic knock-out qualification, CLS cutoff validation, and atomic bilateral credit/NOP checks | 15–40 Pods (\~200k RPS, \< 3 ms p99) |
| **fx-route-exec-service** | com.jk.apps.banking.fx.sor.engine | 5-step OrderRoutingFacade pipeline, Drools LP scoring, internalization, multi-LP sweep, waterfall failover, and Kafka provenance | 10–30 Pods (\~50k RPS, \< 10 ms p99) |
| **fx-admin-service** | com.jk.apps.banking.fx.sor.admin | Control-plane administration, async batch synthetic dataset seeding, load simulation, workflow execution logging (dlp\_sor\_system\_db), and dealing-desk overrides | 2–3 Isolated Control-Plane Pods (Async Batch) |

## **2\. Microservice Responsibilities, API Endpoints & Business Logic**

### **2.1 fx-quote-service (Pricing & Streaming Quote Engine)**

> * **Core Business Logic:** Ingests continuous two-way Bid/Ask streams from global Liquidity Providers (LPs), maintains top-of-book and depth-of-book in Caffeine L1 / Redis L2 memory, detects active trading session regimes (Tokyo, London/NY overlap, NY 5:00 PM EST rollover, macro news flags), applies symmetric client-tier markups, and filters stale price ticks exceeding TTL thresholds.

| HTTP Method & Endpoint | Purpose | Key Inputs / Outputs |
| :---- | :---- | :---- |
| GET /api/v1/fx/quotes/stream | SSE / WebSocket real-time streaming quotes | **In:** ccyPairs, clientTier, tenor **Out:** Continuous two-way Bid/Ask ticks with all-in spread |
| POST /api/v1/fx/quotes/indicative | Executable indicative quote lookup | **In:** clientId, clientTier, ccyPair, side, notional, tenor **Out:** Executable rate, markup bps, quoteId, TTL ms |
| GET /api/v1/fx/rates/{ccyPair} | Spot reference mid/top-of-book snapshot | **In:** Path ccyPair **Out:** Aggregated Bid, Ask, Mid, active LP count |

### **2.2 fx-qualify-service (Deterministic Knock-Out & Credit Pre-Check)**

> * **Core Business Logic:** Acts as the fast pre-trade compliance and risk gate. Evaluates deterministic knock-outs (currency pair/tenor support, min/max clip sizes, restricted emerging-market rules, CLS/settlement cutoffs, quote staleness) and executes atomic Redis Lua scripts to verify and reserve bilateral counterparty credit lines and Net Open Position (NOP) ceilings in a single round-trip.

| HTTP Method & Endpoint | Purpose | Key Inputs / Outputs |
| :---- | :---- | :---- |
| POST /api/v1/fx/orders/qualify | Full pre-trade venue qualification | **In:** clientOrderId, clientId, ccyPair, notionalUsd, tenor, valueDate **Out:** Eligible LP list, knock-out reasons for excluded LPs, credit reservation token |
| GET /api/v1/fx/credit/utilization/{clientId} | Real-time credit & NOP headroom lookup | **In:** Path clientId; Query counterpartyLpId **Out:** Max limit, utilized USD, reserved USD, reset epoch |
| POST /api/v1/fx/venues/knockout-check | Lightweight venue eligibility filter | **In:** ccyPair, tenor, notionalUsd **Out:** Pass/fail status per LP with policy rule codes |

### **2.3 fx-route-exec-service (Smart Order Routing & Execution Core)**

> * **Core Business Logic (5-Step OrderRoutingFacade Pipeline):**  
  1. **Parallel Enrichment:** Concurrently retrieves live depth, session regime, and NoSQL LP quality projections via Java 21 Virtual Threads.  
  2. **Idempotency & Qualification Verification:** Acquires atomic Redis SET NX PX lock on clientOrderId to prevent duplicate execution on retries.  
  3. **Drools/DMN Composite LP Scoring:** Ranks eligible LPs by balancing raw spread against rolling fill ratios, last-look hold latency, and markout toxicity scores.  
  4. **Internalization & Smart Splitting:** Matches against internal principal inventory book when safe; otherwise routes to \#1 LP or splits institutional block orders across top N LPs with Resilience4j semaphore bulkheads, circuit breakers, and waterfall failover within slippage limits.  
  5. **Async Provenance Audit:** Publishes immutable Avro explainability trace (FX Global Code / MiFID II RTS-27/28) to Apache Kafka off the hot path.

| HTTP Method & Endpoint | Purpose | Key Inputs / Outputs |
| :---- | :---- | :---- |
| POST /api/v1/fx/orders/route-execute | End-to-end smart order routing & execution | **In:** clientOrderId, quoteId, clientId, ccyPair, side, notional, maxSlippageBps **Out:** Execution report, child LP splits, achieved rate, slippage bps |
| POST /api/v1/fx/orders/{orderId}/failover | Waterfall cascade on last-look rejection | **In:** Path orderId; Body rejectedLpId, unfilledNotional, reasonCode **Out:** Re-routed child execution details on backup LP(s) |
| GET /api/v1/fx/orders/{clientOrderId}/trace | Regulatory explainability & audit retrieval | **In:** Path clientOrderId **Out:** Full decision trace, evaluated LPs, knock-out codes, Drools rule version |

### **2.4 fx-admin-service (Control Plane, Synthetic Batch Engine & Workflow Logging)**

> * **Core Business Logic:** Provides an RBAC-secured administrative control plane isolated from trading pods. Generates default-driven synthetic datasets in configurable batches across PostgreSQL, Redis L2, and NoSQL, triggers high-RPS load simulations, tracks all async jobs in dlp\_sor\_system\_db.wf\_exec\_log with full CRUD/cancellation, and hosts future dealing-desk operational overrides (cache flushes, credit overrides, LP kill switches).

| HTTP Method & Endpoint | Purpose & Defaults | Async Execution & wf\_exec\_log Behavior |
| :---- | :---- | :---- |
| POST /api/v1/admin/synthetic/datasets/generate | Batch synthetic seeding across target stores. **Defaults:** numBatches=10, batchSize=1000, all 5 dataset types, targetEnv="PERF\_SIM" | Creates QUEUED row in wf\_exec\_log, returns 202 Accepted \+ wfExecId, executes batches on Virtual Threads with per-batch progress updates. |
| POST /api/v1/admin/synthetic/load-sim/trigger | High-RPS traffic & market volatility simulator. **Defaults:** targetRps=50000, durationSeconds=60, regime="NY\_LONDON\_OVERLAP" | Returns 202 Accepted \+ wfExecId; streams synthetic quotes/orders and logs throughput metrics in wf\_exec\_log. |
| GET /api/v1/admin/workflows/{wfExecId} | Read single workflow execution status | Returns 200 OK with status, startedAt, completedAt, batchesCompleted, recordsInserted, and errorDetails. |
| GET /api/v1/admin/workflows | List & filter workflow execution history | Supports filtering by status, workflowType, triggeredBy, with pagination. |
| PATCH /api/v1/admin/workflows/{wfExecId}/cancel | Graceful mid-run workflow cancellation | Signals cancellation between batch chunks, sets status='CANCELLED', and records completed\_at. |
| DELETE /api/v1/admin/workflows/{wfExecId} | Delete log entry & optional synthetic data purge | Deletes wf\_exec\_log row; when purgeSyntheticData=true, removes SYNTH\_-prefixed test records. |
| POST /api/v1/admin/cache/invalidate POST /api/v1/admin/credit/override POST /api/v1/admin/venues/{lpId}/halt | Future control-plane operational extensions | Executes cache flushes, emergency bilateral credit adjustments, or LP venue kill switches while logging every action to wf\_exec\_log. |

## **3\. Polyglot Database Design & Complete DDL Schemas**

### **3.1 Dedicated System Control-Plane Database (dlp\_sor\_system\_db — PostgreSQL)**

Owned exclusively by fx-admin-service so administrative batch tracking never contends with operational trading schemas.  
`CREATE DATABASE dlp_sor_system_db;`

`CREATE TABLE wf_exec_log (`  
    `wf_exec_id          UUID PRIMARY KEY,`  
    `workflow_type       VARCHAR(50)  NOT NULL, -- 'SYNTHETIC_SEED', 'LOAD_SIMULATION', 'CACHE_INVALIDATION', 'CREDIT_OVERRIDE', 'LP_KILL_SWITCH'`  
    `dataset_types       JSONB        NULL,     -- e.g., ["SPREAD_SCHEDULES", "SETTLEMENT_POLICIES", "BILATERAL_CREDIT", "LP_TELEMETRY", "EXECUTION_ORDERS"]`  
    `num_batches         INTEGER      NOT NULL DEFAULT 10,`  
    `batch_size          INTEGER      NOT NULL DEFAULT 1000,`  
    `batches_completed   INTEGER      NOT NULL DEFAULT 0,`  
    `records_inserted    BIGINT       NOT NULL DEFAULT 0,`  
    `request_params      JSONB        NOT NULL, -- Full resolved request payload including applied defaults`  
    `execution_summary   JSONB        NULL,     -- Per-dataset record counts, target tables, or admin action diff`  
    `status              VARCHAR(20)  NOT NULL, -- 'QUEUED', 'RUNNING', 'COMPLETED', 'PARTIAL_FAILED', 'FAILED', 'CANCELLED'`  
    `started_at          TIMESTAMPTZ  NOT NULL DEFAULT NOW(),`  
    `updated_at          TIMESTAMPTZ  NOT NULL DEFAULT NOW(),`  
    `completed_at        TIMESTAMPTZ  NULL,`  
    `duration_ms         BIGINT       NULL,`  
    `triggered_by        VARCHAR(100) NOT NULL DEFAULT 'ADMIN_API',`  
    `error_details       TEXT         NULL`  
`);`

`CREATE INDEX idx_wf_exec_log_status_started ON wf_exec_log (status, started_at DESC);`  
`CREATE INDEX idx_wf_exec_log_type_started   ON wf_exec_log (workflow_type, started_at DESC);`

### **3.2 Operational Trading Relational Database (fx\_sor\_trading\_db — PostgreSQL / Aurora)**

`-- Owned by fx-quote-service (Loaded into Caffeine L1; zero hot-path SQL queries)`  
`CREATE TABLE client_tier_spread_schedules (`  
    `tier_id             VARCHAR(30)   NOT NULL,`  
    `ccy_pair            VARCHAR(10)   NOT NULL,`  
    `session_regime      VARCHAR(30)   NOT NULL, -- 'TOKYO', 'LONDON_NY_OVERLAP', 'NY_ROLLOVER', 'MACRO_NEWS'`  
    `markup_bps          NUMERIC(8,4)  NOT NULL,`  
    `news_multiplier     NUMERIC(6,2)  NOT NULL DEFAULT 1.00,`  
    `effective_from      TIMESTAMPTZ   NOT NULL,`  
    `approved_by         VARCHAR(100)  NOT NULL,`  
    `PRIMARY KEY (tier_id, ccy_pair, session_regime)`  
`);`

`-- Owned by fx-qualify-service (Loaded into Caffeine L1; governs knock-out rules)`  
`CREATE TABLE settlement_cutoffs_and_policies (`  
    `ccy_pair            VARCHAR(10)   NOT NULL,`  
    `tenor               VARCHAR(10)   NOT NULL, -- 'SPOT', 'TOM', 'NEXT', '1M'`  
    `cls_eligible        BOOLEAN       NOT NULL DEFAULT TRUE,`  
    `cutoff_time_utc     TIME          NOT NULL,`  
    `min_clip_usd        NUMERIC(18,2) NOT NULL,`  
    `max_clip_usd        NUMERIC(18,2) NOT NULL,`  
    `restricted_flag     BOOLEAN       NOT NULL DEFAULT FALSE,`  
    `PRIMARY KEY (ccy_pair, tenor)`  
`);`

`-- Owned by fx-route-exec-service (Range-partitioned by trade_date; written asynchronously via Kafka micro-batches)`  
`CREATE TABLE fx_execution_orders (`  
    `trade_date          DATE          NOT NULL,`  
    `order_id            UUID          NOT NULL,`  
    `client_order_id     VARCHAR(64)   NOT NULL,`  
    `client_id           VARCHAR(50)   NOT NULL,`  
    `ccy_pair            VARCHAR(10)   NOT NULL,`  
    `side                VARCHAR(4)    NOT NULL, -- 'BUY', 'SELL'`  
    `requested_notional  NUMERIC(18,2) NOT NULL,`  
    `executed_rate       NUMERIC(18,8) NOT NULL,`  
    `slippage_bps        NUMERIC(8,4)  NOT NULL,`  
    `execution_mode      VARCHAR(20)   NOT NULL, -- 'INTERNALIZED', 'SINGLE_LP', 'MULTI_LP_SWEEP'`  
    `status              VARCHAR(20)   NOT NULL, -- 'FILLED', 'PARTIAL_FILLED', 'REJECTED', 'ESCALATED'`  
    `created_at          TIMESTAMPTZ   NOT NULL DEFAULT NOW(),`  
    `PRIMARY KEY (trade_date, order_id)`  
`) PARTITION BY RANGE (trade_date);`

`CREATE TABLE child_venue_splits (`  
    `trade_date          DATE          NOT NULL,`  
    `split_id            UUID          NOT NULL,`  
    `order_id            UUID          NOT NULL,`  
    `lp_id               VARCHAR(30)   NOT NULL,`  
    `allocated_notional  NUMERIC(18,2) NOT NULL,`  
    `fill_rate           NUMERIC(18,8) NULL,`  
    `hold_latency_ms     INTEGER       NOT NULL,`  
    `lp_status           VARCHAR(20)   NOT NULL, -- 'ACCEPTED', 'LAST_LOOK_REJECTED', 'TIMEOUT'`  
    `PRIMARY KEY (trade_date, split_id)`  
`) PARTITION BY RANGE (trade_date);`

### **3.3 Distributed In-Memory State (Redis L2) & High-Velocity Telemetry (NoSQL)**

| Storage Engine & Owner | Key / Table Pattern | Schema Attributes | Access Pattern & SLA |
| :---- | :---- | :---- | :---- |
| **Redis L2** (fx-quote-service) | quote:lp:{ccyPair}:{lpId} (Hash w/ millisecond TTL) | bid, ask, available\_size, quote\_ts, ttl\_ms | Pipelined sub-millisecond reads to build top/depth-of-book across active LPs. |
| **Redis L2** (fx-qualify-service) | credit:bilateral:{clientId}:{lpId} nop:desk:{ccyPair} | max\_credit\_usd, utilized\_usd, reserved\_usd, reset\_epoch | Atomic Redis Lua script checks and reserves credit/NOP in one round-trip (\< 0.5 ms). |
| **Redis L2** (fx-route-exec-service) | idem:order:{clientOrderId} (Hash, 24h TTL) | state (IN\_FLIGHT / FILLED), order\_id, locked\_rate | SET NX PX atomic guard blocking duplicate execution on client retries. |
| **NoSQL Wide-Column** (fx-route-exec-service) | lp\_telemetry\_projections **PK:** CCY\_PAIR\#SESSION\_BUCKET\#LP\_ID **SK:** WINDOW\_TYPE (5M/1H/SESSION) | fill\_ratio\_pct, avg\_hold\_ms, last\_look\_reject\_rate, markout\_toxicity\_bps, ttl\_epoch | Single-partition point lookup in **\< 1 ms** during Drools LP scoring; updated via async micro-batches with 7-day TTL. |

