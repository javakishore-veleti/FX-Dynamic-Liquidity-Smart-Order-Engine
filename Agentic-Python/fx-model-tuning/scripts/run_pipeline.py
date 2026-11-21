#!/usr/bin/env python3
from __future__ import annotations

import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
if str(ROOT) not in sys.path:
    sys.path.insert(0, str(ROOT))

from fx_model_tuning.presentation.cli.pipeline_cli import PipelineCli


def main() -> int:
    return PipelineCli(ROOT).run()


if __name__ == "__main__":
    raise SystemExit(main())
