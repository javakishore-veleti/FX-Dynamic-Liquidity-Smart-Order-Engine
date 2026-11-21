#!/usr/bin/env bash
set -euo pipefail
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
COMPOSE="${SCRIPT_DIR}/AIOps/docker-compose.yaml"

echo "==> Starting FX SOR AIOps stack"
if [[ "${WITH_HOLMES:-0}" == "1" ]]; then
  docker compose -f "${COMPOSE}" --profile holmes up -d
else
  docker compose -f "${COMPOSE}" up -d
fi
"${SCRIPT_DIR}/aiops-status.sh"
