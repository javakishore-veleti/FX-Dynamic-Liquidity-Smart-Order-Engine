from __future__ import annotations

from pathlib import Path

import yaml

from fx_model_tuning.services.dtos.pipeline_dtos import UseCaseConfigDto, UseCaseId


class UseCaseCatalog:
    def __init__(self, config_path: Path) -> None:
        self._config_path = config_path

    def get(self, use_case_id: UseCaseId) -> UseCaseConfigDto:
        raw = yaml.safe_load(self._config_path.read_text(encoding="utf-8"))
        entry = raw["use_cases"][use_case_id]
        return UseCaseConfigDto(
            use_case_id=use_case_id,
            name=entry["name"],
            technique=entry["technique"],
            teacher_model_bedrock=entry.get("teacher_model_bedrock"),
            student_model_hf=entry["student_model_hf"],
            student_model_local=entry["student_model_local"],
            system_prompt=entry["system_prompt"],
            schema_gate=entry["schema_gate"],
        )
