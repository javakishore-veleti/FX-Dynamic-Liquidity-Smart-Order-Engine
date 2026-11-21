#!/usr/bin/env bash
set -euo pipefail
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
COMPOSE="${SCRIPT_DIR}/AIOps/docker-compose.yaml"
echo "==> Stopping FX SOR AIOps stack"
docker compose -f "${COMPOSE}" --profile holmes down
"${SCRIPT_DIR}/aiops-status.sh"
