# **ChiefArchitect-ADRs: FX Dynamic Liquidity & Smart Order Routing Engine**

**Repository:** fx-dynamic-liquidity-smart-order-engine  
**Base Package Namespace:** com.jk.apps.banking.fx.sor  
**Architecture Style:** Neal Ford Closed-Layer Microservice Architecture (Zero-Sharing Maven Reactor Monorepo)  
**Target Throughput:** Up to 1,000,000 Aggregate RPS across Kubernetes (50–150+ Pods)  
**Governance Status:** Chief Architect Approved Baseline (28 ADRs)

## **1\. Compute, Runtime & Networking (ADR-001 to ADR-004)**

| ADR ID | Decision Title | Problem / Rejected Option | Approved Architectural Decision | Consequences & Guardrails |
| :---- | :---- | :---- | :---- | :---- |
| **ADR-001** | Horizontal Pod Density Over Vertical Packing | Running 10 massive pods at 100k RPS/pod (\~10 µs/req CPU budget, socket exhaustion, catastrophic single-node blast radius). | Scale horizontally across **50–150+ Kubernetes pods** sized at **7,000–15,000 RPS/pod** across pricing, pre-check, and execution tiers. | Preserves CPU headroom for JSON/FIX serialization and rule evaluation; survives node drains without cascading failure. |
| **ADR-002** | Java 21+ Virtual Threads & Generational ZGC | Blocking servlet thread pools (OS thread exhaustion) and G1GC (multi-ms stop-the-world pauses breaking \< 5 ms p99 SLA). | Standardize on **Java 21+ Virtual Threads** paired with **Generational ZGC** across all trading microservices. | Sub-millisecond GC pause times regardless of heap size; requires strict avoidance of synchronized monitor pinning on I/O. |
| **ADR-003** | eBPF Kernel Networking (Cilium) | Legacy kube-proxy with iptables (sequential rule traversal and Linux conntrack table exhaustion at 1M RPS). | Mandate **eBPF-based CNI networking (Cilium)** in kube-proxy-replacement mode across all trading clusters. | Eliminates conntrack exhaustion and ephemeral port starvation during sub-second market data bursts. |
| **ADR-004** | Edge API Gateway Traffic Shedding | Allowing repetitive public/rate-card quote polling (70–90% of ingress volume) to hit core JVM trading pods. | Terminate TLS, enforce rate limits, and cache repetitive quote reads at the **Edge API Gateway / CDN layer**. | Reduces core pod compute load by up to 80% during session overlaps and macroeconomic news releases. |

## **2\. Application Architecture & Build Isolation (ADR-005 to ADR-008)**

| ADR ID | Decision Title | Problem / Rejected Option | Approved Architectural Decision | Consequences & Guardrails |
| :---- | :---- | :---- | :---- | :---- |
| **ADR-005** | Zero-Sharing Maven Reactor Monorepo | Polyrepos (library version drift) and shared internal domain JARs (tight compile-time coupling and cascading rebuilds). | Root pom.xml acts strictly as aggregator/BOM; each microservice builds as a **100% self-contained Spring Boot app** with zero shared code. | Guarantees independent deployments, isolated dependency upgrades, and path-filtered CI/CD pipelines. |
| **ADR-006** | Neal Ford Closed-Layer Internal Topology | Unstructured packages where FIX/REST transport details leak into pricing and Drools qualification rules. | Enforce **\[CLOSED\] Presentation, Business Orchestration, and Persistence** layers with an **\[OPEN\] Shared Services** layer per module. | Decouples business logic from protocols and storage engines; verified at compile time via ArchUnit fitness tests. |
| **ADR-007** | Declarative Drools/DMN Policy Hot-Reload | Hardcoding LP scoring matrices and session spread guards in Java (multi-week release cycles). | Externalize policies into versioned **Drools/DMN decision tables** with 4-eyes Maker-Checker sign-off and shadow replay before live hot-reload. | Enables \< 5 min zero-downtime policy updates while preventing unvalidated rules from misrouting live institutional flow. |
| **ADR-008** | Resilience4j Bulkheads & Circuit Breakers | A degraded or slow external Liquidity Provider (LP) venue stalling upstream routing pipelines. | Wrap every external LP adapter and network call in **Resilience4j semaphore bulkheads**, strict latency timeouts, and circuit breakers. | Isolates failing LPs within milliseconds and triggers deterministic waterfall failover to backup venues. |

## **3\. Caching & Distributed State Management (ADR-009 to ADR-011)**

