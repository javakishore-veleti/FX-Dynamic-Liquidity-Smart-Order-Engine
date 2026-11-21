# **Service Topology, Dual-Stack MCP Servers & Agentic Control-Plane Specification**

**Document ID:** Dual-Stack-Agentic-MCP-and-Topology-Spec.md  
**Repository:** fx-dynamic-liquidity-smart-order-engine  
**Base Java Namespace:** com.jk.apps.banking.fx.sor  
**Base Python Packages:** fx\_topology\_mcp & fx\_agent\_orchestrator  
**Architecture Style:** Neal Ford Closed-Layer Microservice Architecture (Zero-Sharing Polyglot Monorepo)  
**Target Scale:** 1,000,000 Aggregate RPS Ingestion / Sub-Second (\< 500 ms) Topology & Blast-Radius Query SLA

## **1\. Domain-Named Polyglot Monorepo Layout**

All service names reflect pure business and platform capabilities without encoding implementation languages (-spring- or \-python-). Java 21 Spring Boot / Spring AI modules reside in Middleware/ under the zero-sharing Maven Reactor BOM, while Python 3.12 FastAPI \+ LangChain/LangGraph modules reside in Agentic-Python/.  
`fx-dynamic-liquidity-smart-order-engine/`  
`├── pom.xml                                    # Root Maven Aggregator + BOM (Zero cross-module code sharing)`  
`├── Middleware/                                # Java 21 + Spring Boot 3.4+ / Spring AI`  
`│   ├── fx-quote-service/                      # Hot-path streaming pricing (~750k RPS)`  
`│   ├── fx-qualify-service/                    # Hot-path knock-out & bilateral credit (~200k RPS)`  
`│   ├── fx-route-exec-service/                 # Hot-path Drools scoring & LP routing (~50k RPS)`  
`│   ├── fx-admin-service/                      # Control plane, synthetic batching & dlp_sor_system_db.wf_exec_log`  
`│   ├── fx-topology-service/                   # 3-layer real-time service map (eBPF + IPC + Iceberg)`  
`│   ├── fx-topology-mcp-server/                # Domain-named Spring AI MCP Server (@Tool beans)`  
`│   └── fx-agent-orchestrator-service/         # Domain-named Spring AI Agent Orchestrator (Manual REST API)`  
`└── Agentic-Python/                            # Python 3.12 + FastAPI + LangChain/LangGraph`  
    `├── fx-topology-mcp-server/                # Identical domain-named Python FastAPI MCP Server`  
    `└── fx-agent-orchestrator-service/         # Identical domain-named Python LangGraph Orchestrator`

## **2\. Three-Layer Real-Time Service Topology (fx-topology-service)**

Inspired by Netflix's *Service Topology at Scale* architecture, fx-topology-service ingests three physically separated sources of truth through reactive backpressure pipelines and merges them in \< 500 ms via Java 21 Virtual Threads.

| Topology Layer | Telemetry Source | Physical Storage Engine | Enriched FX Domain Metadata Overlay | Programmatic Query Endpoint |
| :---- | :---- | :---- | :---- | :---- |
| **Layer 1: Network Graph** | Cilium eBPF Hubble L3/L4/L7 Flow Logs via Kafka | Graph Partition A (Neo4j / AWS Neptune) keyed by src\#dst\#bucket | pod\_namespace, az\_zone, egress\_lp\_gateway, socket\_retransmits | GET /api/v1/topology/graph/network?service={svc}\&depth=3 |
| **Layer 2: Application IPC Graph** | Micrometer / Prometheus Metrics & SSE Streams | Graph Partition B (Isolated Graph Store) | availability\_tier (TIER\_0\_CRITICAL), desk\_owner (NY\_EFX), p99\_ms, stp\_fill\_pct, circuit\_state | GET /api/v1/topology/graph/ipc?ccyPair=EURUSD\&window=5m |
| **Layer 3: Request Trace Layer** | OpenTelemetry Tail-Sampled Spans (0.1% nominal \+ 100% \> 10 ms outliers) | Columnar Parquet / Apache Iceberg on S3 | client\_order\_id, drools\_rule\_version, knockout\_reason, winning\_lp\_id | GET /api/v1/topology/traces/outliers?minLatencyMs=10 |
| **Unified Merger & Time-Travel API** | Parallel Virtual Thread Fan-Out Merger | Sub-second unified graph & snapshot diff engine | Merges Network \+ IPC \+ Trace layers in **\< 500 ms** | POST /api/v1/topology/blast-radius GET /api/v1/topology/diff?from={t1}\&to={t2} |

