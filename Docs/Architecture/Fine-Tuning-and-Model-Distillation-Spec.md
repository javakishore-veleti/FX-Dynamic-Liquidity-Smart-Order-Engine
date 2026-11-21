# **Fine-Tuning & Model Distillation Specification (AWS Bedrock, Hugging Face & Local Models)**

**Document ID:** Fine-Tuning-and-Model-Distillation-Spec.md  
**Repository:** fx-dynamic-liquidity-smart-order-engine  
**Base Java Namespace:** com.jk.apps.banking.fx.sor  
**Python Workspace:** Agentic-Python/fx-model-tuning (Strict Class-Based OOP \+ Pydantic v2 DTO-Only Architecture)  
**Security & Redaction Baseline:** ADR-026 (VPC Isolation, AWS KMS Encryption, Counterparty & Spread PII Redaction)

## **1\. Strategic Fine-Tuning Scope & Guardrails**

In a sub-10 ms institutional FX Smart Order Routing (SOR) platform, fine-tuning is **strictly prohibited** for memorizing volatile market state (live Bid/Ask quotes, bilateral credit balances, or Net Open Position limits, which reside in Caffeine L1 and Redis L2). Instead, fine-tuning is targeted exclusively at high-frequency, strict-schema, and domain-classification workloads where zero-shot prompting or RAG hits latency, accuracy, or token-cost ceilings.

| Use Case \# | Fine-Tuning Objective | Base Models (Bedrock vs. Hugging Face) | Platform Training Data Source | Tuning Technique | Target Production Outcome |
| :---- | :---- | :---- | :---- | :---- | :---- |
| **FT-01** | Natural-Language → Drools / DMN Decision Table Compiler | **Bedrock:** Amazon Nova Lite / Claude 3 Haiku **HF / Local:** Qwen/Qwen2.5-Coder-14B / 32B | 2,000+ historical eFX desk rule changes, DMN XML/JSON tables, and shadow-mode replay outcomes. | **Supervised Fine-Tuning (SFT) with QLoRA** | Eliminates XML/JSON syntax hallucinations and reduces few-shot prompt tokens by **\~85%** in fx-admin-service. |
| **FT-02** | 3:00 AM SRE & Service Topology Root-Cause Reasoning | **Bedrock:** Amazon Nova Pro / Lite **HF / Local:** Qwen/Qwen2.5-32B / DeepSeek-R1-Distill-Qwen-32B | Historical Iceberg outlier traces (\> 10 ms), Cilium eBPF graph diffs, and senior SRE post-mortem RCA reports. | **SFT \+ Direct Preference Optimization (DPO)** | Trains fast, compact models to follow multi-hop diagnostic playbooks (Quote → Qualify → LP\_VEN\_X). |
| **FT-03** | MiFID II / FX Global Code Best-Execution Explainer | **Bedrock:** Amazon Nova Micro (Distilled) **HF / Local:** meta-llama/Llama-3.1-8B-Instruct | 50,000+ fx.exec.order-routed-provenance.v1 Avro records paired with compliant corporate TCA summaries. | **Teacher-Student Knowledge Distillation** (Claude 3.7 Sonnet → Nova Micro / Llama-8B) | Cuts corporate TCA narration latency by **4x** and token costs by **\~80%** with zero regulatory phrasing drift. |
| **FT-04** | Sub-Millisecond Macro News & Volatility Regime Classifier | **HF / Local ONNX:** ProsusAI/finbert or BAAI/bge-large-en-v1.5 | Historical FOMC, NFP, ECB, and BoJ wire headlines labeled with realized EUR/USD & USD/JPY spread regimes. | **Encoder Sequence Classification (Mode 2\)** | Classifies breaking headlines into NORMAL, ELEVATED, or EXTREME\_SPIKE in **\< 5 ms** on CPU/ONNX without LLM latency. |
| **FT-05** | Synthetic Chaos & 28-Pair Edge-Case Scenario Generator | **Bedrock:** Amazon Nova Lite **HF / Local:** microsoft/Phi-4 (14B) | dlp\_sor\_system\_db.wf\_exec\_log runs \+ 28-pair edge-case schemas (USD/INR NDF cutoffs, 96% credit utilization). | **SFT with LoRA** | Guarantees 100% valid JSON payloads for POST /api/v1/admin/synthetic/datasets/generate. |

## **2\. Platform-by-Platform Fine-Tuning Execution Matrix**

