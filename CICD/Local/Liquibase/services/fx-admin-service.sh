#!/usr/bin/env bash
set -euo pipefail
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
# shellcheck source=../lib-liquibase.sh
source "${SCRIPT_DIR}/../lib-liquibase.sh"
run_liquibase_for_service "Middleware/fx-admin-service" "dlp_sor_system_db" "${1:-update}"
