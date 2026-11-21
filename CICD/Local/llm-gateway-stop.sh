#!/usr/bin/env bash
set -euo pipefail
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
COMPOSE="${SCRIPT_DIR}/LLM-Gateway/docker-compose.yaml"
echo "==> Stopping fx-llm-gateway"
docker compose -f "${COMPOSE}" down
"${SCRIPT_DIR}/llm-gateway-status.sh"
