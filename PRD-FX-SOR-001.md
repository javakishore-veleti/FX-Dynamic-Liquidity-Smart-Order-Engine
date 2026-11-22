# **PRD-FX-SOR-001: FX Dynamic Liquidity & Smart Order Routing Engine**

**Document ID:** PRD-FX-SOR-001  
**Repository:** fx-dynamic-liquidity-smart-order-engine  
**Base Package Namespace:** com.jk.apps.banking.fx.sor  
**Architecture Style:** Neal Ford Closed-Layer Microservice Architecture (Zero-Sharing Maven Reactor Monorepo)  
**Target Throughput:** Up to 1,000,000 Aggregate RPS across Kubernetes (50–150+ Pods)

## **1\. Executive Summary & Business Objectives**

The **FX Dynamic Liquidity & Smart Order Routing (SOR) Engine** is an institutional-grade, policy-driven electronic Foreign Exchange (eFX) qualification and execution platform engineered for a major U.S. Tier-1 Commercial & Investment Bank. It replaces static counterparty routing tables with real-time market microstructure awareness, deterministic knock-out qualification, and dynamic Liquidity Provider (LP) scoring.

| Strategic Pillar | Business Outcome | Target SLA / Benchmark |
| :---- | :---- | :---- |
| **Margin Preservation** | Eliminates spread leakage by routing to optimal all-in wholesale liquidity | \+0.35 to \+0.80 bps net spread capture per \$1M traded |
| **Execution Certainty** | Penalizes LPs with elevated last-look rejection rates and hold latency | \> 98.5% first-pass Straight-Through Processing (STP) fill rate |
| **Low-Latency Decisioning** | Non-blocking rule evaluation and LP ranking on Java 21 Virtual Threads | \< 5 ms internal decisioning (p99); \< 15 ms end-to-end |
| **Policy Agility** | Maker-checker Drools/DMN decision tables hot-reloaded without downtime | \< 5 minutes policy rollout with shadow-mode replay |
| **Regulatory Provenance** | Immutable audit trace for every routing and internalization decision | Compliance with FX Global Code (incl. Principle 17 last-look disclosure); MiFID II RTS-27/28 and Dodd-Frank applicability to be confirmed by Compliance |

> **Note on numeric targets:** the figures in the table above are internal design targets. They are not sourced industry benchmarks. Each must be validated by the business and by performance testing before it is used as an acceptance criterion.

## **2\. Monorepo & Microservice Topology (Zero Cross-Module Sharing)**

The platform is structured as a **Maven Reactor Monorepo** where the root pom.xml acts strictly as an aggregator and dependency version BOM, and each microservice module is a **100% self-contained Spring Boot 3.4+ (Java 21\) application** with zero shared Java codebases.

| Maven Module (PRD name) | Repo module (this monorepo) | Package Root | Core Functional Scope | Target Peak Load |
| :---- | :---- | :---- | :---- | :---- |
| **fx-sor-pricing-service** | **fx-quote-service** | com.jk.apps.banking.fx.sor.quote | Pre-trade streaming quote aggregation, session spread guards, and L1/L2 rate-card caching | Up to 700,000 RPS (Edge \+ Pod Cache) |
| **fx-sor-precheck-service** | **fx-qualify-service** | com.jk.apps.banking.fx.sor.qual | Deterministic knock-out qualification, bilateral credit & Net Open Position (NOP) checks, client tier validation | Up to 200,000 RPS |
| **fx-sor-engine-service** | **fx-route-exec-service** | com.jk.apps.banking.fx.sor.exec | 5-step OrderRoutingFacade pipeline, Drools composite LP scoring, internalization check, multi-LP sweep & waterfall failover | Up to 100,000 RPS |

**Name map:** pricing → `quote` · precheck → `qualify` · engine → `route-exec`.

## **3\. Neal Ford Closed-Layer Architecture & 5-Step Routing Pipeline**

Each microservice enforces strict top-down layer isolation so external transport protocols (FIX/REST) never leak into business rules, and domain logic remains decoupled from storage engines.

