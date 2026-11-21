# LLM Router, Agent-to-Agent (A2A) & Local AIOps Specification

**Document ID:** LLM-Router-A2A-and-Local-AIOps-Spec.md  
**Repository:** fx-dynamic-liquidity-smart-order-engine  
**Related:** Bedrock-and-Hybrid-LLM-Agentic-Spec.md · Fine-Tuning-and-Model-Distillation-Spec.md · Observability-SRE-Dashboards-and-Eventing-Spec.md  
**Local paths:** `CICD/Local/LLM-Gateway/` · `CICD/Local/AIOps/` · `Docs/Architecture/a2a/`

## 1. Executive recommendation

| Concern | Choice | Why |
|---------|--------|-----|
| Agnostic LLM router | **LiteLLM Proxy** as `fx-llm-gateway` sidecar | Single OpenAI-compatible `/v1/chat/completions` + `/v1/embeddings` for Spring AI **and** Python LangGraph; native Bedrock / Hugging Face / Ollama with retries, fallbacks, spend tracking |
| Cross-service agent comms | **Google Agent2Agent (A2A)** over JSON-RPC/HTTP(+SSE) | RCA → Blast-Radius → Admin Remediation handoffs without proprietary bus; AgentCards at `/.well-known/agent.json` |
| Agent→tool | **MCP** (existing `fx-topology-mcp-server`) | Deterministic topology/admin tools |
| Local AIOps | OTel Collector + Prometheus + Grafana + OpenSearch + HolmesGPT | Laptop-parity Netflix-style observability; agents diagnose via same LLM gateway |

**Rejected as sole router:** Bedrock Intelligent Prompt Routing (no offline Ollama/HF); in-process Spring AI / LangChain fallbacks alone (duplicates policy across Java & Python).

Gateway URL (compose network): `http://fx-llm-gateway:4000`  
Host laptop: `http://localhost:4000`

## 2. LiteLLM Proxy (polyglot router)

### Why LiteLLM for this monorepo

1. **One protocol** — `spring-ai-openai` and LangChain `ChatOpenAI` both speak OpenAI wire format.  
2. **Fallback chains** — Bedrock → Hugging Face → Ollama without code changes.  
3. **Ops controls** — rate limits, virtual keys, token budgets in one place.  
4. **ADR-026** — PII/spread redaction can be applied at the gateway edge before cloud egress.

### Model tiers

| Tier alias | Agents | Bedrock | HF / Local fallback |
|------------|--------|---------|---------------------|
| `fx-reasoning-tier` | Incident RCA, Drools/DMN Policy, Treasury Hedge | Claude 3.7 Sonnet | Qwen2.5-72B / `qwen2.5:14b` |
| `fx-fast-triage-tier` | Backpressure, TCA Explainer, Chaos, Macro Guard | Nova Lite / Haiku | Phi-4 / Mistral / FinBERT+Llama-8B |
| `fx-graph-tier` | Blast-Radius | Nova Pro | Llama-3.3-70B / `llama3.3:8b` |
| `fx-embedding-tier` | Regulatory RAG | Titan Embed v2 | BGE-M3 (TEI) |

### Config artifact

`CICD/Local/LLM-Gateway/litellm_config.yaml` — multi-entry `model_list` with shared `model_name` for automatic fallback order.

## 3. Model selection rationale (summary)

| Workflow | Model | Why |
|----------|-------|-----|
| 3:00 AM Incident RCA | Claude 3.7 Sonnet / Qwen2.5-72B | Multi-hop causal reasoning over eBPF diffs + Iceberg outliers |
| NL Drools/DMN Policy | Claude 3.7 / Qwen2.5-Coder | Strict XML/JSON decision tables; low syntax drift |
| Blast-Radius Guard | Nova Pro / Llama-3.3 | Large-context graph traversal, cost/speed balance |
| Backpressure Remediation | Haiku / Nova Lite / Mistral | Sub-second tool calling before queue overflow |
| TCA Explainer & Macro Guard | Nova Micro + Titan / FinBERT + Llama-8B | High volume, low margin; encoder &lt; 10 ms for regimes |