| ADR ID | Decision Title | Problem / Rejected Option | Approved Architectural Decision | Consequences & Guardrails |
| :---- | :---- | :---- | :---- | :---- |
| **ADR-009** | Two-Tier Caching (Caffeine L1 \+ Redis L2) | Fetching rule sets, session calendars, and client tier schedules over the network on every request at 1M RPS. | Combine sub-microsecond **Caffeine L1 local heap caching** inside each pod with **Redis/Lettuce L2 distributed caching**. | Near-zero read latency on hot reference data while bounding pod heap via explicit size and TTL eviction policies. |
| **ADR-010** | Event-Driven L1 Cache Invalidation | Local L1 caches across 50–150+ pods serving stale spreads or outdated policy tables after a hot-reload. | Broadcast immediate cache-eviction events via **Redis Pub/Sub & Kafka** upon policy, session, or client tier updates. | Sub-second cache coherency across the entire Kubernetes fleet without polling overhead. |
| **ADR-011** | Atomic Redis Lua Credit & NOP Limits | Concurrent order evaluations across dozens of pods racing on counterparty bilateral credit lines and NOP ceilings. | Execute multi-key credit checks, exposure reservations, and TTL leases inside **atomic Redis Lua scripts** in one round-trip. | Prevents hard settlement rejections and credit over-allocation without distributed database locks. |

## **4\. Relational & NoSQL Data Modeling, Reads & Inserts (ADR-012 to ADR-016)**

