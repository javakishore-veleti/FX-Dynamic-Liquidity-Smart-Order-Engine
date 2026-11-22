#!/usr/bin/env bash
set -euo pipefail
ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
PROJ="${ROOT_DIR}/Agentic-Python/fx-topology-mcp-server"
bash "${ROOT_DIR}/CICD/Local/python-uv-bootstrap.sh" "${PROJ}" "fx-topology-mcp" "FX Topology MCP Server"
cd "${PROJ}"
exec uv run uvicorn fx_topology_mcp.app:app --host 0.0.0.0 --port "${FX_TOPOLOGY_MCP_PORT:-8191}"
