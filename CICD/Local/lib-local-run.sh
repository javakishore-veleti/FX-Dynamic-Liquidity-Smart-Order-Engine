#!/usr/bin/env bash
# Shared helpers for local middleware / portal process management
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
RUN_ROOT="${ROOT_DIR}/.local-run"

ensure_run_dirs() {
  local kind="$1"
  mkdir -p "${RUN_ROOT}/${kind}/pids" "${RUN_ROOT}/${kind}/logs"
}

is_pid_alive() {
  local pid="$1"
  [[ -n "${pid}" ]] && kill -0 "${pid}" 2>/dev/null
}

port_listening() {
  local port="$1"
  if command -v lsof >/dev/null 2>&1; then
    lsof -nP -iTCP:"${port}" -sTCP:LISTEN >/dev/null 2>&1
    return $?
  fi
  if command -v nc >/dev/null 2>&1; then
    nc -z 127.0.0.1 "${port}" >/dev/null 2>&1
    return $?
  fi
  return 1
}

read_pid() {
  local pid_file="$1"
  if [[ -f "${pid_file}" ]]; then
    tr -d '[:space:]' < "${pid_file}"
  fi
}

stop_pid_file() {
  local name="$1"
  local pid_file="$2"
  local pid
  pid="$(read_pid "${pid_file}" || true)"
  if [[ -z "${pid}" ]]; then
    echo "  [SKIP] ${name} (no pid file)"
    return 0
  fi
  if is_pid_alive "${pid}"; then
    echo "  [STOP] ${name} pid=${pid}"
    # Kill process group if started with setsid; fallback to pid
    kill -TERM "-${pid}" 2>/dev/null || kill -TERM "${pid}" 2>/dev/null || true
    for _ in 1 2 3 4 5 6 7 8 9 10; do
      if ! is_pid_alive "${pid}"; then
        break
      fi
      sleep 0.5
    done
    if is_pid_alive "${pid}"; then
      kill -KILL "-${pid}" 2>/dev/null || kill -KILL "${pid}" 2>/dev/null || true
    fi
  else
    echo "  [GONE] ${name} stale pid=${pid}"
  fi
  rm -f "${pid_file}"
}

print_status_row() {
  local name="$1"
  local port="$2"
  local pid_file="$3"
  local pid
  pid="$(read_pid "${pid_file}" || true)"
  local alive="no"
  local listen="no"
  if [[ -n "${pid}" ]] && is_pid_alive "${pid}"; then
    alive="yes"
  fi
  if port_listening "${port}"; then
    listen="yes"
  fi
  if [[ "${alive}" == "yes" && "${listen}" == "yes" ]]; then
    echo "  [UP]     ${name}  port=${port}  pid=${pid}"
  elif [[ "${listen}" == "yes" ]]; then
    echo "  [LISTEN] ${name}  port=${port}  pid=${pid:-n/a} (port in use; pid file may be stale)"
  elif [[ "${alive}" == "yes" ]]; then
    echo "  [START]  ${name}  port=${port}  pid=${pid} (process up; port not listening yet)"
  else
    echo "  [DOWN]   ${name}  port=${port}"
  fi
}

# Poll until port listens or timeout. Returns 0 when ready, 1 on timeout/failure.
# Exits as soon as the port is listening — does not wait out the full timeout.
wait_for_port() {
  local name="$1"
  local port="$2"
  local pid_file="$3"
  local log_file="${4:-}"
  local timeout_sec="${5:-${FX_SOR_START_WAIT_SEC:-120}}"
  local interval_sec=2
  local elapsed=0
  local pid
  pid="$(read_pid "${pid_file}" || true)"

  while (( elapsed < timeout_sec )); do
    if port_listening "${port}"; then
      echo "  [UP]     ${name}  port=${port}  pid=${pid:-n/a}  ready=${elapsed}s"
      return 0
    fi
    if [[ -n "${pid}" ]] && ! is_pid_alive "${pid}"; then
      echo "  [FAIL]   ${name}  port=${port}  process exited after ${elapsed}s"
      if [[ -n "${log_file}" && -f "${log_file}" ]]; then
        echo "           last log lines (${log_file}):"
        tail -n 12 "${log_file}" | sed 's/^/           /'
      fi
      return 1
    fi
    if [[ -n "${log_file}" && -f "${log_file}" ]] && grep -qE 'BUILD FAILURE|APPLICATION FAILED TO START|Error:|EADDRINUSE|Cannot find module' "${log_file}" 2>/dev/null; then
      # Avoid false positives while Angular is still compiling (Error: often appears mid-build noise)
      if grep -qE 'BUILD FAILURE|APPLICATION FAILED TO START|EADDRINUSE|Cannot find module|Ng build failed|Failed to compile' "${log_file}" 2>/dev/null; then
        echo "  [FAIL]   ${name}  port=${port}  build/startup failure after ${elapsed}s"
        tail -n 12 "${log_file}" | sed 's/^/           /'
        return 1
      fi
    fi
    sleep "${interval_sec}"
    elapsed=$((elapsed + interval_sec))
  done

  echo "  [TIMEOUT] ${name}  port=${port}  not listening after ${timeout_sec}s"
  if [[ -n "${log_file}" && -f "${log_file}" ]]; then
    tail -n 12 "${log_file}" | sed 's/^/           /'
  fi
  return 1
}

