# ARCHITECTURE.md — System layout

Read [DESIGN.md](DESIGN.md) for the rules. Read [PRD-FX-SOR-001.md](PRD-FX-SOR-001.md) for the requirements. This file shows where each piece lives and how they connect.

---

## 1. Components

### Middleware (Spring Boot 3.4, Java 21)

| Service | Module | Port (local) | Database (local) | Role | PRD owner of |
|---|---|---|---|---|---|
| Quote | `Middleware/fx-quote-service` | 8081 | `fx_quote_db` | Client pricing, session markups, economic calendar, LP book, macro flag | FR-01 |
| Qualify | `Middleware/fx-qualify-service` | 8082 | `fx_qualify_db` | Knock-out checks, settlement rules, credit reservation, restricted currencies | FR-02, FR-04 |
| Route-exec | `Middleware/fx-route-exec-service` | 8083 | `fx_route_exec_db` | LP ranking, order split, failover, provenance | FR-03 |
| Admin | `Middleware/fx-admin-service` | 8084 | `dlp_sor_system_db` | Control plane: BPMN workflows, Initial Data, policy workflows, synthetic data | FR-05 |
| Topology | `Middleware/fx-topology-service` | 8085 | `fx_topology_db` | Service topology and blast-radius graph (supporting) | — |
| Topology MCP | `Middleware/fx-topology-mcp-server` | 8091 | `fx_topology_mcp_db` | MCP tool surface over topology (supporting) | — |
| Agent orchestrator | `Middleware/fx-agent-orchestrator-service` | 8092 | `fx_agent_orchestrator_db` | Agent workflows: RCA, macro guard, policy authoring assist (supporting) | FR-05 assist |

### Python services (`Agentic-Python/`, FastAPI)

| Service | Port (local) | Role |
|---|---|---|
| fx-topology-mcp-server | 8191 (default) | Python MCP server (dual-stack with Java) |
| fx-agent-orchestrator-service | — | Python orchestrator (dual-stack with Java) |
| fx-model-tuning | — | Batch fine-tuning pipeline (CLI, not a service) |

### Portals (Angular)

| Portal | Port (local) | Users | Main screens |
|---|---|---|---|
| Admin | 4201 | Dealing desk, SRE, risk, policy owners | Initial Data, Policy Studio, Workflows, Credit, LP book |
| Corporate | 4202 | Corporate treasury clients | Blotter, execution, TCA, alerts |
| Retail | 4203 | Retail and remittance clients | Convert, advisor, tracker |

### Infrastructure (Docker Compose, `CICD/Local/`)

| Component | Port | Used by |
|---|---|---|
| PostgreSQL 16 | 5432 | One database per service (list above), when the `local-postgres` profile is on |
| Redis 7.4 | 6379 | Quote, qualify, route-exec, admin, when the `load-redis` profile is on |
| Kafka 7.7 (with Zookeeper) | 9092 | Events, when the `load-kafka` profile is on |
| LiteLLM gateway | 4000 | LLM calls from agent services |
| OpenTelemetry collector | 4317 / 4318 | Traces and metrics export (off by default) |
| Prometheus | 9090 | Metrics |
| Grafana | 3000 | Dashboards |
| OpenSearch / Dashboards | 9200 / 5601 | Logs and Kibana views |

Local default: services run on H2 in memory. Data resets on restart unless the Postgres profile is used.

---

## 2. Layers inside a service

Quote, qualify, and route-exec follow the closed-layer layout:

```
presentation/controllers   REST endpoints
business/facade            business rules, one facade per capability
business/dtos              request and response shapes
services/adapters          outbound HTTP and Kafka clients
persistence/entities       JPA entities
persistence/repositories   repositories
cache/                     Caffeine L1 and Redis L2 stores
```

Admin follows its own flat layout, documented in [DESIGN.md §2](DESIGN.md#2-package-and-folder-rules):

```
controller/   REST endpoints
service/      orchestration (BpmnWorkflowService) and outbound clients
initdata/     Initial Data facade, view, catalog; initdata/tasks/ for its tasks
wf/           WfTask contract used by the workflow engine
bpmn/         FR-01 step catalog
entity/ repository/ dto/
```

Dependencies point down only. A controller calls a facade. A facade calls services and repositories. A task calls an outbound client, never a controller.

---

## 3. Key flows

### 3.1 Quote request (hot path, FR-01 and FR-03)

```
Corporate / Retail portal
  → Quote (8081): price with session markup, macro multiplier (L1 cache)
  → Qualify (8082): knock-outs, settlement, credit reservation
  → Route-exec (8083): rank LPs, split, failover; provenance to Kafka (off the critical path)
```

### 3.2 Initial Data setup (button-driven, FR-05 control plane)

```
Admin portal "Setup" button
  → Admin controller  POST /api/v1/admin/initial-data/{dataset}/setup
  → InitDataFacade    checks: not already COMPLETED, not already RUNNING
  → BpmnWorkflowService.start(INIT_*)   writes wf_exec_log (parent)
      → WfTask CalLoadTask   upserts seed rows via QuoteScheduleClient → Quote API
      → WfTask CalVerifyTask lists rows via QuoteScheduleClient, checks count
      → one wf_task_exec_log row per task (input_json, output_json, status, timings)
  → portal reads GET /api/v1/admin/initial-data and renders the task flow
```

### 3.3 Policy change (FR-05, maker-checker)

```
Admin portal Policy Studio
  → Admin: draft → submit → approve (proxied to Quote spread-schedule API)
  → Quote: applies to L1 cache, invalidates cache
  → Agent orchestrator (optional): drafting assist
```

---

## 4. Eventing and cache

| Item | Where | Notes |
|---|---|---|
| Kafka topics | Quote, qualify, route-exec, admin | Names follow `fx.<domain>.<event>.v<n>`. Enabled by the `load-kafka` profile. |
| Redis keys | Quote (macro flag), qualify (credit counters), route-exec (fill telemetry) | Enabled by the `load-redis` profile. |
| Caffeine L1 | Quote (spread schedules, calendar), qualify, route-exec | Always on. Invalidated by admin cache-invalidate endpoint. |
| Economic calendar | Quote, in memory | No table, no Redis. Lost on restart; reload with the Initial Data button. |

---

## 5. Open architecture items

1. Quote's economic calendar is in memory only. It needs a table or Redis key before the Initial Data setup can be relied on across restarts.
2. The old Reference Data pages are still routable but not in the nav.
3. Admin uses a flat layout while the other services use the closed-layer layout. The admin layout is kept to avoid a rewrite; new admin code follows DESIGN.md §2.
4. Route-exec and qualify do not yet expose Initial Data loaders. Those datasets (settlement, credit, LP book, LP rankings) are pending business confirmation.
