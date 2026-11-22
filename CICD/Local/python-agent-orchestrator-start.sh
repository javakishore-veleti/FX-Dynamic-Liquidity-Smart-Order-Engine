#!/usr/bin/env bash
set -euo pipefail
ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
PROJ="${ROOT_DIR}/Agentic-Python/fx-agent-orchestrator-service"
bash "${ROOT_DIR}/CICD/Local/python-uv-bootstrap.sh" "${PROJ}" "fx-agent-orchestrator" "FX Agent Orchestrator Service"
cd "${PROJ}"
exec uv run uvicorn fx_agent_orchestrator.app:app --host 0.0.0.0 --port "${FX_AGENT_ORCHESTRATOR_PORT:-8192}"
