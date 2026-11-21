#!/usr/bin/env bash
set -euo pipefail
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
COMPOSE="${SCRIPT_DIR}/LLM-Gateway/docker-compose.yaml"
echo "==> Starting fx-llm-gateway (LiteLLM)"
docker compose -f "${COMPOSE}" up -d
"${SCRIPT_DIR}/llm-gateway-status.sh"
