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

Requires [`uv`](https://docs.astral.sh/uv/). Startup runs `uv init` (if needed) + `uv sync`, then `uv run`.

```bash
# from repo root (preferred)
npm run local:topology-mcp:python

# or directly
cd Agentic-Python/fx-topology-mcp-server
uv sync
export TOPOLOGY_BASE_URL=http://localhost:8085
export ADMIN_BASE_URL=http://localhost:8084
uv run uvicorn fx_topology_mcp.app:app --host 0.0.0.0 --port 8191 --reload
```

Default port **8191** (Java sibling uses **8091**; Java agent orchestrator uses **8092**).

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
