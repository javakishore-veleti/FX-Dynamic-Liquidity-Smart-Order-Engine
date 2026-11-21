#!/usr/bin/env bash
set -euo pipefail
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
COMPOSE="${SCRIPT_DIR}/AIOps/docker-compose.yaml"
echo "==> AIOps status"
docker compose -f "${COMPOSE}" ps || true
EXPECTED=(fx-sor-otel-collector fx-sor-prometheus fx-sor-grafana fx-sor-opensearch fx-sor-opensearch-dashboards)
for c in "${EXPECTED[@]}"; do
  if docker ps --format '{{.Names}}' | grep -qx "${c}"; then
    echo "  [UP]   ${c}"
  else
    echo "  [DOWN] ${c}"
  fi
done
echo "URLs: Grafana :3000 | Prometheus :9090 | OpenSearch :9200 | Dashboards :5601 | OTLP :4317/:4318"
echo "Optional Holmes stub :8088 (WITH_HOLMES=1 npm run local:aiops:start)"