# Wait for a list of "name|port|pid_file|log_file" entries. Returns count of failures.
wait_for_services() {
  local timeout_sec="${1:-${FX_SOR_START_WAIT_SEC:-120}}"
  shift
  local failures=0
  local entry name port pid_file log_file
  echo "==> Waiting for readiness (max ${timeout_sec}s each; returns as soon as ready)"
  for entry in "$@"; do
    IFS='|' read -r name port pid_file log_file <<<"${entry}"
    if ! wait_for_port "${name}" "${port}" "${pid_file}" "${log_file}" "${timeout_sec}"; then
      failures=$((failures + 1))
    fi
  done
  return "${failures}"
}

# Docker: container is ready when running, and healthy when a healthcheck exists.
docker_container_ready() {
  local name="$1"
  local status health
  status="$(docker inspect -f '{{.State.Status}}' "${name}" 2>/dev/null || echo absent)"
  [[ "${status}" == "running" ]] || return 1
  health="$(docker inspect -f '{{if .State.Health}}{{.State.Health.Status}}{{else}}none{{end}}' "${name}" 2>/dev/null || echo none)"
  if [[ "${health}" == "none" || "${health}" == "healthy" ]]; then
    return 0
  fi
  return 1
}

# Poll docker containers until ready or timeout. Returns as soon as all are ready.
# names are plain container names. Default timeout 40m (image pulls).
wait_for_docker_containers() {
  local timeout_sec="${1:-${FX_SOR_DOCKER_WAIT_SEC:-2400}}"
  shift
  local interval_sec=5
  local elapsed=0
  local name pending

  echo "==> Waiting for Docker readiness (max ${timeout_sec}s; returns as soon as all ready)"
  while (( elapsed < timeout_sec )); do
    pending=()
    for name in "$@"; do
      if ! docker_container_ready "${name}"; then
        pending+=("${name}")
      fi
    done
    if ((${#pending[@]} == 0)); then
      echo "  [UP]     all containers ready  elapsed=${elapsed}s"
      for name in "$@"; do
        local status health
        status="$(docker inspect -f '{{.State.Status}}' "${name}" 2>/dev/null || echo absent)"
        health="$(docker inspect -f '{{if .State.Health}}{{.State.Health.Status}}{{else}}n/a{{end}}' "${name}" 2>/dev/null || echo n/a)"
        echo "  [UP]     ${name}  status=${status}  health=${health}"
      done
      return 0
    fi
    if (( elapsed % 30 == 0 )); then
      echo "  ... still waiting (${elapsed}s): ${pending[*]}"
    fi
    sleep "${interval_sec}"
    elapsed=$((elapsed + interval_sec))
  done

  echo "  [TIMEOUT] containers not ready after ${timeout_sec}s"
  for name in "$@"; do
    if docker_container_ready "${name}"; then
      echo "  [UP]     ${name}"
    else
      local status health
      status="$(docker inspect -f '{{.State.Status}}' "${name}" 2>/dev/null || echo absent)"
      health="$(docker inspect -f '{{if .State.Health}}{{.State.Health.Status}}{{else}}n/a{{end}}' "${name}" 2>/dev/null || echo n/a)"
      echo "  [DOWN]   ${name}  status=${status}  health=${health}"
    fi
  done
  return 1
}
