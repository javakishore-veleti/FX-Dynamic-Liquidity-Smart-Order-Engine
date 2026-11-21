#!/usr/bin/env bash
# Stop all local shared infrastructure: Redis, Postgres, Kafka
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
LOCAL_DIR="${SCRIPT_DIR}"

COMPOSE_STACKS=(
  "${LOCAL_DIR}/Kafka/docker-compose.yaml"
  "${LOCAL_DIR}/Postgres/docker-compose.yaml"
  "${LOCAL_DIR}/Redis/docker-compose.yaml"
)

echo "==> Stopping FX SOR local infrastructure"
for compose_file in "${COMPOSE_STACKS[@]}"; do
  if [[ ! -f "${compose_file}" ]]; then
    echo "WARN: missing compose file (skipping): ${compose_file}" >&2
    continue
  fi
  echo "--> down: ${compose_file}"
  docker compose -f "${compose_file}" down
done

echo
echo "==> All local stacks stopped. Current status:"
"${SCRIPT_DIR}/all-status.sh"
