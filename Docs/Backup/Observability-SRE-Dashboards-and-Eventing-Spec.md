# **Observability, SRE Dashboards, Cloud Watchdogs & Feature-Toggled Eventing Specification**

**Document ID:** Observability-SRE-Dashboards-and-Eventing-Spec.md  
**Repository:** fx-dynamic-liquidity-smart-order-engine  
**Base Java Namespace:** com.jk.apps.banking.fx.sor  
**Target Runtime & Scale:** Java 21+ Virtual Threads, Generational ZGC, eBPF (Cilium), 50–150+ K8s Pods (\~1,000,000 Aggregate RPS)  
**Local Dev Strategy:** Zero-Broker Default (app.events.broker-enabled=false)

## **1\. SRE & eFX Dealing-Desk Dashboards (ELK vs. AWS OpenSearch)**

Both **ELK (Elasticsearch \+ Logstash/Filebeat \+ Kibana)** and **AWS OpenSearch Service (OpenSearch \+ Data Prepper \+ OpenSearch Dashboards)** ingest non-blocking structured JSON logs (ADR-022) and tail-sampled OpenTelemetry spans (ADR-020), separated into four role-based operational views:

| Dashboard View | Core Visual Panels & Widgets | ELK (Kibana) Implementation | AWS OpenSearch Dashboards Implementation |
| :---- | :---- | :---- | :---- |
| **1\. Executive SLA & Golden Signals (1M RPS Health)** | Aggregate RPS by service (fx-quote, fx-qualify, fx-route-exec); internal decisioning latency (p50, p95, p99 \< 5 ms) & E2E (\< 15 ms); first-pass STP fill ratio (\> 98.5%); HTTP 5xx/timeout error budget burn. | **Kibana Lens \+ TSVB** backed by fx-sor-metrics-\* ILM hot-warm-cold data streams. | **OpenSearch Observability Dashboards** using **PPL (Piped Processing Language)** queries over ISM-managed indices. |
| **2\. Liquidity Provider (LP) Toxicity & Execution View** | Per-LP last-look rejection rate (%) & hold latency heatmap (ms); spread capture vs. slippage (bps) across 28 currency pairs; active **Resilience4j** circuit-breaker state transitions (OPEN/HALF\_OPEN). | **Kibana Data Table \+ Heatmap** grouped by lp\_id, ccy\_pair, and session\_regime. | **OpenSearch Anomaly Detection (RCF)** automatically flagging sudden LP hold-time spikes (\> 50 ms). |
| **3\. JVM ZGC, Virtual Threads & Kernel Backpressure** | Generational ZGC pause durations (\< 1 ms target) & heap allocation rate; Virtual Thread carrier pinning warnings & Redis Lua latency; Kafka consumer lag (Stage 1–3 backpressure) & fx-topology-service graph lag. | **Elastic APM / Metricbeat** JVM & Kubernetes pod views with threshold annotations. | **OpenSearch Trace Analytics \+ Prometheus Remote-Write** correlation panels. |
| **4\. Regulatory Provenance & Admin Audit View** | Knock-out rule breakdown (CLS cutoff, bilateral credit ceiling, stale quote); dealing-desk escalations (USD/INR, USD/BRL, fat-finger); fx-admin-service Maker-Checker actions & wf\_exec\_log batch runs. | **Kibana Discover Saved Searches** with role-based field masking (PCI/spread redaction per ADR-026). | **OpenSearch Security FGAC (Field-Level Security)** \+ **Alerting Monitors** posting to Slack/PagerDuty. |

## **2\. Cloud-Native Observability & Governance (AWS vs. Azure)**

| Capability | AWS Stack (CloudWatch \+ CloudTrail) | Azure Stack (Application Insights \+ Azure Monitor) | Monitored Signals & Thresholds |
| :---- | :---- | :---- | :---- |
| **Control-Plane & Governance Audit** | **AWS CloudTrail** (Management \+ Data Events) | **Azure Activity Log \+ Diagnostic Settings** | Tracks EKS/AKS RBAC changes, KMS/Key Vault rotations, Secrets Manager reads (fx-admin-service), S3/Iceberg audit bucket writes, and IAM role assumptions. |
| **Infrastructure & Container Watchdogs** | **Amazon CloudWatch Container Insights \+ Composite Alarms** | **Azure Monitor Container Insights \+ Smart Detection** | Watches cluster node CPU, Cilium eBPF conntrack utilization, Redis memory/evictions, and PostgreSQL/Aurora replication lag. |
| **Distributed APM & Tail-Sampled Traces** | **AWS X-Ray / OpenTelemetry Collector (ADOT)** | **Azure Application Insights (Live Metrics \+ Application Map)** | Visualizes live service-to-service topology (Quote \-\> Qualify \-\> RouteExec), pinpoints slow LP HTTP/FIX calls, and captures 100% of failed or slow (\> 10 ms) traces. |
| **Proactive Automated Watchdogs / Alerts** | **CloudWatch Anomaly Detection \+ Synthetics Canaries** | **Application Insights Availability Tests \+ KQL Alert Rules** | Runs synthetic /api/v1/fx/quotes/indicative canary probes every 60 seconds and alerts PagerDuty if STP fill rate drops below 98.5% or p99 \> 5 ms. |

