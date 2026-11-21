#!/usr/bin/env bash
# Show Liquibase status for every middleware service DB
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

echo "==> External Liquibase STATUS"
for svc in "${SERVICES[@]}"; do
  echo
  bash "${SCRIPT_DIR}/services/${svc}.sh" status || echo "  [WARN] status failed for ${svc}"
done
