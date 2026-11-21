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
