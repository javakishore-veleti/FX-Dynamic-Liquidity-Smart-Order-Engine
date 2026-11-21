# OpenSearch Dashboards — PPL examples

ISM-managed indices: `fx-sor-metrics-*`, `fx-sor-exec-*`, `fx-sor-qual-*`, `fx-sor-admin-*`.

## 1. Executive SLA & Golden Signals

```ppl
source = fx-sor-metrics-*
| where service in ('fx-quote-service', 'fx-qualify-service', 'fx-route-exec-service')
| stats count() as rps by span(timestamp, 1m), service
```

```ppl
source = fx-sor-metrics-*
| stats percentile(latency_ms, 50) as p50,
        percentile(latency_ms, 95) as p95,
        percentile(latency_ms, 99) as p99 by service
| where p99 > 5
```

```ppl
source = fx-sor-exec-*
| stats avg(stp_fill_pct) as stp by span(timestamp, 5m)
| where stp < 98.5
```

## 2. LP Toxicity & Execution

```ppl
source = fx-sor-exec-*
| stats sum(last_look_rejects) as rejects, sum(last_look_attempts) as attempts by lp_id, ccy_pair
| eval reject_pct = rejects * 100.0 / attempts
| sort - reject_pct
```

```ppl
source = fx-sor-exec-*
| where hold_latency_ms > 50
| stats count() by lp_id, ccy_pair
```

```ppl
source = fx-sor-exec-*
| where circuit_state in ('OPEN', 'HALF_OPEN')
| stats count() by lp_id, circuit_state, span(timestamp, 1m)
```

## 3. JVM ZGC, Virtual Threads & Backpressure

```ppl
source = fx-sor-metrics-*
| where metric_name = 'jvm.gc.pause.duration'
| stats avg(value) as avg_pause_ms, max(value) as max_pause_ms by service
| where max_pause_ms > 1
```

```ppl
source = fx-sor-metrics-*
| where metric_name in ('kafka.consumer.lag', 'fx.topology.graph.lag_ms', 'redis.lua.latency_ms')
| stats max(value) as max_val by metric_name, service
```

## 4. Regulatory Provenance & Admin Audit

```ppl
source = fx-sor-qual-*
| where topic = 'fx.qualify.venue-knockout.v1'
| stats count() by knockoutRuleCode, ccyPair
```

```ppl
source = fx-sor-admin-*
| where topic in ('fx.admin.policy-or-override-applied.v1', 'fx.admin.synthetic-workflow-completed.v1')
| fields wfExecId, actionType, workflowType, approvedBy, durationMs, timestamp
| sort - timestamp
```

OpenSearch Anomaly Detection (RCF) recommendation: monitor `hold_latency_ms` per `lp_id` with spike threshold &gt; 50 ms. Use FGAC field-level security for PCI/spread redaction (ADR-026) and Alerting Monitors → Slack/PagerDuty.
