# Python observability & schema migrations

## Micrometer equivalent (OpenTelemetry + Prometheus)

Java services use **Micrometer** (`/actuator/prometheus` + optional OTLP).  
Python FastAPI services use the same telemetry contract:

| Signal | Python | Endpoint / env |
|--------|--------|----------------|
| Metrics scrape | `prometheus-fastapi-instrumentator` | `GET /metrics` |
| Traces | OpenTelemetry FastAPI + httpx instrumentation | `OTEL_EXPORTER_OTLP_ENDPOINT` (default `http://localhost:4318`) |
| Disable OTEL | — | `OTEL_SDK_DISABLED=true` |

Applies to:
- `Agentic-Python/fx-topology-mcp-server` (:8191)
- `Agentic-Python/fx-agent-orchestrator-service` (:8192)

## Liquibase equivalent (Alembic — mandatory)

Python **must not** use `Base.metadata.create_all`. Schema changes go through **Alembic** (same role as Liquibase in Java):

- `alembic.ini` + `alembic/versions/*` per service  
- Startup runs `AlembicMigrationRunner.upgrade_to_head()` before opening repositories  

```bash
cd Agentic-Python/fx-agent-orchestrator-service
alembic upgrade head

cd Agentic-Python/fx-topology-mcp-server
alembic upgrade head
```
