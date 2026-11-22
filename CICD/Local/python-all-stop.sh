#!/usr/bin/env bash
# Stop background Python dual-stack services (topology-mcp, agent-orchestrator).
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
# shellcheck source=lib-local-run.sh
source "${SCRIPT_DIR}/lib-local-run.sh"

ensure_run_dirs python

echo "==> Stopping FX SOR Python dual-stack services"
stop_pid_file "topology-mcp" "${RUN_ROOT}/python/pids/topology-mcp.pid"
stop_pid_file "agent-orchestrator" "${RUN_ROOT}/python/pids/agent-orchestrator.pid"

# Best-effort: clear listeners if pid files were lost / child processes remain
kill_port_listeners() {
  local port="$1"
  if ! port_listening "${port}"; then
    return 0
  fi
  if command -v lsof >/dev/null 2>&1; then
    local pids
    pids="$(lsof -nP -iTCP:"${port}" -sTCP:LISTEN -t 2>/dev/null || true)"
    if [[ -n "${pids}" ]]; then
      echo "  [KILL] port ${port} listeners: ${pids//$'\n'/ }"
      # shellcheck disable=SC2086
      kill -TERM ${pids} 2>/dev/null || true
      sleep 1
      # shellcheck disable=SC2086
      kill -KILL ${pids} 2>/dev/null || true
    fi
  fi
}

kill_port_listeners "${FX_TOPOLOGY_MCP_PORT:-8191}"
kill_port_listeners "${FX_AGENT_ORCHESTRATOR_PORT:-8192}"

# Pattern fallback for uv/uvicorn orphans without pid files
pkill -f 'uvicorn fx_topology_mcp.app:app' 2>/dev/null || true
pkill -f 'uvicorn fx_agent_orchestrator.app:app' 2>/dev/null || true
sleep 1
kill_port_listeners "${FX_TOPOLOGY_MCP_PORT:-8191}"
kill_port_listeners "${FX_AGENT_ORCHESTRATOR_PORT:-8192}"

echo "==> Python dual-stack stop complete"
