# Functional Requirement FR-03 — Multi-LP Sweep & Waterfall Failover

**Document ID:** FR-03
**PRD:** [PRD-FX-SOR-001](../../PRD-FX-SOR-001.md) §4 / §4.1
**Owner (primary):** `fx-route-exec-service`
**Implementation status:** **PARTIAL — not verified.** Routing and split classes exist; no end-to-end checklist has been run.
**Business view:** [functional-requirements-business-view](Claude/functional-requirements-business-view.md) §2 F3, F4

---

## 1. Requirement statement

> **FR-03 Multi-LP Sweep & Waterfall Failover:** Split institutional block orders across top-ranked LPs to minimize market impact; cascade rejected remainders automatically within the order slippage budget.

## 2. Business rules

- A large order can be split across several banks at the same time.
- Banks are ranked by all-in price first, then by quality (fill rate, hold time, rejections).
- If a bank rejects or does not fill, the remainder moves to the next bank.
- The cascade stops when the total slippage would exceed the client's budget.

## 3. Components already in code (to verify)

| Area | Where | Notes |
|---|---|---|
| Routing | `fx-route-exec-service` · `RouteExecService` | Route-execute with modes and child splits |
| Child splits | `ChildVenueSplit` entity and repository | One row per bank per split |
| Failover API | `POST .../orders/{orderId}/failover` | Manual or automated cascade |
| Events | Kafka `fx.exec.last-look-failover.v1` | Requires `load-kafka` profile |

## 4. Checklist

| # | Item | Status |
|---|---|---|
| 1 | Ranked bank list from LP rankings and LP book | TODO (data not loaded) |
| 2 | Block order split across top N banks | TO VERIFY |
| 3 | Rejected remainder cascades to next bank | TO VERIFY |
| 4 | Cascade stops at slippage budget | TO VERIFY |
| 5 | Every split and failover recorded for audit | TO VERIFY |

## 5. Acceptance test (plain)

1. Rank three banks A, B, C for EUR/USD.
2. Send a 10,000,000 order → split across A and B.
3. Bank A rejects its part → remainder goes to C.
4. Total slippage exceeds the budget → cascade stops and the remainder goes to a dealer (FR-04).