| Capability Dimension | AWS Bedrock Custom Models | Hugging Face (TRL \+ PEFT \+ Unsloth) | Local / On-Premises (LlamaFactory / Axolotl) |
| :---- | :---- | :---- | :---- |
| **Supported Methods** | Supervised Fine-Tuning (SFT), Continued Pre-Training (CPT), Model Distillation (Teacher → Student), Reinforcement Fine-Tuning (RFT). | LoRA / QLoRA (PEFT), DPO / GRPO / PPO (TRL), Full-Parameter SFT (FSDP), Encoder Sequence Classification. | 4-bit NF4 QLoRA on workstation GPUs; GGUF, AWQ, and ONNX export for local inference runtimes. |
| **Supported Model Families** | Amazon Nova (Micro, Lite, Pro), Anthropic Claude 3 Haiku, Meta Llama 3.1/3.3, Cohere Command R, Amazon Titan. | Qwen 2.5 / Qwen 3, Llama 3.3, Mistral, DeepSeek-R1-Distill, Phi-4, FinBERT, BGE-M3. | Any open-weights architecture (Qwen2.5-Coder, Llama-3.3, Phi-4, FinBERT). |
| **Banking Security & VPC Isolation (ADR-026)** | Training datasets stay inside private **AWS S3 / VPC** encrypted with customer-managed **AWS KMS** keys; never shared with third-party providers. | Private Hugging Face Enterprise Hub / dedicated VPC Inference Endpoints with SOC2 isolation. | **100% Air-Gapped.** Zero external network traffic; ideal for sensitive dealing-desk logs and proprietary Drools rules. |
| **Runtime Integration** | Deployed as Bedrock Custom Model / Provisioned Throughput and invoked via BedrockModelGateway. | Deployed to HF Inference Endpoints or SageMaker and invoked via HuggingFaceModelGateway. | Exported to GGUF (Ollama) or ONNX (ONNX Runtime) and invoked via LocalOllamaModelGateway. |

## **3\. End-to-End Distillation & Training Pipeline (Agentic-Python/fx-model-tuning)**

Aligned with our strict OOP class-based and Pydantic v2 DTO-only engineering standards, fine-tuning workflows are organized as a standalone offline batch pipeline that never impacts live trading pods:

> * **Stage 1 — Anonymized Dataset Extraction & Redaction:** Scheduled micro-batch jobs extract historical traces from Apache Iceberg and dlp\_sor\_system\_db.wf\_exec\_log, stripping counterparty account IDs and raw wholesale LP spreads (enforcing ADR-026) before writing JSONL datasets.  
> * **Stage 2 — Teacher-Student Knowledge Distillation:** High-capacity frontier models (**Claude 3.7 Sonnet** on AWS Bedrock) generate verified reasoning chains and DMN rule outputs validated against unit tests, creating gold-standard distillation datasets for smaller student models (**Amazon Nova Micro** and **Qwen2.5-Coder-14B**).  
> * **Stage 3 — Adapter Training & Quantized Export:** Runs QLoRA SFT/DPO jobs on AWS Bedrock Custom Model jobs or GPU nodes via Hugging Face TRL, exporting LoRA adapters and quantized GGUF/ONNX artifacts.  
> * **Stage 4 — Automated Evaluation Gate & Registry Promotion:** Every candidate model must pass a **100% JSON/DMN schema validity gate** and an offline latency/accuracy benchmark before updating ModelProviderConfigDto in fx-agent-orchestrator-service.

## **4\. Standardized JSONL Training Record Format**

All SFT and distillation datasets adhere to a strict chat-completion JSONL schema compatible with both AWS Bedrock fine-tuning jobs and Hugging Face TRL SFTTrainer:  
`{"messages": [`  
  `{"role": "system", "content": "You are an institutional eFX DMN policy compiler. Output valid DMN decision JSON only."},`  
  `{"role": "user", "content": "During NY_ROLLOVER_5PM, penalize any LP whose last_look_reject_rate exceeds 5% by 25 score points on G10 Majors."},`  
  `{"role": "assistant", "content": "{\"ruleId\":\"RULE_NY_ROLLOVER_LL_GUARD\",\"sessionRegime\":\"NY_ROLLOVER_5PM\",\"tier\":\"G10_MAJORS\",\"condition\":\"lastLookRejectRate > 0.05\",\"scorePenalty\":25,\"requiresMakerChecker\":true}"}`  
`]}`