## **3\. Four Autonomous Agentic AI Capabilities (Manual REST Trigger Today)**

Both the Java Spring AI and Python LangGraph implementations of fx-agent-orchestrator-service expose identical manual REST trigger endpoints today (returning HTTP 202 Accepted \+ wfExecId logged in dlp\_sor\_system\_db.wf\_exec\_log), ready for future event-driven triggers.

| Agent Capability | Manual Trigger Endpoint | MCP Tools Invoked | Execution Workflow & Future Automated Trigger |
| :---- | :---- | :---- | :---- |
| **1\. Blast-Radius & Change-Validation Agent** | POST /api/v1/agents/blast-radius/analyze | get\_service\_dependencies, calculate\_blast\_radius, diff\_topology\_snapshots | **Today:** Manually triggered before Drools rule hot-reload or pod drain to assess downstream STP impact and alert NY/London desk owners. **Future:** Argo Rollouts pre-promotion webhook. |
| **2\. Backpressure & Lag Remediation Agent** | POST /api/v1/agents/backpressure/assess | get\_kafka\_consumer\_lag, get\_pod\_zgc\_and\_cpu\_metrics, recommend\_or\_apply\_scaling | **Today:** Manually triggered during load spikes to evaluate Stage 1–3 reactive backpressure, Kafka lag, and ZGC metrics. **Future:** Prometheus KafkaConsumerLagHigh alert webhook. |
| **3\. 3:00 AM Incident & Time-Travel RCA Agent** | POST /api/v1/agents/incident-rca/investigate | query\_iceberg\_outlier\_traces, get\_ipc\_edge\_health, propose\_or\_execute\_lp\_halt | **Today:** Manually triggered during latency spikes to correlate time-travel graph diffs with Iceberg traces (\> 10 ms) and pinpoint toxic LPs or hot Redis shards. **Future:** PagerDuty / Alertmanager Sev-1 webhook. |
| **4\. Synthetic Chaos & Topology-Stress Agent** | POST /api/v1/agents/chaos-sim/execute | trigger\_synthetic\_seed, trigger\_skewed\_load\_sim, poll\_workflow\_status | **Today:** Manually triggered in staging to inject 100x skewed hot-partition traffic (e.g., EUR/USD) via fx-admin-service and verify backpressure recovery. **Future:** Nightly CI/CD chaos schedule. |

## **4\. Standardized 8-Tool MCP Catalog (fx-topology-mcp-server)**

Implemented identically across Java 21 (Spring AI @Tool beans) and Python 3.12 (FastAPI \+ FastMCP class methods) over Streamable HTTP/SSE (/mcp).

| MCP Tool Name | Input DTO Contract | Target Backend Service | Governance & Safety Tier |
| :---- | :---- | :---- | :---- |
| get\_service\_dependencies | ServiceDependencyQueryDto (serviceName, layer, depth) | fx-topology-service | Read-Only (Unrestricted Agent Access) |
| calculate\_blast\_radius | BlastRadiusRequestDto (serviceName, ccyPair) | fx-topology-service | Read-Only (Unrestricted Agent Access) |
| diff\_topology\_snapshots | TopologyDiffRequestDto (fromTs, toTs) | fx-topology-service | Read-Only (Unrestricted Agent Access) |
| query\_iceberg\_outlier\_traces | TraceOutlierQueryDto (serviceName, minLatencyMs, limit) | fx-topology-service / Iceberg | Read-Only (Unrestricted Agent Access) |
| get\_kafka\_consumer\_lag | ConsumerLagQueryDto (consumerGroup) | Prometheus / Kafka Admin | Read-Only (Unrestricted Agent Access) |
| trigger\_synthetic\_seed | SyntheticSeedRequestDto (numBatches, batchSize, datasets) | fx-admin-service | Controlled Write (Non-Prod Only \+ wf\_exec\_log) |
| trigger\_skewed\_load\_sim | SkewedLoadSimRequestDto (targetRps, durationSec, ccyPair) | fx-admin-service | Controlled Write (Non-Prod Only \+ wf\_exec\_log) |
| propose\_or\_execute\_lp\_halt | LpVenueHaltRequestDto (lpId, reason, dryRun, approvalToken) | fx-admin-service | High-Risk Mutation (Requires dryRun=false \+ Maker-Checker approvalToken) |