> * **\[CLOSED\] Presentation Layer:** External API Gateways, FIX Protocol Adapters, schema validation, and service-local inbound/outbound DTOs.  
> * **\[CLOSED\] Business Orchestration Layer:** Executes the deterministic 5-step OrderRoutingFacade pipeline:  
  1. **Enrichment:** Parallel retrieval of live market depth, session regime (Tokyo, London/NY overlap, NY 5:00 PM rollover, macro news windows), and client tier profile.  
  2. **Deterministic Knock-Out:** Eliminates LPs lacking pair/tenor support, breaching min/max clip sizes, exceeding bilateral credit/NOP limits, or streaming stale quotes.  
  3. **Composite LP Scoring & Toxicity Adjustment:** Evaluates Drools/DMN matrices balancing top-of-book spread against rolling fill ratios, last-look hold times, and markout toxicity.  
  4. **Routing & Splitting Execution:** Selects internal principal book matching vs. single-LP best execution, multi-LP institutional sweep, or waterfall failover within slippage limits.  
  5. **Asynchronous Provenance Audit:** Emits immutable execution explainability payloads to Apache Kafka off the critical path.  
> * **\[OPEN\] Shared Domain & Integration Services:** Policy/Rules Engine wrapper, Transaction Cost Analysis (TCA) scoring, and Resilience4j-protected external LP venue adapters.  
> * **\[CLOSED\] Persistence & Distributed State Layer:** Caffeine L1 local memory cache, Redis/Lettuce L2 atomic credit/NOP counters, and PostgreSQL/Aurora operational stores.

## **4\. Functional & Governance Requirements**

> * **FR-01 Session & Regime Guardrails:** Automatically tighten spread tolerances and increase last-look penalties during NY rollover and high-impact macro releases (FOMC, NFP, ECB).  
> * **FR-02 Atomic Bilateral Credit & NOP Protection:** Verify and reserve counterparty credit lines in Redis using atomic Lua/pipeline operations prior to external order dispatch.  
> * **FR-03 Multi-LP Sweep & Waterfall Failover:** Split institutional block orders across top-ranked LPs to minimize market impact; cascade rejected remainders automatically within the order slippage budget.  
> * **FR-04 Dealing Desk Escalation:** Route fat-finger alerts, restricted emerging-market currencies, or zero-liquidity exceptions to NY/London dealing desks with full diagnostic context.  
> * **Cross-cutting: Last-look disclosure:** Where last look is used, it is disclosed to clients, and last-look rejection reasons are recorded per order. This follows FX Global Code Principle 17. Owners: `fx-qualify-service` (rejection reasons) and `fx-route-exec-service` (rejection recording).
> * **FR-05 Policy Authoring Lifecycle:** All Drools/DMN rule updates require four-eyes Maker-Checker approval and historical shadow-mode replay validation before hot-reloading into live pods.

## **4.1 PRD → Repo Microservice / Component Map**

Use this functional map when deciding **which service to touch** for each PRD objective.  
**Name map:** pricing → `fx-quote-service` · precheck → `fx-qualify-service` · engine → `fx-route-exec-service`.

### Strategic pillars → services & components

| Pillar | Primary services | Functional components (this repo) | How it shows up |
| :---- | :---- | :---- | :---- |
| **Margin Preservation** | `fx-quote-service`, `fx-route-exec-service` | `QuoteService` + `ClientTierSpreadSchedule` / markup by session; `QuoteBookCache` (L1/Redis); `RouteExecService` LP ranking & all-in selection | Tight wholesale LP chosen over static map; spread captured on indicative + route |
| **Execution Certainty** | `fx-qualify-service`, `fx-route-exec-service` | `QualifyService` knock-outs; `CreditStore` reserve; `RouteExecService.failover`; Kafka `fx.exec.last-look-failover.v1` | Bad LPs dropped; last-look remainder cascades; STP-oriented path |
| **Low-Latency Decisioning** | `fx-quote-service`, `fx-qualify-service`, `fx-route-exec-service` | Controllers + in-memory/Redis caches; `ServiceMetrics` (Micrometer); Virtual Threads in Spring Boot 3.4 / Java 21 | Hot path stays local cache + short pipeline; measure via Prometheus/Grafana |
| **Policy Agility** | `fx-admin-service` (+ agents assist) | `AdminWorkflowService` / policy & credit/venue ops; maker-checker intent via admin APIs; `PolicyAuthoringAgentWorkflow` (NL→rule assist) | Hot-reload / approve path without full redeploy (admin + rule lifecycle) |
| **Regulatory Provenance** | `fx-route-exec-service` | `RouteExecService.trace`; entities `FxExecutionOrder` / `ChildVenueSplit`; Kafka `fx.exec.order-routed-provenance.v1` | Immutable why-this-LP record for FX Global Code / MiFID-style evidence |

