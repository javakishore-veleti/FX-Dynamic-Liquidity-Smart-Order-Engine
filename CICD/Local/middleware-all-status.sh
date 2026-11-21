#!/usr/bin/env bash
# Status of Middleware Spring Boot local processes / ports
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
# shellcheck source=lib-local-run.sh
source "${SCRIPT_DIR}/lib-local-run.sh"

ensure_run_dirs middleware

SERVICES=(
  "quote|8081"
  "qualify|8082"
  "route-exec|8083"
  "admin|8084"
  "topology|8085"
  "topology-mcp|8091"
  "agent-orchestrator|8092"
)

echo "==> Middleware status"
for entry in "${SERVICES[@]}"; do
  IFS='|' read -r name port <<<"${entry}"
  print_status_row "${name}" "${port}" "${RUN_ROOT}/middleware/pids/${name}.pid"
done

echo
echo "Ports: quote 8081 | qualify 8082 | route-exec 8083 | admin 8084 | topology 8085 | mcp 8091 | orchestrator 8092"
echo "Logs:  ${RUN_ROOT}/middleware/logs/"
