#!/usr/bin/env bash
# Stop local shared infrastructure and remove named volumes (Redis, Postgres, Kafka)
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
LOCAL_DIR="${SCRIPT_DIR}"

COMPOSE_STACKS=(
  "${LOCAL_DIR}/Kafka/docker-compose.yaml"
  "${LOCAL_DIR}/Postgres/docker-compose.yaml"
  "${LOCAL_DIR}/Redis/docker-compose.yaml"
)

echo "==> Stopping FX SOR local infrastructure (containers + volumes)"
for compose_file in "${COMPOSE_STACKS[@]}"; do
  if [[ ! -f "${compose_file}" ]]; then
    echo "WARN: missing compose file (skipping): ${compose_file}" >&2
    continue
  fi
  echo "--> down -v: ${compose_file}"
  docker compose -f "${compose_file}" down -v
done

echo
echo "==> All local stacks stopped. Current status:"
"${SCRIPT_DIR}/all-status.sh"
