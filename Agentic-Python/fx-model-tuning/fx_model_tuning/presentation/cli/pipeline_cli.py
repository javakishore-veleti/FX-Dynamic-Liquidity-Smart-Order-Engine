from __future__ import annotations

import argparse
import json
from pathlib import Path

from fx_model_tuning.business.dtos.pipeline_command_dtos import RunFineTuningCommandDto
from fx_model_tuning.business.facades.tuning_pipeline_facade import TuningPipelineFacade
from fx_model_tuning.services.dtos.pipeline_dtos import TrainingProvider, UseCaseId
from fx_model_tuning.services.registry.use_case_catalog import UseCaseCatalog
from fx_model_tuning.services.stages.stage1_redaction import Stage1RedactionService
from fx_model_tuning.services.stages.stage2_distillation import Stage2DistillationService
from fx_model_tuning.services.stages.stage3_adapter_training import Stage3AdapterTrainingService
from fx_model_tuning.services.stages.stage4_eval_registry import Stage4EvalRegistryService


class PipelineCli:
    """Presentation adapter: argparse → frozen command DTO → facade."""

    def __init__(self, workspace_root: Path) -> None:
        self._workspace_root = workspace_root
        catalog = UseCaseCatalog(workspace_root / "configs" / "ft_use_cases.yaml")
        self._facade = TuningPipelineFacade(
            catalog=catalog,
            stage1=Stage1RedactionService(),
            stage2=Stage2DistillationService(),
            stage3=Stage3AdapterTrainingService(),
            stage4=Stage4EvalRegistryService(),
        )

    def run(self, argv: list[str] | None = None) -> int:
        parser = argparse.ArgumentParser(description="FX SOR fine-tuning & distillation pipeline")
        parser.add_argument(
            "--use-case",
            required=True,
            choices=["FT-01", "FT-02", "FT-03", "FT-04", "FT-05"],
        )
        parser.add_argument(
            "--provider",
            default="LOCAL_OLLAMA",
            choices=["BEDROCK", "HUGGINGFACE", "LOCAL_OLLAMA"],
        )
        args = parser.parse_args(argv)
        result = self._facade.run(
            RunFineTuningCommandDto(
                use_case_id=args.use_case,  # type: ignore[arg-type]
                provider=args.provider,  # type: ignore[arg-type]
                workspace_root=self._workspace_root,
            )
        )
        print(json.dumps(result.model_dump(mode="json"), indent=2, default=str))
        return 0 if result.passed_eval_gate else 2
