# **Synthetic Data Catalog & Batch Seeding Specification (fx-admin-service)**

**Document ID:** Synthetic-Data-Catalog-and-Seeding-Spec.md  
**Repository:** fx-dynamic-liquidity-smart-order-engine  
**Owning Service:** Middleware/fx-admin-service (com.jk.apps.banking.fx.sor.admin)  
**Control-Plane Database:** dlp\_sor\_system\_db (wf\_exec\_log)  
**Coverage Standard:** 28-Currency Pair Institutional FX Universe & 100% Column Population Across PostgreSQL, Redis L2, NoSQL, and Kafka

## **1\. 28-Currency Pair Institutional FX Universe**

To exercise every routing branch in the Smart Order Routing (SOR) engine—including tight-spread multi-LP sweeps, session regime spread guards, CLS settlement cutoffs, bilateral credit knock-outs, and emerging-market dealing-desk escalations—fx-admin-service seeds a standardized **28-currency pair catalog** across three liquidity tiers:

| Liquidity Tier | Count | Currency Pairs | Traffic Weight & Routing Behaviors Tested |
| :---- | :---- | :---- | :---- |
| **G10 Majors** | 7 | EUR/USD, USD/JPY, GBP/USD, USD/CHF, AUD/USD, USD/CAD, NZD/USD | 45% of simulated volume (EUR/USD weighted at 25% to stress hot partitions); ultra-tight spreads (0.1–0.8 bps); 12 active LPs; full CLS settlement; multi-LP block sweeps. |
| **G10 Crosses** | 11 | EUR/GBP, EUR/JPY, EUR/CHF, EUR/AUD, EUR/CAD, GBP/JPY, GBP/CHF, AUD/JPY, CAD/JPY, CHF/JPY, AUD/NZD | 35% of simulated volume; medium spreads (0.8–2.5 bps); session-dependent liquidity shifts between Tokyo, London, and NY overlap windows. |
| **Emerging Markets (EM) & Restricted** | 10 | USD/MXN, USD/SGD, USD/HKD, USD/ZAR, USD/TRY, USD/CNH, USD/INR, USD/BRL, USD/KRW, USD/PLN | 20% of simulated volume; wider spreads (2.5–18.5 bps); non-CLS / NDF rules; elevated last-look rejection rates; mandatory **Dealing Desk Escalation** on restricted pairs (USD/INR, USD/BRL). |

## **2\. Synthetic Counterparty, Client Tier & Liquidity Provider Dimensions**

| Dimension | Synthetic IDs Generated | Behavioral Profile & Anomaly Injection |
| :---- | :---- | :---- |
| **Liquidity Providers (12 LPs)** | LP\_CITI, LP\_JPM, LP\_BARX, LP\_UBS, LP\_GS, LP\_MS, LP\_DB, LP\_HSBC, LP\_XTX, LP\_CITADEL, LP\_JUMP, LP\_HC\_TECH | 10 healthy LPs (98.5–99.6% fill ratio, 8–22 ms hold time) \+ **2 deliberately degraded/toxic LPs** (LP\_DB, LP\_HC\_TECH injected with 145 ms last-look hold latency, 14% reject rate, and \+1.6 bps markout toxicity) to validate Drools penalty scoring and Incident RCA Agent detection. |
| **Client Tiers (5 Tiers)** | TIER\_1\_INSTITUTIONAL, TIER\_2\_FORTUNE500, TIER\_3\_COMMERCIAL, TIER\_4\_HNW\_WEALTH, TIER\_5\_RETAIL\_REMIT | Covers institutional block traders ($10M–$50M clips), corporate treasuries ($500k–$10M), and aggregated retail remittance pools ($100–$10k). |
| **Synthetic Clients (50 Clients)** | SYNTH\_CORP\_01..25, SYNTH\_INST\_01..15, SYNTH\_RETAIL\_POOL\_01..10 | 5% of clients are seeded at **96% bilateral credit utilization** to trigger deterministic credit knock-outs during qualification load tests. |
| **Session Regimes (6 Regimes)** | TOKYO, LONDON\_TOKYO\_OVERLAP, LONDON\_NY\_OVERLAP, NY\_AFTERNOON, NY\_ROLLOVER\_5PM, MACRO\_NEWS\_HIGH | Simulates tightening spreads during London/NY overlap and automated spread widening \+ last-look penalties during NY 5:00 PM EST rollover and FOMC/NFP releases. |

