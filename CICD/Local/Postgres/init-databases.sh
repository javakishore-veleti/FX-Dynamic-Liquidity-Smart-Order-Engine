#!/usr/bin/env bash
# Creates one dedicated database per microservice (no shared schema).
set -euo pipefail

psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" <<'SQL'
SELECT 'Creating dedicated microservice databases...' AS info;

CREATE DATABASE fx_quote_db;
CREATE DATABASE fx_qualify_db;
CREATE DATABASE fx_route_exec_db;
CREATE DATABASE dlp_sor_system_db;
CREATE DATABASE fx_topology_db;
CREATE DATABASE fx_agent_orchestrator_db;
SQL
