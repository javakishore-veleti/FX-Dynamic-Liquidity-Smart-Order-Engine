# Local AIOps stack

OTel Collector, Prometheus, Grafana, OpenSearch (+ optional Holmes stub).

```bash
npm run local:aiops:start
WITH_HOLMES=1 npm run local:aiops:start
npm run local:aiops:status
npm run local:aiops:stop
```

- Grafana http://localhost:3000 (admin / fxsor)
- Prometheus http://localhost:9090
- OpenSearch http://localhost:9200
- OTLP :4317 / :4318