## **3\. Column-by-Column Synthetic Data Generation Matrix (100% Column Coverage)**

### **3.1 PostgreSQL Operational Trading DB (fx\_sor\_trading\_db)**

#### **Table 1: client\_tier\_spread\_schedules (Default Seed: 5 Tiers × 28 Pairs × 6 Regimes \= 840 Rows)**

| Column Name | SQL Type | Synthetic Value Generation Rule |
| :---- | :---- | :---- |
| tier\_id | VARCHAR(32) (PK) | Deterministic cross-product across all 5 client tiers. |
| ccy\_pair | VARCHAR(10) (PK) | Deterministic cross-product across all 28 currency pairs. |
| session\_regime | VARCHAR(32) (PK) | Deterministic cross-product across all 6 market session regimes. |
| markup\_bps | NUMERIC(8,4) | Scaled by tier & liquidity: 0.1500 bps (Tier 1 G10 Major in London/NY) up to 18.5000 bps (Tier 5 EM pair during NY Rollover). |
| news\_multiplier | NUMERIC(5,2) | 1.00 for standard sessions; 1.75 to 3.50 for MACRO\_NEWS\_HIGH and NY\_ROLLOVER\_5PM. |
| effective\_from | TIMESTAMPTZ | Set to start of current trading week (NOW() \- INTERVAL '7 days'). |
| approved\_by | VARCHAR(64) | Realistic four-eyes approver tags: SYNTH\_CHIEF\_DEALER\_NY, SYNTH\_DESK\_HEAD\_LDN. |

#### **Table 2: settlement\_cutoffs\_and\_policies (Default Seed: 28 Pairs × 5 Tenors \= 140 Rows)**

| Column Name | SQL Type | Synthetic Value Generation Rule |
| :---- | :---- | :---- |
| ccy\_pair | VARCHAR(10) (PK) | All 28 currency pairs. |
| tenor | VARCHAR(10) (PK) | SPOT, TOM, NEXT, 1W, 1M. |
| cls\_eligible | BOOLEAN | TRUE for G10 Majors/Crosses \+ USD/MXN, USD/SGD, USD/HKD, USD/ZAR, USD/KRW; FALSE for USD/INR, USD/BRL, USD/CNH, USD/TRY. |
| cutoff\_time\_utc | TIME | 17:00:00 UTC for CLS-eligible pairs; 07:30:00 UTC for APAC; 15:30:00 UTC for LATAM. |
| min\_clip\_usd | NUMERIC(18,2) | 100.00 for SPOT (supports retail remittance portal); 10000.00 for forward tenors. |
| max\_clip\_usd | NUMERIC(18,2) | 50000000.00 for G10 Majors; 25000000.00 for G10 Crosses; 5000000.00 for EM pairs. |
| restricted\_flag | BOOLEAN | TRUE for USD/INR and USD/BRL (exercises onshore/NDF dealing desk escalation); FALSE for remaining 26 pairs. |

#### **Table 3 & 4: fx\_execution\_orders & child\_venue\_splits (Default Seed: numBatches × batchSize \= 10,000 Parent Orders \+ \~14,500 Child Splits)**

