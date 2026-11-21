from __future__ import annotations

import json

from fx_model_tuning.services.dtos.pipeline_dtos import (
    AdapterTrainingRequestDto,
    AdapterTrainingResultDto,
)


class Stage3AdapterTrainingService:
    """Emit QLoRA/LoRA/DPO job descriptors + export format plan (offline stub)."""

    def execute(self, request: AdapterTrainingRequestDto) -> AdapterTrainingResultDto:
        request.output_dir.mkdir(parents=True, exist_ok=True)
        descriptor = {
            "useCaseId": request.use_case.use_case_id,
            "technique": request.use_case.technique,
            "provider": request.provider,
            "dataset": str(request.distilled_jsonl_path),
            "hyperparams": {
                "loraRank": 16,
                "loraAlpha": 32,
                "learningRate": 2e-4,
                "epochs": 3,
                "quantization": "nf4" if "QLORA" in request.use_case.technique else None,
            },
            "exports": self._exports(request),
            "status": "DESCRIBED_ONLY_STUB",
            "note": "Launch real Bedrock Custom Model / HF TRL / LlamaFactory jobs from this descriptor.",
        }
        path = request.output_dir / f"{request.use_case.use_case_id.lower()}-job.json"
        path.write_text(json.dumps(descriptor, indent=2), encoding="utf-8")
        return AdapterTrainingResultDto(
            job_descriptor_path=path,
            technique=request.use_case.technique,
            provider=request.provider,
            export_formats=self._exports(request),
            stubbed=True,
        )

    def _exports(self, request: AdapterTrainingRequestDto) -> tuple[str, ...]:
        if request.use_case.use_case_id == "FT-04":
            return ("ONNX",)
        if request.provider == "LOCAL_OLLAMA":
            return ("GGUF", "LoRA")
        if request.provider == "HUGGINGFACE":
            return ("PEFT_ADAPTER", "SAFETENSORS")
        return ("BEDROCK_CUSTOM_MODEL",)
