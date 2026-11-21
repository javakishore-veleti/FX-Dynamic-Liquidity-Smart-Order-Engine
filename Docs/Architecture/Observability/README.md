# Observability & SRE Dashboards

Reference pack for ELK/Kibana, AWS OpenSearch, and cloud watchdogs aligned to
[Observability-SRE-Dashboards-and-Eventing-Spec.md](../../Specs/Observability-SRE-Dashboards-and-Eventing-Spec.md).

## Four role-based dashboard views

| # | View | Primary signals |
|---|------|-----------------|
| 1 | **Executive SLA & Golden Signals** | Aggregate RPS (quote / qualify / route-exec); decisioning p50/p95/p99 &lt; 5 ms; E2E &lt; 15 ms; first-pass STP ≥ 98.5%; HTTP 5xx / timeout error-budget burn |
| 2 | **LP Toxicity & Execution** | Per-LP last-look rejection % & hold-latency heatmap; spread capture vs slippage (bps); Resilience4j circuit state (OPEN / HALF_OPEN) |
| 3 | **JVM ZGC, Virtual Threads & Kernel Backpressure** | Generational ZGC pause (&lt; 1 ms); VT carrier pinning; Redis Lua latency; Kafka consumer lag; topology graph lag |
| 4 | **Regulatory Provenance & Admin Audit** | Knock-out rule breakdown; dealing-desk escalations; Maker-Checker / `wf_exec_log` batch runs |

Artifact locations:

- Kibana (documentation-grade saved objects): [`kibana/`](kibana/)
- OpenSearch PPL examples: [`opensearch/README.md`](opensearch/README.md)
- Cloud watchdogs: [`cloud/aws-cloudwatch-alarms.md`](cloud/aws-cloudwatch-alarms.md), [`cloud/azure-monitor-alerts.md`](cloud/azure-monitor-alerts.md)

## AWS vs Azure watchdog matrix

| Capability | AWS | Azure | Thresholds / probes |
|------------|-----|-------|---------------------|
| Control-plane & governance audit | CloudTrail (mgmt + data) | Activity Log + Diagnostic Settings | EKS/AKS RBAC, KMS/Key Vault, Secrets Manager reads, Iceberg/S3 audit writes, IAM role assumptions |
| Infra & container watchdogs | CloudWatch Container Insights + composite alarms | Azure Monitor Container Insights + Smart Detection | Node CPU, Cilium eBPF conntrack, Redis memory/evictions, Postgres/Aurora lag |
| Distributed APM | X-Ray / ADOT OTel collector | App Insights Live Metrics + Application Map | Quote → Qualify → RouteExec; slow LP FIX/HTTP; 100% failed or &gt; 10 ms traces |
| Proactive canaries / alerts | CloudWatch Anomaly + Synthetics | App Insights Availability + KQL | `/api/v1/fx/quotes/indicative` every 60s; STP &lt; 98.5% or p99 &gt; 5 ms → PagerDuty |

## Domain eventing toggle

Local default: `app.events.broker-enabled: false` → `NoOpLoggingDomainEventPublisher` (DEBUG structured log).

Cluster profiles `local-kafka` / `load-kafka`: `app.events.broker-enabled: true` → `KafkaJsonDomainEventPublisher` (JSON + Avro schema-name header stub).
