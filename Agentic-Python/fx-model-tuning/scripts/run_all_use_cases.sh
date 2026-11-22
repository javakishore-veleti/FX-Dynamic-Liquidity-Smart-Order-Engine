#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
REPO_ROOT="$(cd "${ROOT}/../.." && pwd)"
bash "${REPO_ROOT}/CICD/Local/python-uv-bootstrap.sh" "${ROOT}" "fx-model-tuning" "FX Model Fine-Tuning Pipeline"
cd "${ROOT}"
for uc in FT-01 FT-02 FT-03 FT-04 FT-05; do
  echo "==> $uc"
  uv run python scripts/run_pipeline.py --use-case "$uc" --provider "${LLM_PROVIDER:-LOCAL_OLLAMA}"
done
