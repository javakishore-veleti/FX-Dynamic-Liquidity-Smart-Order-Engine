# Apps Runbook

## Contents

1. [Stop](#1-stop)
2. [Start](#2-start)
3. [Status](#3-status)
4. [External Liquibase](#4-external-liquibase)
5. [API endpoints](#5-api-endpoints)
6. [OpenAPI](#6-openapi)
7. [Portals](#7-portals)
8. [Infrastructure UIs](#8-infrastructure-uis)

---

## 1. Stop

```bash
npm run local:portals:all:stop
npm run local:middleware:all:stop
npm run local:llm-gateway:stop
npm run local:aiops:stop
npm run local:containers:all:stop
```

## 2. Start

```bash
npm run local:containers:all:start
npm run local:llm-gateway:start
npm run local:aiops:start
npm run local:middleware:all:start
npm run local:portals:all:start
```

Python dual-stack (optional):

```bash
npm run local:topology-mcp:python
npm run local:agent-orchestrator:python
```

## 3. Status

```bash
npm run local:containers:all:status
npm run local:llm-gateway:status
npm run local:aiops:status
npm run local:middleware:all:status
npm run local:portals:all:status
```

## 4. External Liquibase

Default: migrations run on Spring Boot startup (`LIQUIBASE_ON_STARTUP=true`).

External path (Postgres):

```bash
npm run local:containers:all:start
npm run local:middleware:liquibase:all:start
LIQUIBASE_ON_STARTUP=false npm run local:middleware:all:start
npm run local:middleware:liquibase:all:status
```

## 5. API endpoints

| Service | URL |
|---------|-----|
| quote | http://localhost:8081 |
| qualify | http://localhost:8082 |
| route-exec | http://localhost:8083 |
| admin | http://localhost:8084 |
| topology | http://localhost:8085 |
| topology-mcp (Java) | http://localhost:8091 |
| agent-orchestrator (Java) | http://localhost:8092 |
| topology-mcp (Python) | http://localhost:8191 |
| agent-orchestrator (Python) | http://localhost:8192 |

| Check | URL pattern |
|-------|-------------|
| Health (Java) | `http://localhost:<port>/actuator/health` |
| Health (Python) | `http://localhost:<port>/health` |
| Metrics (Java) | `http://localhost:<port>/actuator/prometheus` |
| Metrics (Python) | `http://localhost:<port>/metrics` |
| AgentCard | http://localhost:8092/.well-known/agent.json · http://localhost:8192/.well-known/agent.json |

## 6. OpenAPI

| Service | UI | Spec |
|---------|----|------|
| :8081–:8092 (Java) | `http://localhost:<port>/swagger-ui.html` | `http://localhost:<port>/v3/api-docs` |
| :8191 / :8192 (Python) | `http://localhost:<port>/docs` | `http://localhost:<port>/openapi.json` |

## 7. Portals

| App | URL |
|-----|-----|
| Admin | http://localhost:4201 |
| Corporate | http://localhost:4202 |
| Retail | http://localhost:4203 |

## 8. Infrastructure UIs

| Component | URL / host |
|-----------|------------|
| Postgres | localhost:5432 |
| Redis | localhost:6379 |
| Kafka | localhost:9092 |
| LiteLLM | http://localhost:4000 |
| Grafana | http://localhost:3000 (`admin` / `fxsor`) |
| Prometheus | http://localhost:9090 |
| OpenSearch | http://localhost:9200 |
| OpenSearch Dashboards | http://localhost:5601 |
| OTLP HTTP | http://localhost:4318 |
| OTLP gRPC | localhost:4317 |
| Holmes (optional) | `WITH_HOLMES=1 npm run local:aiops:start` → http://localhost:8088 |