## **5\. Python FastAPI Closed-Layer & Strict DTO-Only Engineering Standards**

All services under Agentic-Python/ mirror Neal Ford's Closed-Layer Architecture and Spring Boot conventions with strict OOP enforcement:

> * **100% Class-Based Encapsulation:** Zero standalone module-level functions for controllers, LangGraph builders, MCP tool providers, or repositories. Every component is a class wired via constructor dependency injection (\_\_init\_\_).  
> * **Strict DTO-Only Method Signatures:** Every method across presentation, business, services, and persistence layers accepts **a single immutable Pydantic v2 DTO** (ConfigDict(frozen=True)) and returns a typed DTO—zero independent positional or keyword arguments.  
> * **Closed-Layer Directory Hierarchy:** presentation/ (controllers \+ DTOs) \-\> business/ (LangGraph state graphs \+ facades \+ DTOs) \-\> services/ (MCP adapters \+ DTOs) \-\> persistence/ (dlp\_sor\_system\_db.wf\_exec\_log repository \+ entities \+ DTOs).  
> * **Automated Import Fitness Functions (import-linter):** Enforces closed-layer dependency rules in CI (analogous to ArchUnit in Java) and blocks any method signature accepting primitive arguments outside DTO wrappers.  
> * **Unified Audit Trail:** Both Java Spring AI and Python LangGraph orchestrators log all runs to dlp\_sor\_system\_db.wf\_exec\_log with triggered\_by \= 'SPRING\_AI\_AGENT' or 'LANGGRAPH\_AGENT'.

## **6\. Extended Chief Architect ADRs (ADR-029 to ADR-034)**

| ADR ID | Decision Title | Problem / Rejected Option | Approved Architectural Decision | Consequences & Guardrails |
| :---- | :---- | :---- | :---- | :---- |
| **ADR-029** | Three-Layer Physical Topology Separation | Storing eBPF network flows, IPC metrics, and distributed traces in a single database (causes write contention and query degradation). | Physically isolate **Network Graph (eBPF)**, **IPC Graph (Micrometer)**, and **Trace Layer (Parquet/Iceberg)** behind a parallel Virtual Thread query merger. | Sub-500 ms unified graph queries while allowing each storage engine to scale independently. |
| **ADR-030** | Reactive Backpressure Ingestion Pipelines | Unbounded in-memory queues (OOM crashes under bursts) or drop-based flow control (incomplete topology maps). | Implement end-to-end **reactive backpressure** across Kafka consumers and graph writers so downstream saturation slows upstream fetch rates gracefully. | Zero data loss or pod OOMs during 1M RPS market volatility spikes. |
| **ADR-031** | Composite Partition Keys for Graph Skew Prevention | Keying graph writes solely by destination node (causes 100x hot-shard skew on hubs like Redis L2 and fx-qualify-service). | Partition graph ingestion streams by composite tuple (src\_service\#dst\_service\#time\_bucket). | Distributes graph write IOPS evenly across partitions during peak trading hours. |
| **ADR-032** | Human-in-the-Loop Agentic Control-Plane Governance | Allowing autonomous AI agents to execute live venue halts or credit overrides without audit trails or approval gates. | Default all agent runs to dryRun=true, mandate Maker-Checker approvalToken for live mutations, and log every action in dlp\_sor\_system\_db.wf\_exec\_log. | Combines sub-minute autonomous RCA with strict Tier-1 banking operational risk controls. |
| **ADR-033** | Domain-Driven Polyglot Naming & Dual-Stack MCP Parity | Encoding implementation languages (-spring-, \-python-) into microservice names or diverging tool schemas across stacks. | Use identical domain names (fx-topology-mcp-server, fx-agent-orchestrator-service) across Middleware/ (Java Spring AI) and Agentic-Python/ (FastAPI/LangGraph) sharing an 8-tool MCP contract. | Zero tech-stack leakage in service names and full cross-compatibility between Java and Python agents. |
| **ADR-034** | Python Closed-Layer OOP & Strict DTO-Only Contracts | Procedural Python scripts with unstructured function arguments that diverge from Spring Boot architectural standards. | Enforce 100% class-based encapsulation, Neal Ford closed layers, frozen Pydantic v2 DTO-only method signatures, and CI import-linter fitness rules. | Maintains strict architectural symmetry and compile/CI-time safety across both JVM and Python repositories. |

