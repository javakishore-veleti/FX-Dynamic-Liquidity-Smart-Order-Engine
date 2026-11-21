#!/usr/bin/env bash
# Start all Middleware Spring Boot services in the background (H2 default)
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
# shellcheck source=lib-local-run.sh
source "${SCRIPT_DIR}/lib-local-run.sh"

ensure_run_dirs middleware

# name|maven-module|port
SERVICES=(
  "quote|Middleware/fx-quote-service|8081"
  "qualify|Middleware/fx-qualify-service|8082"
  "route-exec|Middleware/fx-route-exec-service|8083"
  "admin|Middleware/fx-admin-service|8084"
  "topology|Middleware/fx-topology-service|8085"
  "topology-mcp|Middleware/fx-topology-mcp-server|8091"
  "agent-orchestrator|Middleware/fx-agent-orchestrator-service|8092"
)

PROFILES="${FX_SOR_SPRING_PROFILES:-}"

echo "==> Starting FX SOR middleware services (cwd=${ROOT_DIR})"
if [[ -n "${PROFILES}" ]]; then
  echo "    spring profiles: ${PROFILES}"
fi

cd "${ROOT_DIR}"

for entry in "${SERVICES[@]}"; do
  IFS='|' read -r name module port <<<"${entry}"
  pid_file="${RUN_ROOT}/middleware/pids/${name}.pid"
  log_file="${RUN_ROOT}/middleware/logs/${name}.log"

  existing="$(read_pid "${pid_file}" || true)"
  if [[ -n "${existing}" ]] && is_pid_alive "${existing}"; then
    echo "  [SKIP] ${name} already running pid=${existing}"
    continue
  fi
  if port_listening "${port}"; then
    echo "  [SKIP] ${name} port ${port} already in use"
    continue
  fi

  echo "  [START] ${name} → :${port}  (log: ${log_file})"
  mvn_args=(-pl "${module}" spring-boot:run)
  if [[ -n "${PROFILES}" ]]; then
    mvn_args+=("-Dspring-boot.run.profiles=${PROFILES}")
  fi

  (
    cd "${ROOT_DIR}"
    # setsid so stop can signal the whole process group
    if command -v setsid >/dev/null 2>&1; then
      setsid mvn "${mvn_args[@]}" >"${log_file}" 2>&1 &
      echo $! >"${pid_file}"
    else
      nohup mvn "${mvn_args[@]}" >"${log_file}" 2>&1 &
      echo $! >"${pid_file}"
    fi
  )
done

echo
echo "==> Middleware start requested. Status:"
"${SCRIPT_DIR}/middleware-all-status.sh"
echo
echo "Tip: FX_SOR_SPRING_PROFILES=local-postgres,load-redis,load-kafka npm run local:middleware:all:start"