## 4. Agent-to-Agent (A2A) & standard libraries

**Yes — A2A is required.** Example: Incident RCA detects toxic LP → delegates to Blast-Radius (corporate exposure) → Admin Remediation proposes `POST /venues/{lpId}/halt` with Maker-Checker.

| Layer | Standard | Java | Python |
|-------|----------|------|--------|
| Agent↔Agent | Google **A2A** (JSON-RPC 2.0 / HTTP+SSE) | RestClient + AgentCard resources | `a2a-sdk` / httpx JSON-RPC |
| Agent↔Tool | **MCP** | spring-ai-mcp / RestClient | `mcp` / FastMCP |
| In-process orchestration | Orchestrator-Workers / StateGraph | Spring AI patterns | LangGraph `Command` handoffs |

### AgentCard locations

| Agent | Card |
|-------|------|
| Java orchestrator (all skills) | `Middleware/fx-agent-orchestrator-service/.../static/.well-known/agent.json` |
| Python orchestrator | `Agentic-Python/fx-agent-orchestrator-service/.../agent.json` |
| Canonical schemas | `Docs/Architecture/a2a/*.agent.json` |

Skills advertised: `incident-rca`, `blast-radius`, `backpressure`, `policy-authoring`, `lp-toxicity`, `best-execution`, `treasury-hedge`, `regulatory-rag`, `chaos-sim`, `macro-guard`.

## 5. Local Docker AIOps stack (`CICD/Local/AIOps/`)

| Component | Image | Ports | Integration |
|-----------|-------|-------|-------------|
| OTel Collector | `otel/opentelemetry-collector-contrib` | 4317, 4318 | Spring Micrometer OTLP; Python OTel FastAPI |
| Prometheus | `prom/prometheus` | 9090 | Scrape `/actuator/prometheus`, LiteLLM metrics |
| Grafana | `grafana/grafana` | 3000 | Golden signals + LLM spend dashboards |
| OpenSearch | `opensearchproject/opensearch` | 9200 | Structured JSON logs / RCF anomaly |
| OpenSearch Dashboards | `opensearchproject/opensearch-dashboards` | 5601 | Discover / alerting |
| HolmesGPT (optional profile) | `robustadev/holmes` (or stub service) | 8088 | Uses `fx-llm-gateway`; exposable as MCP tool |

## 6. npm scripts (`local:` prefix)

```bash
npm run local:llm-gateway:start|stop|status
npm run local:aiops:start|stop|status
npm run local:containers:all:start   # includes Redis/Postgres/Kafka; optional WITH_LLM=1 WITH_AIOPS=1
```

## 7. Application wiring

```yaml
# Java / Python — point OpenAI-compatible clients at the gateway
fx:
  llm:
    provider: OPENAI_COMPAT
    openai-compatible:
      base-url: ${LLM_GATEWAY_URL:http://localhost:4000}
      api-key: ${LLM_GATEWAY_KEY:sk-local-fx-sor}
      reasoning-model: fx-reasoning-tier
      fast-model: fx-fast-triage-tier
      graph-model: fx-graph-tier
      embedding-model: fx-embedding-tier
```

Python: `LLM_GATEWAY_URL`, `OPENAI_API_BASE`, model aliases identical.

## 8. Implementation checklist

- [x] Spec under `Docs/Architecture/`  
- [x] LiteLLM compose + `litellm_config.yaml`  
- [x] AIOps compose (OTel, Prometheus, Grafana, OpenSearch)  
- [x] AgentCard JSON schemas  
- [x] `local:llm-gateway:*` / `local:aiops:*` scripts  
- [x] Gateway URL config on Java/Python orchestrators  
- [ ] Production IAM for Bedrock behind LiteLLM  
- [ ] HolmesGPT full image pin + MCP bridge in orchestrator  
