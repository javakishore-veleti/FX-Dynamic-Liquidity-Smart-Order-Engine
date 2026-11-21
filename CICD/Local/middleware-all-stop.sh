#!/usr/bin/env bash
# Stop all Middleware services started via middleware-all-start.sh
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
# shellcheck source=lib-local-run.sh
source "${SCRIPT_DIR}/lib-local-run.sh"

ensure_run_dirs middleware

NAMES=(quote qualify route-exec admin topology topology-mcp agent-orchestrator)

echo "==> Stopping FX SOR middleware services"
for name in "${NAMES[@]}"; do
  stop_pid_file "${name}" "${RUN_ROOT}/middleware/pids/${name}.pid"
done

echo
"${SCRIPT_DIR}/middleware-all-status.sh"
