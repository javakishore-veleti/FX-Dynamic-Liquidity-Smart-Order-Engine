#!/usr/bin/env bash
set -euo pipefail
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
# shellcheck source=../lib-liquibase.sh
source "${SCRIPT_DIR}/../lib-liquibase.sh"
run_liquibase_for_service "Middleware/fx-route-exec-service" "fx_route_exec_db" "${1:-update}"
