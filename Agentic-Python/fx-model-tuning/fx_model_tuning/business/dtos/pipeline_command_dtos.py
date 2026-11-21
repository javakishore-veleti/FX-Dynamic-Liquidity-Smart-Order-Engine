from __future__ import annotations

from pathlib import Path

from pydantic import BaseModel, ConfigDict

from fx_model_tuning.services.dtos.pipeline_dtos import TrainingProvider, UseCaseId


class RunFineTuningCommandDto(BaseModel):
    model_config = ConfigDict(frozen=True)

    use_case_id: UseCaseId
    provider: TrainingProvider
    workspace_root: Path
