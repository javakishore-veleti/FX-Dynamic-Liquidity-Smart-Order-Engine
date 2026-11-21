#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"
for uc in FT-01 FT-02 FT-03 FT-04 FT-05; do
  echo "==> $uc"
  python scripts/run_pipeline.py --use-case "$uc" --provider "${LLM_PROVIDER:-LOCAL_OLLAMA}"
done
