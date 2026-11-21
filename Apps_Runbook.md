# Apps Runbook

## Table of contents

1. [Prerequisites](#1-prerequisites)
2. [Stop everything](#2-stop-everything)
3. [Start everything](#3-start-everything)
4. [Optional: external Liquibase](#4-optional-external-liquibase)
5. [Status checks](#5-status-checks)
6. [URLs — Middleware APIs](#6-urls--middleware-apis)
7. [URLs — OpenAPI / Swagger](#7-urls--openapi--swagger)
8. [URLs — Portals](#8-urls--portals)
9. [URLs — Containers & AIOps UI](#9-urls--containers--aiops-ui)
10. [URLs — LLM gateway](#10-urls--llm-gateway)

---

## 1. Prerequisites

- Docker Desktop running  
- Node 20+  
- Java 21 + Maven  
- (Optional) Python 3.11+ for Agentic-Python services  
- (Optional) Ollama on `:11434` for local LLM fallbacks  

---

## 2. Stop everything

Run in this order:

```bash
npm run local:portals:all:stop
npm run local:middleware:all:stop
npm run local:llm-gateway:stop
npm run local:aiops:stop
npm run local:containers:all:stop
```

---

## 3. Start everything

Run in this order:

```bash
npm run local:containers:all:start
npm run local:llm-gateway:start
npm run local:aiops:start
npm run local:middleware:all:start
npm run local:portals:all:start
```

Optional Python dual-stack:

```bash
npm run local:topology-mcp:python
npm run local:agent-orchestrator:python
```

Optional model-tuning (offline):

```bash
npm run local:model-tuning:all
```

---

## 4. Optional: external Liquibase

Default: Liquibase runs **inside** each Spring Boot app (`LIQUIBASE_ON_STARTUP=true`).

External flavor (Postgres only):

```bash
npm run local:containers:all:start
npm run local:middleware:liquibase:all:start
LIQUIBASE_ON_STARTUP=false npm run local:middleware:all:start
```

Status:

```bash
npm run local:middleware:liquibase:all:status
```

---

## 5. Status checks

```bash
npm run local:containers:all:status
npm run local:llm-gateway:status
npm run local:aiops:status
npm run local:middleware:all:status
npm run local:portals:all:status
npm run local:middleware:liquibase:all:status
```

---

## 6. URLs — Middleware APIs

| Service | Base URL |
|---------|----------|
| fx-quote-service | http://localhost:8081 |
| fx-qualify-service | http://localhost:8082 |
| fx-route-exec-service | http://localhost:8083 |
| fx-admin-service | http://localhost:8084 |
| fx-topology-service | http://localhost:8085 |
| fx-topology-mcp-server (Java) | http://localhost:8091 |
| fx-agent-orchestrator-service (Java) | http://localhost:8092 |
| fx-topology-mcp-server (Python) | http://localhost:8191 |
| fx-agent-orchestrator-service (Python) | http://localhost:8192 |

Health examples:

- http://localhost:8081/actuator/health  
- http://localhost:8092/actuator/health  
- http://localhost:8192/health  

Metrics:

- Java: `http://localhost:<port>/actuator/prometheus`  
- Python: `http://localhost:8191/metrics` · `http://localhost:8192/metrics`  

A2A AgentCard:

- http://localhost:8092/.well-known/agent.json  
- http://localhost:8192/.well-known/agent.json  

---

## 7. URLs — OpenAPI / Swagger

| Service | Swagger UI | OpenAPI JSON |
|---------|------------|--------------|
| quote :8081 | http://localhost:8081/swagger-ui.html | http://localhost:8081/v3/api-docs |
| qualify :8082 | http://localhost:8082/swagger-ui.html | http://localhost:8082/v3/api-docs |
| route-exec :8083 | http://localhost:8083/swagger-ui.html | http://localhost:8083/v3/api-docs |
| admin :8084 | http://localhost:8084/swagger-ui.html | http://localhost:8084/v3/api-docs |
| topology :8085 | http://localhost:8085/swagger-ui.html | http://localhost:8085/v3/api-docs |
| topology-mcp :8091 | http://localhost:8091/swagger-ui.html | http://localhost:8091/v3/api-docs |
| agent-orchestrator :8092 | http://localhost:8092/swagger-ui.html | http://localhost:8092/v3/api-docs |
| topology-mcp (Python) :8191 | http://localhost:8191/docs | http://localhost:8191/openapi.json |
| agent-orchestrator (Python) :8192 | http://localhost:8192/docs | http://localhost:8192/openapi.json |

---

## 8. URLs — Portals

| Portal | URL |
|--------|-----|
| Admin | http://localhost:4201 |
| Corporate | http://localhost:4202 |
| Retail | http://localhost:4203 |

---

## 9. URLs — Containers & AIOps UI

| Component | URL |
|-----------|-----|
| Postgres | localhost:5432 |
| Redis | localhost:6379 |
| Kafka | localhost:9092 |
| Grafana | http://localhost:3000 (admin / fxsor) |
| Prometheus | http://localhost:9090 |
| OpenSearch | http://localhost:9200 |
| OpenSearch Dashboards | http://localhost:5601 |
| OTel Collector (OTLP HTTP) | http://localhost:4318 |
| OTel Collector (OTLP gRPC) | localhost:4317 |
| Holmes stub (optional) | http://localhost:8088 |

Holmes profile:

```bash
WITH_HOLMES=1 npm run local:aiops:start
```

---

## 10. URLs — LLM gateway

| Item | URL |
|------|-----|
| LiteLLM Proxy | http://localhost:4000 |
| Chat completions | http://localhost:4000/v1/chat/completions |
| Embeddings | http://localhost:4000/v1/embeddings |

Key default: `sk-local-fx-sor` (`LITELLM_MASTER_KEY`).  
Model aliases: `fx-reasoning-tier` · `fx-fast-triage-tier` · `fx-graph-tier` · `fx-embedding-tier`.
