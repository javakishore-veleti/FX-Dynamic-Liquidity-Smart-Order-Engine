#!/usr/bin/env bash
# Status for background Python dual-stack services.
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
# shellcheck source=lib-local-run.sh
source "${SCRIPT_DIR}/lib-local-run.sh"

ensure_run_dirs python

echo "==> Python dual-stack status"
print_status_row "topology-mcp" "${FX_TOPOLOGY_MCP_PORT:-8191}" "${RUN_ROOT}/python/pids/topology-mcp.pid"
print_status_row "agent-orchestrator" "${FX_AGENT_ORCHESTRATOR_PORT:-8192}" "${RUN_ROOT}/python/pids/agent-orchestrator.pid"
echo "URLs: topology-mcp http://localhost:${FX_TOPOLOGY_MCP_PORT:-8191} | agent-orchestrator http://localhost:${FX_AGENT_ORCHESTRATOR_PORT:-8192}"
echo "Logs: ${RUN_ROOT}/python/logs/"
