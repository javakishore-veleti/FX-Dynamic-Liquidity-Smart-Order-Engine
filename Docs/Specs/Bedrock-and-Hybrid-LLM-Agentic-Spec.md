# Bedrock & Hybrid LLM Agentic Specification

**Document ID:** Bedrock-and-Hybrid-LLM-Agentic-Spec.md  
**Repository:** fx-dynamic-liquidity-smart-order-engine  
**Runtime services:** `Middleware/fx-agent-orchestrator-service` (Spring AI) · `Agentic-Python/fx-agent-orchestrator-service` (LangGraph)  
**Architecture:** Provider-agnostic `ModelGatewayStrategy` / `ChatModel` profile switching across **AWS Bedrock**, **Hugging Face Inference / TEI**, and **local Ollama / vLLM** via a single configuration toggle.

## 1. Executive intent

Power **10 institutional FX agentic use cases** without rewriting agent graph logic when switching cloud vs open-source vs laptop runtimes.

- **Staging / Prod:** AWS Bedrock (Claude 3.7 Sonnet / Nova Pro / Titan Embeddings) with Guardrails  
- **Open-source cluster:** Hugging Face Inference Endpoints / TEI  
- **Laptop offline:** Ollama / vLLM (zero cloud cost)

## 2. Ten agentic AI use cases

| # | Agentic use case | Target runtime | MCP tools & data sources | Bedrock model | Hugging Face / local swap-in |
|---|------------------|----------------|--------------------------|---------------|------------------------------|
| 1 | 3:00 AM Incident & Time-Travel RCA Agent | fx-agent-orchestrator-service (Python LangGraph + Spring AI) | `diff_topology_snapshots`, `query_iceberg_outlier_traces`, `get_ipc_edge_health` | Claude 3.7 Sonnet (extended thinking) | `Qwen/Qwen2.5-72B-Instruct` or local `qwen2.5:32b` |
| 2 | Blast-Radius & Pre-Change Guard Agent | fx-agent-orchestrator-service | `get_service_dependencies`, `calculate_blast_radius`, Argo Rollout diffs | Amazon Nova Pro | `meta-llama/Llama-3.3-70B-Instruct` or local `llama3.3:8b` |
| 3 | Backpressure & Kafka Lag Remediation Agent | fx-agent-orchestrator-service | `get_kafka_consumer_lag`, Prometheus ZGC metrics, K8s HPA scaler | Claude 3.5 Haiku | `mistralai/Mistral-Small-24B-Instruct` or local `mistral:7b` |
| 4 | Natural-Language Drools / DMN Policy Authoring Agent | fx-admin-service + orchestrator | Existing DMN tables → rule XML/JSON + shadow replay | Claude 3.7 Sonnet | `Qwen/Qwen2.5-Coder-32B-Instruct` or local `qwen2.5-coder:14b` |
| 5 | LP Toxicity & Last-Look Anomaly Detective Agent | fx-agent-orchestrator-service | `lp_telemetry_projections` + last-look failover events | Amazon Nova Pro + Bedrock Guardrails | `deepseek-ai/DeepSeek-R1-Distill-Qwen-32B` |
| 6 | MiFID II / FX Global Code Best-Execution Explainer | fx-route-exec-service / Corporate BFF | `GET /api/v1/fx/orders/{clientOrderId}/trace` | Claude 3.5 Haiku | `meta-llama/Llama-3.1-8B-Instruct` (vLLM) |
| 7 | Corporate Treasury Hedge & Block-Splitting Copilot | Portals/fx-corporate-portal BFF + LangGraph | `POST /orders/qualify`, `GET /credit/utilization/{clientId}` | Claude 3.7 Sonnet | `Qwen/Qwen2.5-32B-Instruct` |
| 8 | Conversational FX Regulatory & ADR Knowledge RAG Agent | fx-agent-orchestrator-service | Vector search over ADRs, FX Global Code, PRD-FX-SOR-001 | Titan Text Embeddings v2 + Claude 3.5 Haiku | `BAAI/bge-m3` (TEI) + Llama-3.1-8B |
| 9 | Synthetic Chaos & Skewed Load-Generator Agent | fx-admin-service + orchestrator | `trigger_synthetic_seed`, `trigger_skewed_load_sim` | Amazon Nova Lite | `microsoft/Phi-4` or local `phi4:14b` |
| 10 | Macro News & Central Bank Volatility Guard Agent | fx-quote-service / orchestrator | FOMC/NFP/ECB headlines → regime severity + spread multipliers | Claude 3.5 Haiku | FinBERT + `Qwen2.5-7B` |

### HTTP surface (dual-stack)

All agents expose **HTTP 202** under `/api/v1/agents/...` with `dryRun` default **true** and Maker-Checker `approvalToken` required for live mutations (ADR-032).

| # | Path | Agent type enum |
|---|------|-----------------|
| 1 | `POST /incident-rca/investigate` | `INCIDENT_RCA` |
| 2 | `POST /blast-radius/analyze` | `BLAST_RADIUS` |
| 3 | `POST /backpressure/assess` | `BACKPRESSURE` |
| 4 | `POST /policy-authoring/generate` | `POLICY_AUTHORING` |
| 5 | `POST /lp-toxicity/detect` | `LP_TOXICITY` |
| 6 | `POST /best-execution/explain` | `BEST_EXECUTION` |
| 7 | `POST /treasury-hedge/copilot` | `TREASURY_HEDGE` |
| 8 | `POST /regulatory-rag/ask` | `REGULATORY_RAG` |
| 9 | `POST /chaos-sim/execute` | `CHAOS_SIM` |
| 10 | `POST /macro-guard/assess` | `MACRO_GUARD` |

