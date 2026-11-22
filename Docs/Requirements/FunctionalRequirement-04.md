# Functional Requirement FR-04 — Dealing Desk Escalation

**Document ID:** FR-04
**PRD:** [PRD-FX-SOR-001](../../PRD-FX-SOR-001.md) §4 / §4.1
**Owner (primary):** `fx-qualify-service` (+ `fx-admin-service` venue halt)
**Implementation status:** **PARTIAL — not verified.** Escalation flag and admin halt exist in code; no restricted currencies are loaded.
**Business view:** [functional-requirements-business-view](Claude/functional-requirements-business-view.md) §2 F2, F5

---

## 1. Requirement statement

> **FR-04 Dealing Desk Escalation:** Route fat-finger alerts, restricted emerging-market currencies, or zero-liquidity exceptions to NY/London dealing desks with full diagnostic context.

## 2. Business rules

- An order is escalated to a human dealer when:
  - the currency is restricted, or
  - the amount looks like a typing error (fat finger), or
  - no bank has liquidity.
- The dealer receives the order, the reason, and the checks that failed.
- An admin can halt a bank so no orders go to it.

## 3. Components already in code (to verify)

| Area | Where | Notes |
|---|---|---|
| Restricted check | `fx-qualify-service` · `QualifyService` | Failed policy `RESTRICTED_CCY` sets `escalatedToDesk` |
| Venue knock-out event | `emitVenueKnockout` | Emitted on escalation |
| Admin halt | `fx-admin-service` · `POST /api/v1/admin/venues/{lpId}/halt` | Halts one bank |

Restricted-currency data is **not loaded**. Settlement policies currently mark every seeded pair as not restricted.

## 4. Checklist

| # | Item | Status |
|---|---|---|
| 1 | Restricted-currency list from the business (currency, reason, desk) | TODO |
| 2 | Restricted order escalated with `escalatedToDesk` | TO VERIFY |
| 3 | Fat-finger threshold defined by the business and enforced | TODO (threshold not defined) |
| 4 | Zero-liquidity case escalated | TO VERIFY |
| 5 | Dealer view shows the full diagnostic context | TO VERIFY (dealer queue UI not confirmed) |
| 6 | Admin halt stops routing to a bank | TO VERIFY |

## 5. Acceptance test (plain)

1. Mark USD/INR as restricted, owned by the NY desk.
2. Send a USD/INR order → not routed; appears in the NY desk queue with the reason "restricted".
3. Halt bank A → no order goes to bank A until the halt is lifted.
