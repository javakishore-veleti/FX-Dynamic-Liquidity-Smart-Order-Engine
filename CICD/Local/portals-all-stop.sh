#!/usr/bin/env bash
# Stop all Angular portals started via portals-all-start.sh
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
# shellcheck source=lib-local-run.sh
source "${SCRIPT_DIR}/lib-local-run.sh"

ensure_run_dirs portals

NAMES=(admin corporate retail)

echo "==> Stopping FX SOR portals"
for name in "${NAMES[@]}"; do
  stop_pid_file "${name}" "${RUN_ROOT}/portals/pids/${name}.pid"
done

echo
"${SCRIPT_DIR}/portals-all-status.sh"
