# OpenAPI / Swagger endpoints

Java services use **springdoc-openapi** (`springdoc-openapi-starter-webmvc-ui`).  
Python FastAPI exposes OpenAPI automatically.

## Java (springdoc-openapi)

| Service | Port | Swagger UI | OpenAPI JSON |
|---------|------|------------|--------------|
| fx-quote-service | 8081 | http://localhost:8081/swagger-ui.html | http://localhost:8081/v3/api-docs |
| fx-qualify-service | 8082 | http://localhost:8082/swagger-ui.html | http://localhost:8082/v3/api-docs |
| fx-route-exec-service | 8083 | http://localhost:8083/swagger-ui.html | http://localhost:8083/v3/api-docs |
| fx-admin-service | 8084 | http://localhost:8084/swagger-ui.html | http://localhost:8084/v3/api-docs |
| fx-topology-service | 8085 | http://localhost:8085/swagger-ui.html | http://localhost:8085/v3/api-docs |
| fx-topology-mcp-server | 8091 | http://localhost:8091/swagger-ui.html | http://localhost:8091/v3/api-docs |
| fx-agent-orchestrator-service | 8092 | http://localhost:8092/swagger-ui.html | http://localhost:8092/v3/api-docs |

Also: `/swagger-ui/index.html` and YAML at `/v3/api-docs.yaml`.

## Python (FastAPI built-in)

| Service | Port | Swagger UI | ReDoc | OpenAPI JSON |
|---------|------|------------|-------|--------------|
| fx-topology-mcp-server | 8191 | http://localhost:8191/docs | http://localhost:8191/redoc | http://localhost:8191/openapi.json |
| fx-agent-orchestrator-service | 8192 | http://localhost:8192/docs | http://localhost:8192/redoc | http://localhost:8192/openapi.json |
