from __future__ import annotations

import json
from pathlib import Path

from fx_model_tuning.services.dtos.pipeline_dtos import (
    DistillationRequestDto,
    DistillationResultDto,
    TrainingRecordDto,
)


class Stage2DistillationService:
    """Teacher→student gold-label generation (stubbed without cloud SDKs)."""

    def execute(self, request: DistillationRequestDto) -> DistillationResultDto:
        teacher = self._teacher_model(request)
        student = self._student_model(request)
        records = self._read_jsonl(request.redacted_jsonl_path)
        distilled: list[dict] = []
        for raw in records:
            # Offline stub: copy assistant as verified teacher output and tag provenance
            payload = dict(raw)
            messages = list(payload.get("messages", []))
            if messages:
                messages = [
                    *messages[:-1],
                    {
                        "role": "assistant",
                        "content": messages[-1].get("content", "{}"),
                    },
                ]
                payload["messages"] = messages
            payload["distillation"] = {
                "teacherModel": teacher,
                "studentTarget": student,
                "verified": True,
                "stubbed": True,
            }
            distilled.append(payload)

        request.output_jsonl_path.parent.mkdir(parents=True, exist_ok=True)
        with request.output_jsonl_path.open("w", encoding="utf-8") as handle:
            for row in distilled:
                handle.write(json.dumps(row) + "\n")

        return DistillationResultDto(
            teacher_model=teacher,
            student_target=student,
            records_generated=len(distilled),
            output_jsonl_path=request.output_jsonl_path,
            stubbed=True,
        )

    def _teacher_model(self, request: DistillationRequestDto) -> str:
        if request.provider == "BEDROCK":
            return request.use_case.teacher_model_bedrock or "bedrock-teacher"
        if request.provider == "HUGGINGFACE":
            return request.use_case.student_model_hf
        return request.use_case.student_model_local

    def _student_model(self, request: DistillationRequestDto) -> str:
        if request.provider == "BEDROCK":
            return "amazon.nova-micro-v1:0"
        if request.provider == "HUGGINGFACE":
            return request.use_case.student_model_hf
        return request.use_case.student_model_local

    def _read_jsonl(self, path: Path) -> list[dict]:
        if not path.exists():
            return []
        rows: list[dict] = []
        with path.open(encoding="utf-8") as handle:
            for line in handle:
                line = line.strip()
                if line:
                    rows.append(json.loads(line))
        return rows
