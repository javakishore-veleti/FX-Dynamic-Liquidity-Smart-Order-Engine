from __future__ import annotations

import json
from pathlib import Path

from fx_model_tuning.services.dtos.pipeline_dtos import EvalGateRequestDto, EvalGateResultDto


class Stage4EvalRegistryService:
    """100% JSON/DMN schema validity gate before ModelProviderConfigDto promotion."""

    def execute(self, request: EvalGateRequestDto) -> EvalGateResultDto:
        rows = self._read(request.distilled_jsonl_path)
        failures: list[str] = []
        ok = 0
        for idx, row in enumerate(rows):
            messages = row.get("messages") or []
            assistant = next((m.get("content") for m in messages if m.get("role") == "assistant"), "")
            if self._passes_schema(request.use_case.schema_gate, str(assistant)):
                ok += 1
            else:
                failures.append(f"row-{idx}")
        total = max(len(rows), 1)
        pct = (ok / total) * 100.0
        passed = pct >= 100.0 and len(rows) > 0
        promoted = None
        if passed:
            promoted = request.use_case.student_model_local
            self._promote(request.registry_path, request.use_case.use_case_id, promoted)
        return EvalGateResultDto(
            passed=passed,
            schema_validity_pct=pct,
            failed_examples=tuple(failures[:10]),
            promoted_model_id=promoted,
            registry_path=request.registry_path,
        )

    def _passes_schema(self, gate: str, content: str) -> bool:
        if gate == "regime_label":
            return content.strip().upper() in {"NORMAL", "ELEVATED", "EXTREME_SPIKE"}
        try:
            payload = json.loads(content)
        except json.JSONDecodeError:
            return False
        if not isinstance(payload, dict):
            return False
        if gate == "dmn_decision_json":
            return {"ruleId", "condition", "scorePenalty"} <= payload.keys()
        if gate == "rca_playbook_json":
            return "causalChain" in payload or "steps" in payload
        if gate == "tca_summary_json":
            return "summary" in payload or "plainEnglish" in payload
        if gate == "synthetic_generate_json":
            return "numBatches" in payload or "datasets" in payload or "batchSize" in payload
        return True

    def _promote(self, registry_path: Path, use_case_id: str, model_id: str) -> None:
        registry_path.parent.mkdir(parents=True, exist_ok=True)
        registry: dict = {}
        if registry_path.exists():
            registry = json.loads(registry_path.read_text(encoding="utf-8"))
        registry[use_case_id] = {
            "promotedModelId": model_id,
            "status": "PROMOTED",
            "updates": "ModelProviderConfigDto in fx-agent-orchestrator-service",
        }
        registry_path.write_text(json.dumps(registry, indent=2), encoding="utf-8")

    def _read(self, path: Path) -> list[dict]:
        if not path.exists():
            return []
        rows: list[dict] = []
        with path.open(encoding="utf-8") as handle:
            for line in handle:
                line = line.strip()
                if line:
                    rows.append(json.loads(line))
        return rows
