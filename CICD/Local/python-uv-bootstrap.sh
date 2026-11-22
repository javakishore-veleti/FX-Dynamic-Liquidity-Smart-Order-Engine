#!/usr/bin/env bash
# Ensure a Python project is uv-initialized and deps are installed (uv sync).
# Usage: bash CICD/Local/python-uv-bootstrap.sh <project-dir> [name] [description]
set -euo pipefail

PROJECT_DIR="${1:?project dir required}"
PROJECT_NAME="${2:-}"
PROJECT_DESC="${3:-}"

if [[ ! -d "${PROJECT_DIR}" ]]; then
  echo "  [ERR] python project dir missing: ${PROJECT_DIR}" >&2
  exit 1
fi

if ! command -v uv >/dev/null 2>&1; then
  echo "  [ERR] uv not found on PATH. Install: https://docs.astral.sh/uv/" >&2
  exit 1
fi

cd "${PROJECT_DIR}"

if [[ ! -f pyproject.toml ]]; then
  name_flag=()
  desc_flag=()
  if [[ -n "${PROJECT_NAME}" ]]; then
    name_flag=(--name "${PROJECT_NAME}")
  fi
  if [[ -n "${PROJECT_DESC}" ]]; then
    desc_flag=(--description "${PROJECT_DESC}")
  fi
  echo "  [UV] init ${PROJECT_DIR}"
  # Existing source trees: package mode, no nested git, keep README if present.
  readme_flag=(--no-readme)
  [[ -f README.md ]] && readme_flag=()
  uv init --package --vcs none --author-from none "${name_flag[@]}" "${desc_flag[@]}" "${readme_flag[@]}" .
  # Prefer flat layout used by this monorepo (remove stub src/ if created).
  if [[ -d src ]]; then
    rm -rf src
  fi
fi

echo "  [UV] sync ${PROJECT_DIR}"
# uv has no `install` command — `uv sync` creates .venv and installs lock/deps.
uv sync

echo "  [UV] ready $(basename "${PROJECT_DIR}")"
