#!/usr/bin/env bash
set -euo pipefail
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
# shellcheck source=lib-local-run.sh
source "${SCRIPT_DIR}/lib-local-run.sh"
COMPOSE="${SCRIPT_DIR}/AIOps/docker-compose.yaml"
WAIT_SEC="${FX_SOR_DOCKER_WAIT_SEC:-2400}"

EXPECTED=(fx-sor-otel-collector fx-sor-prometheus fx-sor-grafana fx-sor-opensearch fx-sor-opensearch-dashboards)
if [[ "${WITH_HOLMES:-0}" == "1" ]]; then
  EXPECTED+=(fx-sor-holmes-stub)
fi

echo "==> Starting FX SOR AIOps stack"
if [[ "${WITH_HOLMES:-0}" == "1" ]]; then
  docker compose -f "${COMPOSE}" --profile holmes up -d
else
  docker compose -f "${COMPOSE}" up -d
fi

echo
set +e
wait_for_docker_containers "${WAIT_SEC}" "${EXPECTED[@]}"
wait_rc=$?
set -e

echo
"${SCRIPT_DIR}/aiops-status.sh"
exit "${wait_rc}"
