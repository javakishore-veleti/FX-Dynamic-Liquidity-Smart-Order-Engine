from __future__ import annotations

import json
from pathlib import Path

from fx_model_tuning.services.dtos.pipeline_dtos import (
    ChatMessageDto,
    RedactionRequestDto,
    RedactionResultDto,
    TrainingRecordDto,
)
from fx_model_tuning.services.redaction.adr026_redactor import Adr026Redactor


class Stage1RedactionService:
    """Extract & redact historical traces into Bedrock/HF-compatible JSONL."""

    def __init__(self, redactor: Adr026Redactor | None = None) -> None:
        self._redactor = redactor or Adr026Redactor()

    def execute(self, request: RedactionRequestDto) -> RedactionResultDto:
        raw_rows = self._load_raw(request.raw_dataset_path)
        records: list[TrainingRecordDto] = []
        fields: list[str] = []
        for row in raw_rows:
            cleaned, report = self._redactor.redact_mapping(row)
            fields.extend(report.fields_touched)
            user_content = cleaned.get("user") or cleaned.get("prompt") or json.dumps(cleaned)
            assistant_content = (
                cleaned.get("assistant")
                or cleaned.get("label")
                or cleaned.get("expected")
                or "{}"
            )
            records.append(
                TrainingRecordDto(
                    messages=(
                        ChatMessageDto(role="system", content=request.use_case.system_prompt.strip()),
                        ChatMessageDto(role="user", content=str(user_content)),
                        ChatMessageDto(role="assistant", content=str(assistant_content)),
                    )
                )
            )
        request.output_jsonl_path.parent.mkdir(parents=True, exist_ok=True)
        with request.output_jsonl_path.open("w", encoding="utf-8") as handle:
            for record in records:
                handle.write(record.model_dump_json() + "\n")
        return RedactionResultDto(
            records_in=len(raw_rows),
            records_out=len(records),
            redacted_fields=tuple(dict.fromkeys(fields)),
            output_jsonl_path=request.output_jsonl_path,
        )

    def _load_raw(self, path: Path) -> list[dict]:
        if not path.exists():
            return []
        rows: list[dict] = []
        with path.open(encoding="utf-8") as handle:
            for line in handle:
                line = line.strip()
                if not line:
                    continue
                rows.append(json.loads(line))
        return rows
