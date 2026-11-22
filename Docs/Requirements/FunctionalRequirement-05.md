# Functional Requirement FR-05 — Policy Authoring Lifecycle

**Document ID:** FR-05
**PRD:** [PRD-FX-SOR-001](../../PRD-FX-SOR-001.md) §4 / §4.1
**Owner (primary):** `fx-admin-service` (+ policy agent assist)
**Implementation status:** **PARTIAL — not verified.** Workflow engine and spread-schedule draft/submit/approve exist; no user roles are checked.
**Business view:** [functional-requirements-business-view](Claude/functional-requirements-business-view.md) §2 F8, F9

---

## 1. Requirement statement

> **FR-05 Policy Authoring Lifecycle:** All Drools/DMN rule updates require four-eyes Maker-Checker approval and historical shadow-mode replay validation before hot-reloading into live pods.

## 2. Business rules

- Every rule change is drafted, then submitted, then approved by a different person.
- The maker cannot approve their own change.
- Changes are replayed against past data (shadow mode) before they go live.
- Every step is recorded: who, when, and what changed.

## 3. Components already in code (to verify)

| Area | Where | Notes |
|---|---|---|
| Spread schedule lifecycle | `fx-quote-service` · draft, submit, approve | Maker-checker style, via admin proxy |
| Admin proxy | `fx-admin-service` · `/api/v1/admin/spread-schedules/*` | Forwards to quote |
| Workflow engine | `fx-admin-service` · BPMN master list and executions | Records each run in `wf_exec_log` |
| Policy agent | `fx-agent-orchestrator-service` · `PolicyAuthoringAgentWorkflow` | Assists drafting |

## 4. Checklist

| # | Item | Status |
|---|---|---|
| 1 | Users and roles: maker, checker, admin | TODO (not reviewed) |
| 2 | Maker cannot approve own change | TO VERIFY |
| 3 | Shadow-mode replay against historical orders | TODO (not built) |
| 4 | Every change recorded with before and after values | TO VERIFY |
| 5 | Live change only after approval | TO VERIFY |

## 5. Acceptance test (plain)

1. Maker drafts a new markup for TIER_2, EUR/USD, London/NY overlap.
2. Maker tries to approve it → refused.
3. Checker approves → change is replayed against last month's orders and the result is shown.
4. Change goes live only after the replay result is accepted.
