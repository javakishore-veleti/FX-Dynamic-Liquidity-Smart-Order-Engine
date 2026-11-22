# Functional Requirement FR-02 — Atomic Bilateral Credit & NOP Protection

**Document ID:** FR-02
**PRD:** [PRD-FX-SOR-001](../../PRD-FX-SOR-001.md) §4 / §4.1
**Owner (primary):** `fx-qualify-service` (+ `fx-admin-service` override)
**Implementation status:** **PARTIAL — not verified.** Code classes exist; no end-to-end checklist has been run.
**Business view:** [functional-requirements-business-view](Claude/functional-requirements-business-view.md) §2 F2, F6

---

## 1. Requirement statement

> **FR-02 Atomic Bilateral Credit & NOP Protection:** Verify and reserve counterparty credit lines in Redis using atomic Lua/pipeline operations prior to external order dispatch.

## 2. Business rules

- Before an order goes to a bank, the client's credit with that bank is checked and a reservation is made.
- If the reservation fails, that bank is removed from the candidates for the order.
- An authorised person can override a credit limit; the override is recorded.

## 3. Components already in code (to verify)

| Area | Where | Notes |
|---|---|---|
| Qualification | `fx-qualify-service` · `QualifyService.tryReserve` | Reserves credit per client per bank |
| Credit store | `CreditStore`, `RedisCreditStore`, `InMemoryCreditStore` | Redis path when `load-redis` profile is on |
| Utilisation API | `GET .../credit/utilization/{clientId}` | Read side |
| Override | `fx-admin-service` · `POST /api/v1/admin/credit/override` | Admin override |

Credit lines are currently seeded in code for one default client and three banks. Business-supplied limits are not loaded yet.

## 4. Checklist

| # | Item | Status |
|---|---|---|
| 1 | Credit line per client per bank, from business data | TODO |
| 2 | Reservation is atomic (no double spend under concurrent orders) | TO VERIFY |
| 3 | Failed reservation removes the bank from candidates | TO VERIFY |
| 4 | Release of a reservation when the trade does not proceed | TO VERIFY |
| 5 | Credit override recorded with approver | TO VERIFY |
| 6 | Breach event emitted when utilisation crosses threshold | TO VERIFY |

## 5. Acceptance test (plain)

1. Set a client's limit with bank A to 1,000,000.
2. Send a 600,000 order to bank A → accepted, 600,000 reserved.
3. Send a 500,000 order to bank A → rejected with "credit headroom exhausted"; bank A is not contacted.
4. Cancel the first order → reserved amount returns to 0; the second order now succeeds.
