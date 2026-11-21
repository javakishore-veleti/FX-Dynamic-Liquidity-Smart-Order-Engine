#!/usr/bin/env bash
set -euo pipefail
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
COMPOSE="${SCRIPT_DIR}/LLM-Gateway/docker-compose.yaml"
echo "==> LLM Gateway status"
docker compose -f "${COMPOSE}" ps || true
if docker ps --format '{{.Names}}' | grep -qx fx-sor-llm-gateway; then
  echo "  [UP] fx-sor-llm-gateway  http://localhost:4000  (OpenAI /v1/chat/completions)"
else
  echo "  [DOWN] fx-sor-llm-gateway"
fi