## **3\. Feature-Toggled Domain Eventing (Default OFF in Local Dev)**

To keep local laptop development (--spring.profiles.active=local with embedded H2) completely independent of an external Kafka broker, every microservice routes event emission through a closed-layer DomainEventPublisher abstraction controlled by a Spring Boot feature toggle:  
\# application.yml (Default: OFF for local laptop development)  
app:  
  events:  
    broker-enabled: false \# Set to true in dev/staging/prod profiles when Kafka is active

> * **When app.events.broker-enabled=false (Default Local Mode):** Spring injects NoOpLoggingEventPublisher, which serializes a lightweight DEBUG structured log line without requiring Kafka containers or network sockets.  
> * **When app.events.broker-enabled=true (Cluster Mode):** Spring injects KafkaAvroDomainEventPublisher, publishing non-blocking Avro events to Kafka topics consumed downstream by Airflow batch DAGs (per ADR-017) and real-time topology/audit pipelines.

## **4\. Catalog of Optional Domain Events by Microservice**

| Owning Microservice | Event Topic / Avro Schema | Trigger Condition | Key Payload Fields |
| :---- | :---- | :---- | :---- |
| **fx-quote-service** | fx.quote.regime-shifted.v1 | Trading session transitions (LONDON\_NY\_OVERLAP, NY\_ROLLOVER\_5PM) or macro news guard activates. | eventId, ccyPair, previousRegime, activeRegime, spreadMultiplier, timestamp |
| **fx-quote-service** | fx.quote.stale-feed-detected.v1 | An LP streaming quote age exceeds its configured ttl\_ms threshold. | eventId, lpId, ccyPair, lastQuoteTs, ageMs, thresholdMs |
| **fx-qualify-service** | fx.qualify.venue-knockout.v1 | Order fails deterministic qualification (CLS cutoff, bilateral credit limit, or restricted EM pair). | eventId, clientOrderId, clientId, ccyPair, knockoutRuleCode, escalatedToDesk |
| **fx-qualify-service** | fx.qualify.credit-threshold-breached.v1 | Bilateral counterparty credit utilization or desk NOP crosses 85% warning or 95% hard-stop threshold. | eventId, clientId, lpId, utilizedUsd, maxLimitUsd, utilizationPct |
| **fx-route-exec-service** | fx.exec.order-routed-provenance.v1 | Every completed order execution (mandatory FX Global Code / MiFID II RTS-27/28 regulatory audit trail). | eventId, clientOrderId, winningLpId, internalized, childSplits\[\], slippageBps, droolsRuleVersion |
| **fx-route-exec-service** | fx.exec.last-look-failover.v1 | Primary LP rejects an order during last-look hold window, triggering waterfall failover to backup LP. | eventId, orderId, rejectedLpId, holdDurationMs, backupLpId, incrementalSlippageBps |
| **fx-admin-service** | fx.admin.policy-or-override-applied.v1 | Admin executes cache invalidation, LP venue halt, credit override, or Drools policy hot-reload. | eventId, wfExecId, actionType, targetResource, approvedBy, diffSummary |
| **fx-admin-service** | fx.admin.synthetic-workflow-completed.v1 | Async batch synthetic dataset seeding or load-simulation run completes in dlp\_sor\_system\_db.wf\_exec\_log. | eventId, wfExecId, workflowType, batchesCompleted, recordsInserted, durationMs |
| **fx-topology-service** | fx.topology.blast-radius-alert.v1 | Time-travel graph diff detects a new runtime dependency edge or degraded upstream/downstream path. | eventId, sourceService, targetService, layer (EBPF/IPC), p99DeltaMs, affectedDeskOwners\[\] |

