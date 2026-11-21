#!/usr/bin/env bash
# Start all local shared infrastructure: Redis, Postgres, Kafka
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
LOCAL_DIR="${SCRIPT_DIR}"

COMPOSE_STACKS=(
  "${LOCAL_DIR}/Redis/docker-compose.yaml"
  "${LOCAL_DIR}/Postgres/docker-compose.yaml"
  "${LOCAL_DIR}/Kafka/docker-compose.yaml"
)

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
echo "==> All local stacks requested. Current status:"
"${SCRIPT_DIR}/all-status.sh"
