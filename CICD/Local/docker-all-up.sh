#!/usr/bin/env bash
# Start all local shared infrastructure: Redis, Postgres, Kafka
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
# shellcheck source=lib-local-run.sh
source "${SCRIPT_DIR}/lib-local-run.sh"
LOCAL_DIR="${SCRIPT_DIR}"

COMPOSE_STACKS=(
  "${LOCAL_DIR}/Redis/docker-compose.yaml"
  "${LOCAL_DIR}/Postgres/docker-compose.yaml"
  "${LOCAL_DIR}/Kafka/docker-compose.yaml"
)

EXPECTED_CONTAINERS=(
  fx-sor-redis
  fx-sor-postgres
  fx-sor-zookeeper
  fx-sor-kafka
)

# Image pulls can take a long time; override with FX_SOR_DOCKER_WAIT_SEC (default 40m)
WAIT_SEC="${FX_SOR_DOCKER_WAIT_SEC:-2400}"

echo "==> Starting FX SOR local infrastructure"
for compose_file in "${COMPOSE_STACKS[@]}"; do
  if [[ ! -f "${compose_file}" ]]; then
    echo "ERROR: missing compose file: ${compose_file}" >&2
    exit 1
  fi
  echo "--> up: ${compose_file}"
  docker compose -f "${compose_file}" up -d
done

echo
set +e
wait_for_docker_containers "${WAIT_SEC}" "${EXPECTED_CONTAINERS[@]}"
wait_rc=$?
set -e

echo
"${SCRIPT_DIR}/all-status.sh"
echo
echo "Tip: FX_SOR_DOCKER_WAIT_SEC=3600 npm run local:containers:all:start   # longer pull wait (default 2400s / 40m)"

exit "${wait_rc}"
