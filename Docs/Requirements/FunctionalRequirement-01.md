# Functional Requirement FR-01 — Session & Regime Guardrails

**Document ID:** FR-01  
**PRD:** [PRD-FX-SOR-001](../../PRD-FX-SOR-001.md) §4 / §4.1  
**Owner (primary):** `fx-quote-service`  
**Implementation status:** **DONE 100%**  
**Last updated:** 2026-11-21  

## 1. Requirement statement

> **FR-01 Session & Regime Guardrails:** Automatically tighten spread tolerances and increase last-look penalties during NY rollover and high-impact macro releases (FOMC, NFP, ECB).

| Strategic pillar linkage | How FR-01 contributes | Status |
| :---- | :---- | :---- |
| **Margin Preservation** | Session-aware client markup avoids giving away spread in thin/volatile windows | DONE |
| **Execution Certainty** | Higher last-look penalties in rollover/macro reduce toxic LP preference downstream | DONE |
| **Low-Latency Decisioning** | Regime + markup resolved from L1 schedule cache on the quote hot path | DONE |
| **Policy Agility** | Desk updates tier×pair×regime schedules without redeploying quote pods | DONE |
| **Regulatory Provenance** | Regime-shift events, BPMN `wf_exec_log`, quote responses carry `sessionRegime` | DONE |

---

## 2. Portals (nothing hidden)

| Portal | Left-nav / screen | What is visible | Status |
| :---- | :---- | :---- | :---- |
| **FX Admin** | **Workflows → Master List** | Master `workflows` table (BPMN definitions) | DONE |
| **FX Admin** | **Workflows → Executions** | All `wf_exec_log` rows + BPMN task execution view | DONE |
| **FX Admin** | Policy Studio | Spread schedules + Arm/Clear MACRO + calendar via APIs | DONE |
| **FX Corporate** | Blotter | Regime / markup / indicative + **Run quote→route BPMN** task strip | DONE |
| **FX Retail** | Convert | Retail markup / regime + **Run convert BPMN** task strip | DONE |

**Portal split:** Admin = configure + BPMN catalog/executions · Corporate + Retail = consume + start scoped BPMN (logged in admin `wf_exec_log`).

---

## 3. BPMN + master workflows + wf_exec_log

| Artifact | Purpose | Status |
| :---- | :---- | :---- |
| Table `workflows` | Master list of BPMN process definitions | DONE |
| Table `wf_exec_log` (+ `workflow_key`) | Every BPMN start / ops action execution log | DONE |
| `bpmn/fr01-session-regime-guardrails.bpmn20.xml` | Admin FR-01 process (incl. desk USER confirm) | DONE |
| `bpmn/fr01-corporate-quote-route.bpmn20.xml` | Corporate quote → route | DONE |
| `bpmn/fr01-retail-convert.bpmn20.xml` | Retail convert | DONE |
| `BpmnWorkflowService` | Executes steps; persists task states in `execution_summary` | DONE |
| APIs | `GET /workflow-definitions`, `POST /bpmn/{key}/start`, `GET/POST .../bpmn/executions/{id}` | DONE |

**No silent auto-arm:** economic calendar is published and scanned only inside the visible BPMN `task_scan_calendar`; desk must complete USER task `task_confirm_macro` before `task_arm_macro`.

---

## 4. Microservices

| Impact | Microservice | Role | Status |
| :---- | :---- | :---- | :---- |
| **Primary** | `fx-quote-service` | Regime, markup×news, schedule CRUD, calendar API, macro flag (in-mem/Redis), events | DONE |
| **Control** | `fx-admin-service` | Master workflows, BPMN engine, `wf_exec_log`, Policy Studio proxies | DONE |
| **Downstream** | `fx-route-exec-service` | Self-resolve regime (request → Kafka hint → quote HTTP); penalty in splits | DONE |
| **Assist** | `fx-agent-orchestrator-service` | MacroGuard → `PUT /macro-flag` when `dryRun=false` | DONE |

---

## 5. Data / APIs / facades (checklist)

| # | Capability | Status |
| :---- | :---- | :---- |
| 1 | Clock session Tokyo / London–NY / NY rollover | DONE |
| 2 | Published FOMC/NFP/ECB calendar JSON + `GET /economic-calendar` | DONE |
| 3 | Macro arm via BPMN / Admin / Agent (armedBy visible) | DONE |
| 4 | `markup × news_multiplier` (+ severity when macro) | DONE |
| 5 | L1 `SpreadScheduleCache` + invalidate | DONE |
| 6 | Maker-checker draft/submit/approve | DONE |
| 7 | Kafka topics `regime-shifted` / `last-look-penalty-hint` (+ route-exec consumer when kafka profile) | DONE |
| 8 | Redis-backed macro flag when `load-redis` | DONE |
| 9 | Route-exec self-resolve last-look weight | DONE |
| 10 | Portal BPMN task views (Admin / Corporate / Retail) | DONE |

---

## 6. Practical sequence

1. Restart middleware (`npm run local:middleware:all:stop` then `start`).  
2. Admin http://localhost:4201/workflows/master — see FR-01 BPMN definitions.  
3. Admin http://localhost:4201/workflows/executions — **Start BPMN**; if macro window active, click **Complete desk confirm**.  
4. Corporate http://localhost:4202/blotter — **Run quote→route BPMN**; task strip appears.  
5. Retail http://localhost:4203/convert — **Run convert BPMN**.  

---

## 7. Package / design compliance

| Rule | How FR-01 complied | Status |
| :---- | :---- | :---- |
| Neal Ford closed layers | Quote facades under `business.facade` | DONE |
| Zero-sharing | Admin/route-exec/agent use HTTP RestClient only | DONE |
| Visible workflows | Master `workflows` + every run in `wf_exec_log` + Angular task views | DONE |
