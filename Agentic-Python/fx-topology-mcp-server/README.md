# fx-topology-mcp-server (Python)

Domain-named FastAPI MCP tool server (ADR-033 / ADR-034). Identical 8-tool catalog as `Middleware/fx-topology-mcp-server`.

## Architecture

Closed layers (Neal Ford):

```
presentation/  → controllers + request DTOs
business/      → facades + orchestration DTOs
services/      → HTTP adapters (topology :8085, admin :8084) + DTOs
persistence/   → mcp_tool_invocation_log repository + entities + DTOs
```

Rules (ADR-034):

- 100% class-based (`__init__` DI)
- Every method accepts **one frozen Pydantic v2 DTO** and returns a typed DTO
- Import fitness: `lint-imports` via `.importlinter`

## Run

```bash
cd Agentic-Python/fx-topology-mcp-server
python -m venv .venv && source .venv/bin/activate
pip install -r requirements.txt
export TOPOLOGY_BASE_URL=http://localhost:8085
export ADMIN_BASE_URL=http://localhost:8084
uvicorn fx_topology_mcp.app:app --host 0.0.0.0 --port 8092 --reload
```

Default port **8092** (Java sibling uses **8091**).

## Endpoints

| Method | Path | Purpose |
|--------|------|---------|
| GET | `/mcp` | Tool catalog |
| POST | `/mcp/tools/get_service_dependencies` | Topology deps |
| POST | `/mcp/tools/calculate_blast_radius` | Blast radius |
| POST | `/mcp/tools/diff_topology_snapshots` | Snapshot diff |
| POST | `/mcp/tools/query_iceberg_outlier_traces` | Outlier traces |
| POST | `/mcp/tools/get_kafka_consumer_lag` | Consumer lag |
| POST | `/mcp/tools/trigger_synthetic_seed` | Admin synthetic seed |
| POST | `/mcp/tools/trigger_skewed_load_sim` | Admin skewed load |
| POST | `/mcp/tools/propose_or_execute_lp_halt` | LP halt (`dryRun` default `true`) |

## Import linter

```bash
lint-imports
```
