#!/usr/bin/env bash
set -euo pipefail
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
COMPOSE="${SCRIPT_DIR}/AIOps/docker-compose.yaml"
PROFILES=()
if [[ "${WITH_HOLMES:-0}" == "1" ]]; then
  PROFILES+=(--profile holmes)
fi
echo "==> Starting FX SOR AIOps stack"
docker compose -f "${COMPOSE}" "${PROFILES[@]}" up -d
"${SCRIPT_DIR}/aiops-status.sh"