| Table & Column Name | SQL Type | Synthetic Value Generation Rule |
| :---- | :---- | :---- |
| fx\_execution\_orders.trade\_date | DATE (PK) | Distributed across trailing 7 trading days to populate multiple PostgreSQL range partitions. |
| fx\_execution\_orders.order\_id | UUID (PK) | Time-ordered UUIDv7. |
| fx\_execution\_orders.client\_order\_id | VARCHAR(64) | Prefixed SYNTH-ORD-\<YYYYMMDD\>-\<SEQ\> for deterministic cleanup via purgeSyntheticData=true. |
| fx\_execution\_orders.client\_id | VARCHAR(64) | Sampled from the 50 synthetic institutional, corporate, and retail pool clients. |
| fx\_execution\_orders.ccy\_pair | VARCHAR(10) | Weighted: 45% G10 Majors, 35% G10 Crosses, 20% EM pairs. |
| fx\_execution\_orders.side | VARCHAR(4) | Balanced 50% BUY / 50% SELL. |
| fx\_execution\_orders.notional\_usd | NUMERIC(18,2) | Tier-matched: $250–$10k (Retail), $100k–$5M (Commercial/Corporate), $10M–$45M (Institutional Multi-LP Sweep). |
| fx\_execution\_orders.executed\_rate | NUMERIC(14,6) | Realistic spot reference rate ± generated spread (e.g., 1.084250 EUR/USD, 153.420000 USD/JPY, 84.125000 USD/INR). |
| fx\_execution\_orders.lp\_id | VARCHAR(32) | Winning LP ID, INTERNAL\_BOOK (\~18% internalized), or MULTI\_LP\_SWEEP (\~15% institutional blocks). |
| fx\_execution\_orders.fill\_status | VARCHAR(24) | 98.6% FILLED, 0.9% FILLED\_AFTER\_FAILOVER, 0.5% ESCALATED\_DESK. |
| fx\_execution\_orders.slippage\_bps | NUMERIC(6,3) | \-0.150 to \+0.450 bps on standard fills; up to \+1.800 bps on waterfall failover orders. |
| fx\_execution\_orders.created\_at | TIMESTAMPTZ | Timestamp matching trade\_date and simulated session regime. |
| child\_venue\_splits.\* | UUID / NUMERIC / INT | Populates all 8 columns (split\_id, trade\_date, order\_id, lp\_id, split\_notional, lp\_quote\_rate, hold\_latency\_ms, drools\_score) across 1–4 child legs per parent order. |

### **3.2 Redis L2 & NoSQL Wide-Column Telemetry Stores**

| Target Store & Keyspace | Seed Volume | Every Field / Column Populated |
| :---- | :---- | :---- |
| **Redis L2:** quote:lp:{ccyPair}:{lpId} | 28 pairs × 12 LPs \= **336 Hashes** | bid, ask, available\_size ($1M–$25M), quote\_ts (current epoch ms), ttl\_ms (250–1000 ms). |
| **Redis L2:** credit:bilateral:{clientId}:{lpId} & nop:desk:{ccyPair} | 50 clients × 12 LPs \= **600 Credit Hashes** \+ **28 NOP Hashes** | max\_credit\_usd ($50M–$500M), utilized\_usd (15%–96% utilized), reserved\_usd (0), reset\_epoch (next NY 5 PM rollover). |
| **NoSQL Wide-Column:** lp\_telemetry\_projections | 28 pairs × 6 sessions × 12 LPs × 3 windows \= **6,048 Records** | PK (CCY\_PAIR\#SESSION\_BUCKET\#LP\_ID), SK (5M \| 1H \| SESSION), fill\_ratio\_pct, avg\_hold\_ms, last\_look\_reject\_rate, markout\_toxicity\_bps, ttl\_epoch (+7 days). |

### **3.3 Dedicated System Control-Plane DB (dlp\_sor\_system\_db.wf\_exec\_log)**

Every synthetic seeding or load-simulation execution populates **all 15 columns** of wf\_exec\_log: wf\_exec\_id, workflow\_type, dataset\_types, num\_batches, batch\_size, batches\_completed, records\_inserted, request\_params, execution\_summary (JSONB map of exact row counts inserted per table/store), status, started\_at, updated\_at, completed\_at, duration\_ms, triggered\_by, and error\_details.

## **4\. Batch Execution Mechanics & Safety Guardrails**

> * **Dependency-Ordered Batch Pipeline:** Executes in 4 deterministic phases per workflow run so foreign keys and telemetry projections exist before orders are generated: (1) Reference Schedules & Settlement Cutoffs (PostgreSQL JDBC batch), (2) Bilateral Credit & Live LP Quotes (Redis Pipeline), (3) NoSQL LP Telemetry Projections (Async Micro-Batch), and (4) Historical Parent/Child Execution Orders (PostgreSQL Partitioned Batch \+ Optional Kafka Test Topic).  
> * **Default-Driven API Contract:** Calling POST /api/v1/admin/synthetic/datasets/generate with an empty body {} automatically defaults to numBatches=10, batchSize=1000, all 28 currency pairs, and all 5 dataset types.  
> * **Production Environment Lock & Clean Teardown:** Hard-blocked in live PROD clusters via app.admin.synthetic.enabled=false (returns HTTP 403 Forbidden). Every seeded record carries a SYNTH\_ / SYNTH-ORD- prefix so DELETE /api/v1/admin/workflows/{wfExecId}?purgeSyntheticData=true cleanly removes synthetic test data without affecting baseline reference rows.