# fx-model-tuning

Offline fine-tuning & model distillation pipeline for the FX Dynamic Liquidity & SOR platform.

**Spec:** `Docs/Architecture/Fine-Tuning-and-Model-Distillation-Spec.md`  
**Architecture:** Strict class-based OOP + Pydantic v2 DTO-only (closed layers)  
**Security:** ADR-026 redaction — never fine-tune on live Bid/Ask, credit balances, or NOP

## Use cases (FT-01 … FT-05)

| ID | Objective | Technique |
|----|-----------|-----------|
| FT-01 | NL → Drools/DMN compiler | SFT + QLoRA |
| FT-02 | SRE topology RCA reasoning | SFT + DPO |
| FT-03 | MiFID II / FX Global Code TCA explainer | Teacher→Student distillation |
| FT-04 | Macro news regime classifier | Encoder classification (ONNX) |
| FT-05 | Synthetic chaos scenario generator | SFT + LoRA |

## Pipeline stages

1. **Redaction** — anonymize Iceberg / `wf_exec_log` extracts → JSONL  
2. **Distillation** — teacher (Bedrock Claude) → student gold labels  
3. **Adapter training** — QLoRA/LoRA/DPO job descriptors (Bedrock / HF TRL / local)  
4. **Eval gate & registry** — 100% JSON/DMN validity before promoting `ModelProviderConfigDto`

## Run (local stub — no GPU/cloud required)

```bash
# from repo root (preferred) — start scripts run uv init (if needed) + uv sync
npm run local:model-tuning -- --use-case FT-01 --provider LOCAL_OLLAMA
npm run local:model-tuning:all

# or directly with uv
cd Agentic-Python/fx-model-tuning
uv sync
uv run python scripts/run_pipeline.py --use-case FT-01 --provider LOCAL_OLLAMA
```

Outputs land under `samples/` (gitignored large artifacts; tiny seeds committed).

## Provider toggle

```bash
export LLM_PROVIDER=BEDROCK|HUGGINGFACE|LOCAL_OLLAMA
```

Same contract as `fx-agent-orchestrator-service` hybrid gateway.
