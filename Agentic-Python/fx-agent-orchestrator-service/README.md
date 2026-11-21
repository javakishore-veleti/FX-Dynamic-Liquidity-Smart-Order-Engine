# fx-agent-orchestrator-service (Python)

Domain-named FastAPI + LangGraph agent orchestrator (ADR-032 / ADR-034). Identical four manual REST triggers as `Middleware/fx-agent-orchestrator-service`.

## Architecture

Closed layers (Neal Ford):

```
presentation/  → controllers + request/response DTOs
business/      → LangGraph workflow stubs + facade + DTOs
services/      → MCP HTTP adapter (:8091) + DTOs
persistence/   → agent_run_log repository + entities + DTOs
```

Rules (ADR-034):

- 100% class-based (`__init__` DI)
- Every method accepts **one frozen Pydantic v2 DTO** and returns a typed DTO
- `triggered_by=LANGGRAPH_AGENT` on every `agent_run_log` row
- `dryRun` defaults to `true` (ADR-032)

## Run

```bash
cd Agentic-Python/fx-agent-orchestrator-service
python -m venv .venv && source .venv/bin/activate
pip install -r requirements.txt
export FX_MCP_BASE_URL=http://localhost:8091
uvicorn fx_agent_orchestrator.app:app --host 0.0.0.0 --port 8192 --reload
```

Default port **8192** (Java sibling uses **8092**).

## Endpoints (HTTP 202)

| Method | Path | Agent |
|--------|------|-------|
| POST | `/api/v1/agents/blast-radius/analyze` | Blast-Radius & Change-Validation |
| POST | `/api/v1/agents/backpressure/assess` | Backpressure & Lag Remediation |
| POST | `/api/v1/agents/incident-rca/investigate` | Incident & Time-Travel RCA |
| POST | `/api/v1/agents/chaos-sim/execute` | Synthetic Chaos & Topology-Stress |

## Import linter

```bash
lint-imports
```
