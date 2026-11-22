#!/usr/bin/env bash
# Start Python agent orchestrator in the background (does not hold the terminal).
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
# shellcheck source=lib-local-run.sh
source "${SCRIPT_DIR}/lib-local-run.sh"

ensure_run_dirs python
PROJ="${ROOT_DIR}/Agentic-Python/fx-agent-orchestrator-service"
NAME="agent-orchestrator"
PORT="${FX_AGENT_ORCHESTRATOR_PORT:-8192}"
pid_file="${RUN_ROOT}/python/pids/${NAME}.pid"
log_file="${RUN_ROOT}/python/logs/${NAME}.log"
WAIT_SEC="${FX_SOR_PYTHON_WAIT_SEC:-${FX_SOR_START_WAIT_SEC:-120}}"

bash "${SCRIPT_DIR}/python-uv-bootstrap.sh" "${PROJ}" "fx-agent-orchestrator" "FX Agent Orchestrator Service"

existing="$(read_pid "${pid_file}" || true)"
if [[ -n "${existing}" ]] && is_pid_alive "${existing}" && port_listening "${PORT}"; then
  echo "  [SKIP] ${NAME} already up pid=${existing} port=${PORT}"
  exit 0
fi
if port_listening "${PORT}"; then
  echo "  [SKIP] ${NAME} port ${PORT} already in use"
  exit 0
fi

echo "==> Starting ${NAME} → 0.0.0.0:${PORT}  (log: ${log_file})"
(
  cd "${PROJ}"
  if command -v setsid >/dev/null 2>&1; then
    setsid uv run uvicorn fx_agent_orchestrator.app:app --host 0.0.0.0 --port "${PORT}" >"${log_file}" 2>&1 &
    echo $! >"${pid_file}"
  else
    nohup uv run uvicorn fx_agent_orchestrator.app:app --host 0.0.0.0 --port "${PORT}" >"${log_file}" 2>&1 &
    echo $! >"${pid_file}"
  fi
)

set +e
wait_for_port "${NAME}" "${PORT}" "${pid_file}" "${log_file}" "${WAIT_SEC}"
wait_rc=$?
set -e

pid="$(read_pid "${pid_file}" || true)"
if (( wait_rc == 0 )); then
  echo "  [UP] ${NAME}  http://localhost:${PORT}  pid=${pid}"
else
  echo "  [FAIL] ${NAME} — see ${log_file}"
fi
exit "${wait_rc}"
