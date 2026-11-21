#!/usr/bin/env bash
# Shared helpers for per-service external Liquibase runs
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/../../.." && pwd)"
LIQUIBASE_IMAGE="${LIQUIBASE_IMAGE:-liquibase/liquibase:4.29}"
PG_HOST="${POSTGRES_HOST:-localhost}"
PG_PORT="${POSTGRES_PORT:-5432}"
PG_USER="${POSTGRES_USER:-fx_sor}"
PG_PASSWORD="${POSTGRES_PASSWORD:-fx_sor}"

run_liquibase_for_service() {
  local service_dir="$1"
  local db_name="$2"
  local command="${3:-update}"

  local changelog_dir="${ROOT_DIR}/${service_dir}/src/main/resources/db/changelog"
  if [[ ! -f "${changelog_dir}/db.changelog-master.yaml" ]]; then
    echo "ERROR: missing changelog at ${changelog_dir}/db.changelog-master.yaml" >&2
    return 1
  fi

  local url="jdbc:postgresql://${PG_HOST}:${PG_PORT}/${db_name}"
  echo "==> Liquibase ${command}: ${service_dir} → ${db_name}"
  docker run --rm \
    --add-host=host.docker.internal:host-gateway \
    -e LIQUIBASE_COMMAND_URL="${url/localhost/host.docker.internal}" \
    -e LIQUIBASE_COMMAND_USERNAME="${PG_USER}" \
    -e LIQUIBASE_COMMAND_PASSWORD="${PG_PASSWORD}" \
    -v "${changelog_dir}:/liquibase/changelog:ro" \
    "${LIQUIBASE_IMAGE}" \
    --changelog-file=db.changelog-master.yaml \
    --search-path=/liquibase/changelog \
    --url="${url/localhost/host.docker.internal}" \
    --username="${PG_USER}" \
    --password="${PG_PASSWORD}" \
    "${command}"
}
