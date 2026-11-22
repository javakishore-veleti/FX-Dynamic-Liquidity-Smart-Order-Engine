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
| **Regulatory Provenance** | Immutable audit trace for every routing and internalization decision | 100% compliance with FX Global Code, Dodd-Frank, and MiFID II RTS-27/28 |

## **2\. Monorepo & Microservice Topology (Zero Cross-Module Sharing)**

The platform is structured as a **Maven Reactor Monorepo** where the root pom.xml acts strictly as an aggregator and dependency version BOM, and each microservice module is a **100% self-contained Spring Boot 3.4+ (Java 21\) application** with zero shared Java codebases.

| Maven Module | Package Root | Core Functional Scope | Target Peak Load |
| :---- | :---- | :---- | :---- |
| **fx-sor-pricing-service** | com.jk.apps.banking.fx.sor.pricing | Pre-trade streaming quote aggregation, session spread guards, and L1/L2 rate-card caching | Up to 700,000 RPS (Edge \+ Pod Cache) |
| **fx-sor-precheck-service** | com.jk.apps.banking.fx.sor.precheck | Deterministic knock-out qualification, bilateral credit & Net Open Position (NOP) checks, client tier validation | Up to 200,000 RPS |
| **fx-sor-engine-service** | com.jk.apps.banking.fx.sor.engine | 5-step OrderRoutingFacade pipeline, Drools composite LP scoring, internalization check, multi-LP sweep & waterfall failover | Up to 100,000 RPS |

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
> * **FR-05 Policy Authoring Lifecycle:** All Drools/DMN rule updates require four-eyes Maker-Checker approval and historical shadow-mode replay validation before hot-reloading into live pods.

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

