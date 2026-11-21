from __future__ import annotations

from pathlib import Path

from fx_model_tuning.business.dtos.pipeline_command_dtos import RunFineTuningCommandDto
from fx_model_tuning.services.dtos.pipeline_dtos import (
    AdapterTrainingRequestDto,
    DistillationRequestDto,
    EvalGateRequestDto,
    PipelineRunResultDto,
    RedactionRequestDto,
)
from fx_model_tuning.services.registry.use_case_catalog import UseCaseCatalog
from fx_model_tuning.services.stages.stage1_redaction import Stage1RedactionService
from fx_model_tuning.services.stages.stage2_distillation import Stage2DistillationService
from fx_model_tuning.services.stages.stage3_adapter_training import Stage3AdapterTrainingService
from fx_model_tuning.services.stages.stage4_eval_registry import Stage4EvalRegistryService


class TuningPipelineFacade:
    """Closed-layer orchestration of FT-01…FT-05 offline stages."""

    def __init__(
        self,
        catalog: UseCaseCatalog,
        stage1: Stage1RedactionService,
        stage2: Stage2DistillationService,
        stage3: Stage3AdapterTrainingService,
        stage4: Stage4EvalRegistryService,
    ) -> None:
        self._catalog = catalog
        self._stage1 = stage1
        self._stage2 = stage2
        self._stage3 = stage3
        self._stage4 = stage4

    def run(self, command: RunFineTuningCommandDto) -> PipelineRunResultDto:
        use_case = self._catalog.get(command.use_case_id)
        root = command.workspace_root
        samples = root / "samples"
        raw_path = samples / "raw" / f"{command.use_case_id.lower()}.jsonl"
        redacted_path = samples / "redacted" / f"{command.use_case_id.lower()}.jsonl"
        distilled_path = samples / "distilled" / f"{command.use_case_id.lower()}.jsonl"
        adapters_dir = samples / "adapters" / command.use_case_id.lower()
        registry_path = samples / "eval" / "model_registry.json"

        redaction = self._stage1.execute(
            RedactionRequestDto(
                use_case=use_case,
                raw_dataset_path=raw_path,
                output_jsonl_path=redacted_path,
            )
        )
        distillation = self._stage2.execute(
            DistillationRequestDto(
                use_case=use_case,
                provider=command.provider,
                redacted_jsonl_path=redacted_path,
                output_jsonl_path=distilled_path,
            )
        )
        training = self._stage3.execute(
            AdapterTrainingRequestDto(
                use_case=use_case,
                provider=command.provider,
                distilled_jsonl_path=distilled_path,
                output_dir=adapters_dir,
            )
        )
        evaluation = self._stage4.execute(
            EvalGateRequestDto(
                use_case=use_case,
                distilled_jsonl_path=distilled_path,
                registry_path=registry_path,
            )
        )
        return PipelineRunResultDto(
            use_case_id=command.use_case_id,
            stages={
                "redaction": redaction.model_dump(mode="json"),
                "distillation": distillation.model_dump(mode="json"),
                "adapterTraining": training.model_dump(mode="json"),
                "evalRegistry": evaluation.model_dump(mode="json"),
            },
            passed_eval_gate=evaluation.passed,
        )
