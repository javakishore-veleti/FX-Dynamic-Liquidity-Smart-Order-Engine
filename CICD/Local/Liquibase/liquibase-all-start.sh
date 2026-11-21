#!/usr/bin/env bash
# Apply dedicated Liquibase changelogs for every middleware service (external flavor)
set -euo pipefail
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

SERVICES=(
  fx-quote-service
  fx-qualify-service
  fx-route-exec-service
  fx-admin-service
  fx-topology-service
  fx-topology-mcp-server
  fx-agent-orchestrator-service
)

echo "==> External Liquibase UPDATE for all middleware services"
echo "    Tip: set LIQUIBASE_ON_STARTUP=false when starting apps so migrations are not double-applied."
for svc in "${SERVICES[@]}"; do
  bash "${SCRIPT_DIR}/services/${svc}.sh" update
done
echo
bash "${SCRIPT_DIR}/liquibase-all-status.sh"
