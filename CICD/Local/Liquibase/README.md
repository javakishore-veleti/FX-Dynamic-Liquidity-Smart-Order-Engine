# External Liquibase (startup toggle OFF)

Two flavors:

1. **In-app (default):** `LIQUIBASE_ON_STARTUP=true` → Spring Boot runs Liquibase on startup  
2. **External:** `LIQUIBASE_ON_STARTUP=false` → migrate with these scripts against dedicated Postgres DBs (K8s Job later)

```bash
# Ensure Postgres is up
npm run local:containers:all:start

# Run all service changelogs
npm run local:middleware:liquibase:all:start
npm run local:middleware:liquibase:all:status

# Or one service
bash CICD/Local/Liquibase/services/fx-quote-service.sh update
bash CICD/Local/Liquibase/services/fx-quote-service.sh status
```

Requires Docker image `liquibase/liquibase:4.29`. Defaults: `localhost:5432`, user/password `fx_sor`.
