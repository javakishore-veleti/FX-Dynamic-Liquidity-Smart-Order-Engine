#!/usr/bin/env bash
set -euo pipefail
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
# shellcheck source=lib-local-run.sh
source "${SCRIPT_DIR}/lib-local-run.sh"
COMPOSE="${SCRIPT_DIR}/LLM-Gateway/docker-compose.yaml"
WAIT_SEC="${FX_SOR_DOCKER_WAIT_SEC:-2400}"

echo "==> Starting fx-llm-gateway (LiteLLM)"
docker compose -f "${COMPOSE}" up -d

echo
set +e
wait_for_docker_containers "${WAIT_SEC}" fx-sor-llm-gateway
wait_rc=$?
set -e

echo
"${SCRIPT_DIR}/llm-gateway-status.sh"
exit "${wait_rc}"
