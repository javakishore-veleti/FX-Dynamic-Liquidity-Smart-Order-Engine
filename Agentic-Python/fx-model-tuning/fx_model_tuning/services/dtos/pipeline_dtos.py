from __future__ import annotations

from pathlib import Path
from typing import Any, Literal

from pydantic import BaseModel, ConfigDict, Field

UseCaseId = Literal["FT-01", "FT-02", "FT-03", "FT-04", "FT-05"]
TrainingProvider = Literal["BEDROCK", "HUGGINGFACE", "LOCAL_OLLAMA"]
PipelineStage = Literal["REDACTION", "DISTILLATION", "ADAPTER_TRAINING", "EVAL_REGISTRY"]


class ChatMessageDto(BaseModel):
    model_config = ConfigDict(frozen=True)

    role: Literal["system", "user", "assistant"]
    content: str


class TrainingRecordDto(BaseModel):
    """Bedrock / HF TRL compatible chat-completion JSONL row."""

    model_config = ConfigDict(frozen=True)

    messages: tuple[ChatMessageDto, ...]


class UseCaseConfigDto(BaseModel):
    model_config = ConfigDict(frozen=True)

    use_case_id: UseCaseId
    name: str
    technique: str
    teacher_model_bedrock: str | None
    student_model_hf: str
    student_model_local: str
    system_prompt: str
    schema_gate: str


class RedactionRequestDto(BaseModel):
    model_config = ConfigDict(frozen=True)

    use_case: UseCaseConfigDto
    raw_dataset_path: Path
    output_jsonl_path: Path


class RedactionResultDto(BaseModel):
    model_config = ConfigDict(frozen=True)

    records_in: int
    records_out: int
    redacted_fields: tuple[str, ...]
    output_jsonl_path: Path


class DistillationRequestDto(BaseModel):
    model_config = ConfigDict(frozen=True)

    use_case: UseCaseConfigDto
    provider: TrainingProvider
    redacted_jsonl_path: Path
    output_jsonl_path: Path


class DistillationResultDto(BaseModel):
    model_config = ConfigDict(frozen=True)

    teacher_model: str
    student_target: str
    records_generated: int
    output_jsonl_path: Path
    stubbed: bool


class AdapterTrainingRequestDto(BaseModel):
    model_config = ConfigDict(frozen=True)

    use_case: UseCaseConfigDto
    provider: TrainingProvider
    distilled_jsonl_path: Path
    output_dir: Path


class AdapterTrainingResultDto(BaseModel):
    model_config = ConfigDict(frozen=True)

    job_descriptor_path: Path
    technique: str
    provider: TrainingProvider
    export_formats: tuple[str, ...]
    stubbed: bool


class EvalGateRequestDto(BaseModel):
    model_config = ConfigDict(frozen=True)

    use_case: UseCaseConfigDto
    distilled_jsonl_path: Path
    registry_path: Path


class EvalGateResultDto(BaseModel):
    model_config = ConfigDict(frozen=True)

    passed: bool
    schema_validity_pct: float
    failed_examples: tuple[str, ...] = Field(default_factory=tuple)
    promoted_model_id: str | None
    registry_path: Path


class PipelineRunRequestDto(BaseModel):
    model_config = ConfigDict(frozen=True)

    use_case_id: UseCaseId
    provider: TrainingProvider
    workspace_root: Path


class PipelineRunResultDto(BaseModel):
    model_config = ConfigDict(frozen=True)

    use_case_id: UseCaseId
    stages: dict[str, Any]
    passed_eval_gate: bool
