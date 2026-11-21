#!/usr/bin/env bash
# Start all Angular portals (ng serve) in the background
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
# shellcheck source=lib-local-run.sh
source "${SCRIPT_DIR}/lib-local-run.sh"

ensure_run_dirs portals

# name|workspace|port
PORTALS=(
  "admin|Portals/fx-admin-portal|4201"
  "corporate|Portals/fx-corporate-portal|4202"
  "retail|Portals/fx-retail-portal|4203"
)

# npm install + first ng serve can be slow; override with FX_SOR_PORTAL_WAIT_SEC
WAIT_SEC="${FX_SOR_PORTAL_WAIT_SEC:-600}"

echo "==> Starting FX SOR portals (cwd=${ROOT_DIR})"
cd "${ROOT_DIR}"

# Ensure workspace deps once
if [[ ! -d "${ROOT_DIR}/node_modules" ]]; then
  echo "  --> npm install (root workspaces)"
  npm install
fi

WAIT_ENTRIES=()

for entry in "${PORTALS[@]}"; do
  IFS='|' read -r name workspace port <<<"${entry}"
  pid_file="${RUN_ROOT}/portals/pids/${name}.pid"
  log_file="${RUN_ROOT}/portals/logs/${name}.log"
  app_dir="${ROOT_DIR}/${workspace}"

  if [[ ! -d "${app_dir}" ]]; then
    echo "  [MISS] ${name} — ${workspace} not found"
    continue
  fi

  existing="$(read_pid "${pid_file}" || true)"
  if [[ -n "${existing}" ]] && is_pid_alive "${existing}" && port_listening "${port}"; then
    echo "  [SKIP] ${name} already up pid=${existing} port=${port}"
    continue
  fi
  if port_listening "${port}"; then
    echo "  [SKIP] ${name} port ${port} already in use"
    continue
  fi

  echo "  [START] ${name} → http://0.0.0.0:${port}/  (log: ${log_file})"
  (
    cd "${ROOT_DIR}"
    if command -v setsid >/dev/null 2>&1; then
      setsid npx --prefix "${app_dir}" ng serve --port "${port}" --host 0.0.0.0 >"${log_file}" 2>&1 &
      echo $! >"${pid_file}"
    else
      nohup npx --prefix "${app_dir}" ng serve --port "${port}" --host 0.0.0.0 >"${log_file}" 2>&1 &
      echo $! >"${pid_file}"
    fi
  )
  WAIT_ENTRIES+=("${name}|${port}|${pid_file}|${log_file}")
done

echo
if ((${#WAIT_ENTRIES[@]} > 0)); then
  set +e
  wait_for_services "${WAIT_SEC}" "${WAIT_ENTRIES[@]}"
  wait_rc=$?
  set -e
else
  wait_rc=0
fi

echo
"${SCRIPT_DIR}/portals-all-status.sh"
echo
echo "Tip: FX_SOR_PORTAL_WAIT_SEC=900 npm run local:portals:all:start   # longer wait (default 600s / 10m)"

exit "${wait_rc}"
