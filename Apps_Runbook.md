# Apps Runbook

## Contents

1. [Stop](#1-stop) — Portals · Middleware · LLM Gateway · AIOps · Containers
2. [Start](#2-start) — Containers · LLM Gateway · AIOps · Middleware · Portals · Python dual-stack
3. [Status](#3-status) — Containers · LLM Gateway · AIOps · Middleware · Portals
4. [External Liquibase](#4-external-liquibase)
5. [API endpoints](#5-api-endpoints)
6. [OpenAPI](#6-openapi)
7. [Portals](#7-portals)
8. [Infrastructure UIs](#8-infrastructure-uis)

---

## 1. Stop

Docker stop scripts remove containers and named volumes (`down -v`).

### 1.1 Portals

```bash
npm run local:portals:all:stop
```

### 1.2 Middleware

```bash
npm run local:middleware:all:stop
```

### 1.3 LLM Gateway

```bash
npm run local:llm-gateway:stop
```

### 1.4 AIOps

```bash
npm run local:aiops:stop
```

### 1.5 Containers

```bash
npm run local:containers:all:stop
```

---

## 2. Start

Run in order.

### 2.1 Containers

```bash
npm run local:containers:all:start
```

### 2.2 LLM Gateway

```bash
npm run local:llm-gateway:start
```

### 2.3 AIOps

```bash
npm run local:aiops:start
```

### 2.4 Middleware

```bash
npm run local:middleware:all:start
```

### 2.5 Portals

```bash
npm run local:portals:all:start
```

### 2.6 Python dual-stack (optional)

```bash
npm run local:topology-mcp:python
npm run local:agent-orchestrator:python
```

---

## 3. Status

### 3.1 Containers

```bash
npm run local:containers:all:status
```

### 3.2 LLM Gateway

```bash
npm run local:llm-gateway:status
```

### 3.3 AIOps

```bash
npm run local:aiops:status
```

### 3.4 Middleware

```bash
npm run local:middleware:all:status
```

### 3.5 Portals

```bash
npm run local:portals:all:status
```

---

## 4. External Liquibase

Default: migrations run on Spring Boot startup (`LIQUIBASE_ON_STARTUP=true`).

External path (Postgres):

```bash
npm run local:containers:all:start
npm run local:middleware:liquibase:all:start
LIQUIBASE_ON_STARTUP=false npm run local:middleware:all:start
npm run local:middleware:liquibase:all:status
```

---

## 5. API endpoints

Services bind `0.0.0.0` (reachable from any host). Replace `localhost` with the machine hostname or IP when calling remotely.

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

| Check | URL |
|-------|-----|
| quote health | http://localhost:8081/actuator/health |
| qualify health | http://localhost:8082/actuator/health |
| route-exec health | http://localhost:8083/actuator/health |
| admin health | http://localhost:8084/actuator/health |
| topology health | http://localhost:8085/actuator/health |
| topology-mcp (Java) health | http://localhost:8091/actuator/health |
| agent-orchestrator (Java) health | http://localhost:8092/actuator/health |
| topology-mcp (Python) health | http://localhost:8191/health |
| agent-orchestrator (Python) health | http://localhost:8192/health |
| quote metrics | http://localhost:8081/actuator/prometheus |
| qualify metrics | http://localhost:8082/actuator/prometheus |
| route-exec metrics | http://localhost:8083/actuator/prometheus |
| admin metrics | http://localhost:8084/actuator/prometheus |
| topology metrics | http://localhost:8085/actuator/prometheus |
| topology-mcp (Java) metrics | http://localhost:8091/actuator/prometheus |
| agent-orchestrator (Java) metrics | http://localhost:8092/actuator/prometheus |
| topology-mcp (Python) metrics | http://localhost:8191/metrics |
| agent-orchestrator (Python) metrics | http://localhost:8192/metrics |
| AgentCard (Java) | http://localhost:8092/.well-known/agent.json |
| AgentCard (Python) | http://localhost:8192/.well-known/agent.json |

---

## 6. OpenAPI

| Service | UI | Spec |
|---------|----|------|
| quote | http://localhost:8081/swagger-ui.html | http://localhost:8081/v3/api-docs |
| qualify | http://localhost:8082/swagger-ui.html | http://localhost:8082/v3/api-docs |
| route-exec | http://localhost:8083/swagger-ui.html | http://localhost:8083/v3/api-docs |
| admin | http://localhost:8084/swagger-ui.html | http://localhost:8084/v3/api-docs |
| topology | http://localhost:8085/swagger-ui.html | http://localhost:8085/v3/api-docs |
| topology-mcp (Java) | http://localhost:8091/swagger-ui.html | http://localhost:8091/v3/api-docs |
| agent-orchestrator (Java) | http://localhost:8092/swagger-ui.html | http://localhost:8092/v3/api-docs |
| topology-mcp (Python) | http://localhost:8191/docs | http://localhost:8191/openapi.json |
| agent-orchestrator (Python) | http://localhost:8192/docs | http://localhost:8192/openapi.json |

---

## 7. Portals

| App | URL |
|-----|-----|
| Admin | http://localhost:4201 |
| Corporate | http://localhost:4202 |
| Retail | http://localhost:4203 |

---

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
