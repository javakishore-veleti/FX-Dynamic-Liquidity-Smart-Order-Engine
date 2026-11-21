# Azure Monitor / Application Insights alerts

Aligned to Observability-SRE-Dashboards-and-Eventing-Spec §2.

## Availability test — indicative quotes

| Setting | Value |
|---------|-------|
| Test type | Standard / multi-step |
| URL | `https://<fx-quote>/api/v1/fx/quotes/indicative` |
| Frequency | 60 seconds |
| Locations | ≥ 5 geo points |
| Alert | Failed locations ≥ 2 → PagerDuty / Action Group |

## KQL alert rules

### STP fill &lt; 98.5%

```kusto
customMetrics
| where name == "stp_fill_pct"
| summarize avgStp = avg(value) by bin(timestamp, 5m)
| where avgStp < 98.5
```

### Decisioning p99 &gt; 5 ms

```kusto
requests
| where cloud_RoleName in ("fx-quote-service", "fx-qualify-service", "fx-route-exec-service")
| summarize p99 = percentile(duration, 99) by bin(timestamp, 1m), cloud_RoleName
| where p99 > 5
```

### HTTP 5xx / timeout burn

```kusto
requests
| where success == false or resultCode startswith "5"
| summarize failures = count() by bin(timestamp, 5m), cloud_RoleName
| where failures > 0
```

## Platform watchdogs

| Signal | Azure feature |
|--------|---------------|
| Control-plane audit | Activity Log + Diagnostic Settings (AKS RBAC, Key Vault, Managed Identity) |
| Containers | Container Insights + Smart Detection |
| APM topology | Application Insights Application Map (Quote → Qualify → RouteExec) |
| Live failures / slow (&gt; 10 ms) | Live Metrics + 100% failed / slow sampling |

Action Group targets: PagerDuty, Slack ops channel, on-call email.
