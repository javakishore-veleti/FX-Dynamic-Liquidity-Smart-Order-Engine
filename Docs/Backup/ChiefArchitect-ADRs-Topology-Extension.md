# **ChiefArchitect-ADRs-Topology-and-Agentic-Extension (ADR-029 to ADR-032)**

**Repository:** fx-dynamic-liquidity-smart-order-engine  
**Extends Baseline:** ChiefArchitect-ADRs.md (ADR-001 through ADR-028)  
**Base Package Namespace:** com.jk.apps.banking.fx.sor.topology & com.jk.apps.banking.fx.sor.admin  
**Governance Status:** Chief Architect Approved Extension (4 ADRs)

## **9\. Real-Time Service Topology & Agentic Control Plane (ADR-029 to ADR-032)**

| ADR ID | Decision Title | Problem / Rejected Option | Approved Architectural Decision | Consequences & Guardrails |
| :---- | :---- | :---- | :---- | :---- |
| **ADR-029** | Three-Layer Physically Separated Service Topology | Hourly/daily batch dependency snapshots (stale during 3:00 AM incidents) or forcing eBPF flows, IPC metrics, and traces into a single monolithic graph DB. | Build streaming-first fx-topology-service with **three physically isolated storage layers**: Layer 1 Cilium eBPF Network Graph, Layer 2 Application IPC Graph, and Layer 3 Tail-Sampled OTel Traces in Apache Iceberg/Parquet, merged at query time in \< 500 ms. | Enables independent storage tuning per ingestion velocity while unifying network truth and FX domain metadata behind a single query API. |
| **ADR-030** | End-to-End Reactive Backpressure & Intermediary Stitching | Unbounded in-memory queues (OOM crashes under load spikes) or drop-based flow control (missing dependency edges during incidents). | Implement a **three-stage reactive backpressure pipeline** (Stage 1 Kafka windowed pre-aggregation \-\> Stage 2 load-balancer/proxy hop stitching \-\> Stage 3 bounded graph commit) that pauses upstream Kafka consumers when graph writes slow down. | Guarantees zero OOM crashes and complete topology fidelity during FOMC/NFP traffic bursts at the trade-off of brief seconds-level update lag. |
| **ADR-031** | Composite Graph Partitioning to Eliminate Hot-Node Skew | Partitioning graph edges solely by destination service ID (causing 100x write skew on central hubs like Redis L2, fx-qualify-service, and top Tier-1 LPs). | Key graph ingestion partitions by **composite tuple (src\_service\#dst\_service\#time\_bucket)** paired with 5-second tumbling window pre-aggregation before graph storage upsert. | Distributes write throughput evenly across graph shards and prevents hub-node bottlenecks at 1M aggregate RPS. |
| **ADR-032** | MCP Agentic Control-Plane Governance & Audit Logging | Granting autonomous AI agents unrestricted write access to production routing controls or allowing unlogged manual interventions. | Expose fx-topology-service (sensing) and fx-admin-service (actuation) via typed **Model Context Protocol (MCP) tools** with read-only autonomy for RCA/blast-radius analysis, mandatory four-eyes human approval for destructive actions (LP kill switches, credit overrides), and immutable logging in dlp\_sor\_system\_db.wf\_exec\_log. | Accelerates 3:00 AM root-cause analysis and canary validation from tens of minutes to seconds while preserving strict Tier-1 banking operational safety. |