**Supporting (not hot-path money):** `fx-topology-service` / MCP / `fx-agent-orchestrator-service` for RCA, toxicity, and best-ex explain — they consume traces; they do not replace SOR.

### FR-01 … FR-05 → services & components

| FR | Owning microservice(s) | Components / APIs | Notes |
| :---- | :---- | :---- | :---- |
| **FR-01 Session & Regime Guardrails** | `fx-quote-service` (primary); agent assist | `QuoteService.currentSessionRegime()` / `resolveMarkup`; entity `ClientTierSpreadSchedule`; event `fx.quote.regime-shifted.v1`; `MacroGuardAgentWorkflow` | Tightens spreads / penalties by Tokyo–London–NY / news regimes |
| **FR-02 Atomic Credit & NOP** | `fx-qualify-service` (primary); `fx-admin-service` override | `CreditStore` / `RedisCreditStore` / `InMemoryCreditStore`; `QualifyService.tryReserve`; `GET .../credit/utilization/{clientId}`; admin `creditOverride` | Reserve before dispatch; Redis Lua when `load-redis` profile on |
| **FR-03 Multi-LP Sweep & Failover** | `fx-route-exec-service` | `RouteExecService` route-execute (modes / child splits); `POST .../orders/{orderId}/failover`; `ChildVenueSplit`; Kafka last-look failover topic | Split blocks; cascade rejects inside slippage budget |
| **FR-04 Dealing Desk Escalation** | `fx-qualify-service` (primary); admin halt | Knock-out path with `RESTRICTED_CCY` → `escalatedToDesk`; `emitVenueKnockout`; admin `POST .../venues/{lpId}/halt` | Fat-finger / restricted / zero-liquidity → desk context |
| **FR-05 Policy Authoring Lifecycle** | `fx-admin-service` (primary); agent assist | Admin workflows / maker-checker style ops; cache invalidate; synthetic/shadow tooling; `PolicyAuthoringAgentWorkflow` | Four-eyes + shadow replay before live Drools/DMN promote |

### One-line ownership

| Requirement | Owner |
| :---- | :---- |
| **FR-01** | `fx-quote-service` |
| **FR-02** | `fx-qualify-service` (+ admin override) |
| **FR-03** | `fx-route-exec-service` |
| **FR-04** | `fx-qualify-service` (+ admin halt) |
| **FR-05** | `fx-admin-service` (+ policy agent) |

Pillars live mainly on: **quote + qualify + route-exec**; provenance on **route-exec**; policy on **admin**.

## **5\. High-Throughput Runtime & Kubernetes Capacity Architecture (1M RPS)**

Sustaining **1,000,000 aggregate RPS** in production is unachievable on a small 10-pod cluster (which would demand 100,000 RPS per pod with zero headroom for JSON/FIX parsing, kernel socket churn, or node failures). The runtime architecture enforces horizontal pod scaling and low-latency JVM/kernel tuning:

| Infrastructure Layer | Production Specification | Engineering & SLA Rationale |
| :---- | :---- | :---- |
| **Kubernetes Pod Sizing** | **50 to 150+ Pods** (7,000–15,000 RPS/pod) | Maintains CPU headroom for serialization/rule evaluation and eliminates single-node blast radius during failovers |
| **Concurrency Model** | **Java 21+ Virtual Threads** (or WebFlux) | Handles massive concurrent downstream I/O (Redis, LP adapters) without OS platform-thread exhaustion |
| **JVM Garbage Collection** | **Generational ZGC / Shenandoah** | Sub-millisecond pause times across large heaps, protecting the \< 5 ms (p99) decisioning SLA |
| **Cluster Networking** | **eBPF (Cilium)** replacing iptables | Eliminates Linux conntrack table exhaustion and sequential rule traversal under high connection churn |
| **Edge & Tiered Caching** | **API Gateway \+ Caffeine L1 \+ Redis L2** | Absorbs 70–90% of repetitive quote reads at the edge/L1 before reaching core qualification pods |
| **Async Audit & Batching** | **Apache Kafka \+ Airflow Batch Consumers** | Keeps regulatory audit writes completely non-blocking on the hot path; downstream analytics consume via scheduled micro-batches |

| CI/CD & Deployment Isolation Note: Each Maven module builds an independent executable Spring Boot JAR and Docker image triggered strictly by path-filtered GitHub Actions workflows, ensuring zero cross-service deployment coupling. |
| :---- |

