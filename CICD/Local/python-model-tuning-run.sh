#!/usr/bin/env bash
set -euo pipefail
ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
PROJ="${ROOT_DIR}/Agentic-Python/fx-model-tuning"
bash "${ROOT_DIR}/CICD/Local/python-uv-bootstrap.sh" "${PROJ}" "fx-model-tuning" "FX Model Fine-Tuning Pipeline"
cd "${PROJ}"
exec uv run python scripts/run_pipeline.py "$@"
