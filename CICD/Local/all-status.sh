#!/usr/bin/env bash
# Show status of local shared infrastructure containers
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
LOCAL_DIR="${SCRIPT_DIR}"

COMPOSE_STACKS=(
  "Redis:${LOCAL_DIR}/Redis/docker-compose.yaml"
  "Postgres:${LOCAL_DIR}/Postgres/docker-compose.yaml"
  "Kafka:${LOCAL_DIR}/Kafka/docker-compose.yaml"
)

EXPECTED_CONTAINERS=(
  fx-sor-redis
  fx-sor-postgres
  fx-sor-zookeeper
  fx-sor-kafka
)

echo "==> Compose project status"
for entry in "${COMPOSE_STACKS[@]}"; do
  name="${entry%%:*}"
  compose_file="${entry#*:}"
  echo
  echo "--- ${name} (${compose_file})"
  if [[ ! -f "${compose_file}" ]]; then
    echo "  compose file missing"
    continue
  fi
  docker compose -f "${compose_file}" ps || true
done

echo
echo "==> Expected containers"
for c in "${EXPECTED_CONTAINERS[@]}"; do
  if docker ps --format '{{.Names}}' | grep -qx "${c}"; then
    status="$(docker inspect -f '{{.State.Status}}' "${c}" 2>/dev/null || echo unknown)"
    health="$(docker inspect -f '{{if .State.Health}}{{.State.Health.Status}}{{else}}n/a{{end}}' "${c}" 2>/dev/null || echo n/a)"
    echo "  [UP]   ${c}  status=${status}  health=${health}"
  elif docker ps -a --format '{{.Names}}' | grep -qx "${c}"; then
    status="$(docker inspect -f '{{.State.Status}}' "${c}" 2>/dev/null || echo unknown)"
    echo "  [DOWN] ${c}  status=${status}"
  else
    echo "  [ABSENT] ${c}"
  fi
done

echo
echo "Ports (defaults): Redis 6379 | Postgres 5432 | Kafka 9092 | Zookeeper 2181"
echo "Services default to H2 in-memory; use --spring.profiles.active=postgres,redis after infra is up."