## 3. Provider-agnostic switchable model architecture

### Configuration toggle

```yaml
# application.yml (Java) / environment (Python)
LLM_PROVIDER=BEDROCK               # BEDROCK | HUGGINGFACE | LOCAL_OLLAMA
BEDROCK_REGION=us-east-1
BEDROCK_REASONING_MODEL=us.anthropic.claude-3-7-sonnet-20250219-v1:0
BEDROCK_FAST_MODEL=us.amazon.nova-lite-v1:0
BEDROCK_EMBEDDING_MODEL=amazon.titan-embed-text-v2:0

HF_ENDPOINT_URL=https://api-inference.huggingface.co
HF_REASONING_MODEL=Qwen/Qwen2.5-72B-Instruct
HF_EMBEDDING_MODEL=BAAI/bge-m3

LOCAL_OLLAMA_BASE_URL=http://localhost:11434
LOCAL_REASONING_MODEL=qwen2.5:14b
```

Java (`fx.llm.*`):

```yaml
fx:
  llm:
    provider: ${LLM_PROVIDER:LOCAL_OLLAMA}
    bedrock:
      region: ${BEDROCK_REGION:us-east-1}
      reasoning-model: ${BEDROCK_REASONING_MODEL:us.anthropic.claude-3-7-sonnet-20250219-v1:0}
      fast-model: ${BEDROCK_FAST_MODEL:us.amazon.nova-lite-v1:0}
      embedding-model: ${BEDROCK_EMBEDDING_MODEL:amazon.titan-embed-text-v2:0}
      guardrail-id: ${BEDROCK_GUARDRAIL_ID:}
    huggingface:
      endpoint-url: ${HF_ENDPOINT_URL:https://api-inference.huggingface.co}
      reasoning-model: ${HF_REASONING_MODEL:Qwen/Qwen2.5-72B-Instruct}
      embedding-model: ${HF_EMBEDDING_MODEL:BAAI/bge-m3}
    local-ollama:
      base-url: ${LOCAL_OLLAMA_BASE_URL:http://localhost:11434}
      reasoning-model: ${LOCAL_REASONING_MODEL:qwen2.5:14b}
```

### Python closed-layer gateway (`services/llm/`)

- `ModelProviderConfigDto` — frozen Pydantic DTO (zero primitive args across layer boundaries)  
- `ModelGatewayFactory.create_chat_model(config_dto)` — returns Bedrock / Hugging Face / Ollama chat client  
- `LlmReasoningService` — DTO-in / DTO-out structured JSON via schema enforcement  
- Optional deps: `langchain-aws`, `langchain-huggingface`, `langchain-ollama` — when absent, **StubChatModel** keeps local CI green

### Java Spring AI profile switching (`llm/`)

- `LlmProviderProperties` (`@ConfigurationProperties(prefix = "fx.llm")`)  
- `ChatModelGateway` strategy interface  
- Implementations: `BedrockChatModelGateway`, `HuggingFaceChatModelGateway`, `OllamaChatModelGateway`, `StubChatModelGateway` (default when SDKs not on classpath)  
- Agents call only `ChatModelGateway.complete(LlmCompletionRequestDto)` — no provider branching in workflows

## 4. Enterprise guardrails

1. **AWS Bedrock Guardrails & PII/spread redaction** — strip counterparty account IDs and raw wholesale LP spreads before prompts leave the VPC (ADR-026).  
2. **Deterministic JSON schema enforcement** — `with_structured_output(PydanticDto)` / Spring AI structured output so every agent returns typed DTOs into LangGraph state and `agent_run_log`.  
3. **Maker-Checker** — live mutations (`dryRun=false`) require `approvalToken`.  
4. **Audience isolation** — retail/corporate explainers never surface raw LP names or toxicity internals.

## 5. Model tier mapping

| Tier | Agents | Bedrock default | Local default |
|------|--------|-----------------|---------------|
| Reasoning | 1, 4, 7 | Claude 3.7 Sonnet | `qwen2.5:14b` |
| Fast / structured | 2, 5, 9 | Nova Pro / Nova Lite | `phi4:14b` / `llama3.3:8b` |
| High-volume plain English | 3, 6, 8, 10 | Claude 3.5 Haiku | `mistral:7b` / `llama3.1:8b` |
| Embeddings | 8 | Titan Embeddings v2 | `BAAI/bge-m3` via TEI |

## 6. Implementation checklist

- [x] Spec filed under `Docs/Specs/`  
- [x] Python `ModelGatewayFactory` + env toggle  
- [x] Java `fx.llm` properties + `ChatModelGateway`  
- [x] All 10 agent HTTP endpoints on dual-stack orchestrators  
- [ ] Production Bedrock IAM role + Guardrail ARN wiring  
- [ ] TEI embedding sidecar compose profile  
- [ ] Portal BFF copilot streaming SSE (corporate / admin)
