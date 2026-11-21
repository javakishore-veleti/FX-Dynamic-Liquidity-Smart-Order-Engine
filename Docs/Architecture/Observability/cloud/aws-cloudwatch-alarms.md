# AWS CloudWatch / Synthetics watchdogs

Aligned to Observability-SRE-Dashboards-and-Eventing-Spec §2.

## Synthetics canary — indicative quotes

| Setting | Value |
|---------|-------|
| Endpoint | `GET/POST /api/v1/fx/quotes/indicative` (fx-quote-service) |
| Schedule | Every 60 seconds |
| Success criteria | HTTP 2xx + payload contains `ccyPair` / executable rate |
| Failure action | PagerDuty (severity: critical) |

Example CloudWatch Synthetics (conceptual):

```json
{
  "Name": "fx-sor-indicative-quote-canary",
  "RuntimeVersion": "syn-nodejs-puppeteer-7.0",
  "Schedule": { "Expression": "rate(1 minute)" },
  "SuccessRetentionPeriodInDays": 31,
  "FailureRetentionPeriodInDays": 31,
  "RunConfig": {
    "EnvironmentVariables": {
      "QUOTE_URL": "https://fx-quote.example/api/v1/fx/quotes/indicative"
    }
  }
}
```

## Metric alarms

| Alarm | Metric / expression | Threshold | Period |
|-------|---------------------|-----------|--------|
| STP fill floor | `stp_fill_pct` (Custom / EMF from route-exec) | &lt; **98.5%** | 5 min |
| Decisioning p99 | `latency_p99_ms` (quote/qualify/route-exec) | &gt; **5 ms** | 1 min |
| HTTP 5xx burn | `http_server_requests{status=~5..}` | error-budget burn rate &gt; 2× | 5 min |
| Kafka consumer lag | `kafka.consumer.lag` | stage-dependent | 1 min |

## Composite / anomaly

- **Container Insights**: node CPU, Redis evictions, Aurora replication lag.
- **CloudWatch Anomaly Detection**: LP hold latency spikes (&gt; 50 ms).
- **CloudTrail**: EKS RBAC changes, KMS rotations, Secrets Manager reads (fx-admin-service), S3/Iceberg audit writes.

## ADOT / X-Ray

Tail-sample 100% of failed traces and spans with duration &gt; 10 ms across Quote → Qualify → RouteExec.
