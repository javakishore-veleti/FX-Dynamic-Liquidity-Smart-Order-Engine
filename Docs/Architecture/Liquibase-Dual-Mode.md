# Liquibase dual mode (Spring Boot)

| Mode | Toggle | When to use |
|------|--------|-------------|
| **In-app startup** (default) | `LIQUIBASE_ON_STARTUP=true` / `fx.features.liquibase-on-startup=true` | Local H2 / simple laptop runs |
| **External CLI / Job** | `LIQUIBASE_ON_STARTUP=false` | Postgres local, CI, future K8s Job per dedicated DB |

```bash
# External flavor
npm run local:containers:all:start
LIQUIBASE_ON_STARTUP=false npm run local:middleware:liquibase:all:start
npm run local:middleware:liquibase:all:status
LIQUIBASE_ON_STARTUP=false npm run local:middleware:all:start
```

Each microservice keeps its **own** changelog under `src/main/resources/db/changelog/` and its own Postgres database (no shared schema).