| ADR ID | Decision Title | Problem / Rejected Option | Approved Architectural Decision | Consequences & Guardrails |
| :---- | :---- | :---- | :---- | :---- |
| **ADR-012** | Polyglot Persistence Split | Forcing one database engine to handle ACID governance, sub-ms telemetry lookups, and 1M RPS audit writes. | Split across **PostgreSQL/Aurora** (ACID reference/config), **NoSQL wide-column/document** (LP telemetry/state), and **Kafka** (audit log). | Eliminates relational locking from the hot path while preserving ACID integrity for settlement and counterparty agreements. |
| **ADR-013** | Relational Partitioning & Read Replicas | Unpartitioned SQL tables degrading under historical queries and blocking hot-path execution. | Model counterparty agreements and settlement rails in PostgreSQL with **declarative range/list partitioning** and read replicas; ban hot-path SQL writes. | Predictable query plans and zero relational write contention during peak market hours. |
| **ADR-014** | NoSQL Query-First Composite Partition Keys | Naive partition keys (LP\_ID or DATE) creating severe hot partitions on major pairs like EUR/USD. | Design NoSQL tables around query-first access patterns using high-cardinality composite keys (CURRENCY\_PAIR\#SESSION\_BUCKET\#LP\_ID). | Distributes read/write IOPS evenly across storage shards and eliminates expensive cross-partition scans. |
| **ADR-015** | Async NoSQL Micro-Batching & TTL Lifecycle | Synchronous single-row inserts at 1M RPS exhausting connection pools and driving prohibitive write costs. | Buffer fill telemetry and markout events asynchronously into **micro-batched NoSQL upserts** with automated **TTL expiration**. | Sustains massive write throughput with bounded storage growth and zero impact on synchronous routing latency. |
| **ADR-016** | Sub-Millisecond NoSQL Read Projections | Computing rolling LP fill ratios, last-look hold latencies, and toxicity scores on the fly during qualification. | Pre-compute rolling 5m/1h/session LP quality metrics into **denormalized NoSQL materialized projections** keyed by lookup tuple. | Enables single-partition point reads in **\< 1 ms** during order qualification. |

## **5\. Event-Driven Architecture & Streaming Pipelines (ADR-017 to ADR-019)**

| ADR ID | Decision Title | Problem / Rejected Option | Approved Architectural Decision | Consequences & Guardrails |
| :---- | :---- | :---- | :---- | :---- |
| **ADR-017** | Async Kafka Regulatory Provenance | Synchronous audit persistence blocking the \< 15 ms execution path for FX Global Code / MiFID II traces. | Stream immutable decision provenance asynchronously to **Apache Kafka**, consumed in scheduled batches by Airflow DAGs for warehouse loading. | 100% regulatory explainability capture with zero synchronous I/O penalty on client orders. |
| **ADR-018** | Deterministic Partitioning & EOS | Out-of-order execution reports or duplicate child fills corrupting position tracking. | Partition Kafka order topics by ClientOrderId and enable **idempotent producers with Exactly-Once Semantics (EOS)**. | Preserves strict causal ordering per trade lifecycle without distributed two-phase commit overhead. |
| **ADR-019** | Schema Registry & Contract-First Events | Implicit JSON changes causing runtime deserialization failures across zero-sharing Maven modules. | Enforce **Avro/Protobuf schemas** in a central Schema Registry with BACKWARD\_TRANSITIVE compatibility checks at build time. | Enables independent service deployments while blocking breaking contract changes before merge. |

## **6\. Observability, Monitoring & Application Logging (ADR-020 to ADR-023)**

| ADR ID | Decision Title | Problem / Rejected Option | Approved Architectural Decision | Consequences & Guardrails |
| :---- | :---- | :---- | :---- | :---- |
| **ADR-020** | OpenTelemetry with Tail-Based Sampling | 100% trace capture saturating network/collector bandwidth and adding serialization overhead at 1M RPS. | Use **OpenTelemetry** with head-based probabilistic sampling (**0.1%–1%**) on nominal flow and **100% tail capture** on errors/outliers (\> 10 ms). | Full diagnostic visibility into every slow or failed trade with negligible steady-state CPU/network cost. |
| **ADR-021** | High-Cardinality Metrics Governance | Unbounded tags (OrderId, ClientId) causing Prometheus TSDB memory exhaustion. | Restrict Micrometer labels to bounded dimensions (service, currency\_pair, lp\_id, session\_regime, outcome). | Protects TSDB stability while powering real-time p99 latency and STP fill-rate alerting. |
| **ADR-022** | Non-Blocking Structured JSON Logging | Synchronous console/file logging blocking Virtual Threads and triggering GC allocation spikes. | Use **structured JSON logging** backed by **LMAX Disruptor / Log4j2 async ring buffers** and pre-allocated thread-local buffers. | Eliminates I/O blocking during log generation and prevents logging bursts from degrading p99 latency. |
| **ADR-023** | Tiered Log Ingestion & Role Dashboards | Indexing 100% of operational logs in hot search clusters at prohibitive cost. | Route critical errors/circuit-breaker alerts to **hot indexed search (OpenSearch/Splunk)** for SRE/Dealing Desk views; offload bulk logs to **S3/Iceberg**. | Sub-second incident triage dashboards combined with cost-efficient long-term regulatory retention. |

## **7\. Application Security, Authentication & Authorization (ADR-024 to ADR-026)**

| ADR ID | Decision Title | Problem / Rejected Option | Approved Architectural Decision | Consequences & Guardrails |
| :---- | :---- | :---- | :---- | :---- |
| **ADR-024** | Edge JWT Auth & Local JWKS Caching | Synchronous external Identity Provider (IdP) token introspection on every request at 1M RPS. | Validate institutional **OAuth2/JWT tokens** at the Edge Gateway and cache rotating JWKS public keys in Caffeine L1 memory for local verification. | Adds **\< 0.1 ms** per-request auth overhead with zero runtime external IdP dependency. |
| **ADR-025** | Zero-Trust East-West Cilium/SPIFFE mTLS | Managing Java keystores inside pods and paying JVM TLS handshake overhead on east-west calls. | Enforce transparent **mTLS and SPIFFE workload identity** at the Cilium eBPF network layer across all trading microservices. | Zero-trust east-west encryption and micro-segmentation with zero JVM TLS overhead or keystore churn. |
| **ADR-026** | Vault Secrets & Banking Field Redaction | Static K8s Secrets or accidental logging of counterparty pricing spreads and account identifiers. | Source dynamic credentials from **HashiCorp Vault / KMS** and enforce runtime **log/trace redaction filters** for sensitive client/spread fields. | Eliminates static credential exposure and prevents commercial data leakage into telemetry pipelines. |

## **8\. Automation, CI/CD & Architectural Governance (ADR-027 to ADR-028)**

| ADR ID | Decision Title | Problem / Rejected Option | Approved Architectural Decision | Consequences & Guardrails |
| :---- | :---- | :---- | :---- | :---- |
| **ADR-027** | Path-Filtered CI/CD & Canary Rollouts | Rebuilding and redeploying all microservices on every commit, increasing blast radius. | Trigger **path-filtered GitHub Actions** per Maven module paired with **Argo Rollouts canary deployments** gated on Prometheus SLAs. | Safe, independent zero-downtime service releases with automated rollback on latency or STP regression. |
| **ADR-028** | Automated Fitness Functions (ArchUnit & k6) | Architectural rules around closed layers, zero sharing, and \< 5 ms latency eroding over time. | Run compile-time **ArchUnit boundary tests** in every build and automated **k6 load gates** in staging prior to production promotion. | Enforces architectural invariants automatically as executable CI checks. |

