#!/usr/bin/env bash
# Status of Angular portal local processes / ports
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
# shellcheck source=lib-local-run.sh
source "${SCRIPT_DIR}/lib-local-run.sh"

ensure_run_dirs portals

PORTALS=(
  "admin|4201"
  "corporate|4202"
  "retail|4203"
)

echo "==> Portals status"
for entry in "${PORTALS[@]}"; do
  IFS='|' read -r name port <<<"${entry}"
  print_status_row "${name}" "${port}" "${RUN_ROOT}/portals/pids/${name}.pid"
done

echo
echo "URLs: admin http://localhost:4201 | corporate http://localhost:4202 | retail http://localhost:4203"
echo "Logs: ${RUN_ROOT}/portals/logs/"
