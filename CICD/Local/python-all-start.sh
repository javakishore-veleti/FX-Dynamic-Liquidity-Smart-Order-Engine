#!/usr/bin/env bash
# Start both Python dual-stack services in the background.
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

echo "==> Starting FX SOR Python dual-stack (background)"
set +e
bash "${SCRIPT_DIR}/python-topology-mcp-start.sh"
rc1=$?
bash "${SCRIPT_DIR}/python-agent-orchestrator-start.sh"
rc2=$?
set -e

echo
bash "${SCRIPT_DIR}/python-all-status.sh"

if (( rc1 != 0 || rc2 != 0 )); then
  exit 1
fi
exit 0
