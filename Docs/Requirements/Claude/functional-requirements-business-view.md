# Functional View: FX Dynamic Liquidity & Smart Order Routing

**Source:** `PRD-FX-SOR-001.md` (FR-01 to FR-05) and `Docs/Requirements/FunctionalRequirement-01.md`
**Purpose:** Business-level view only. It describes who uses the system, what they do, what data exists, and what rules apply. It does not describe code or services.
**Status:** Draft for business review. Open questions are in section 6.

---

## 1. Personas

| Persona | Who they are | What they need |
|---|---|---|
| **Corporate treasurer** | Finance staff at a large company (importer, exporter, fund) | A fair price, trade a currency amount, see the trade settle |
| **Retail customer** | Individual sending money abroad | Convert a small amount quickly at a clear price |
| **Relationship manager (sales)** | Owns the client relationship | See a client's tier and special pricing, check what the client may do |
| **Dealing desk trader** | Human trader in New York, London, or Singapore | Handle exceptions: restricted currencies, unusual orders, no liquidity |
| **Credit / risk officer** | Watches limits and exposure | Set and monitor credit limits per client and per bank; approve overrides |
| **Policy owner (maker)** | Business user who changes rules | Draft a change to pricing, limits, or restrictions |
| **Policy approver (checker)** | Second person who signs off | Approve or reject a change before it goes live |
| **Operations / audit** | Compliance and post-trade review | Prove why each trade was handled the way it was |
| **Market / macro analyst** | Tracks economic events | Record announcement dates that change the rules |
| **Liquidity provider (external bank)** | The bank a trade is sent to | Receives orders; may accept or reject; has its own limits |

---

## 2. Business functions

| # | Function | What it means in business terms | Requirement |
|---|---|---|---|
| F1 | **Price a request** | Build a client price from the market price plus the bank's markup for that client, currency pair, and market session | Pillar: Margin |
| F2 | **Qualify a request** | Decide whether the trade is allowed: pair supported, amount in range, before settlement cutoff, credit available, currency not restricted, price not stale | Pillar: Execution certainty |
| F3 | **Choose the bank(s)** | Rank the available liquidity providers by all-in price and quality, and pick the best | FR-03 |
| F4 | **Split and fail over** | Split large trades across banks; move unfilled or rejected parts to the next bank within the slippage budget | FR-03 |
| F5 | **Escalate to a dealer** | Send unusual cases to a human dealer with full context | FR-04 |
| F6 | **Reserve credit** | Hold part of a client's credit with a bank before sending the trade; release it if the trade does not go ahead | FR-02 |
| F7 | **Adjust for market events** | Widen markups and last-look penalties during NY rollover and big announcements (FOMC, NFP, ECB) | FR-01 |
| F8 | **Govern changes** | Draft, review, approve, and publish rule changes, with a record of each step | FR-05 |
| F9 | **Explain the decision** | Keep a record of every pricing and routing choice, including the alternatives considered | Pillar: Regulatory provenance |

---

## 3. Data

### 3.1 Reference data (set up once, changed rarely)

| Data set | What it holds | Used by |
|---|---|---|
| Currency pairs and settlement rules | Pair, cutoff time, minimum and maximum amount, whether it settles through CLS | F2 |
| Restricted currencies | Currency, reason, dealer who handles it | F2, F5 |
| Clients and tiers | Client, tier (institutional, corporate, retail), home desk | F1, F2 |
| Liquidity providers | Bank name, pairs and sizes it covers, quality profile | F3, F4 |
| Credit limits | Limit per client per bank; overall exposure cap | F2, F6 |
| Market sessions | Start and end times for Tokyo, London, London/NY overlap, NY afternoon, NY rollover | F1, F7 |
| Economic calendar | Event (FOMC, NFP, ECB), date and time window, severity | F7 |
| Markup table | Client tier × currency pair × session → markup and event multiplier | F1, F7 |
| Staff users and roles | Name, role (maker, checker, dealer, credit officer) | F5, F6, F8 |

### 3.2 Transaction data (changes through the day)

- Price requests and their outcome.
- Orders, how each was split, and what each bank filled or rejected.
- Credit currently in use, per client per bank.
- Bank performance: fill rate, hold time, rejection rate.

### 3.3 Audit and history

- Every rule change: who drafted it, who approved it, when, and the value before.
- Every routing decision: the bank chosen and why, with the alternatives.
- Every workflow run and every human step taken.

---

## 4. Business rules

| Rule | Plain statement |
|---|---|
| **Markup** | Client price = market price + markup for the client's tier, pair, and session; during a big announcement, the event multiplier also applies |
| **Session** | The current session is decided by the time of day in each market |
| **Event window** | Event-driven widening only starts after a human has confirmed the event; nothing changes automatically without a person |
| **Knock-out** | A trade is rejected if any one check fails: unsupported pair, amount out of range, past cutoff, credit exhausted, restricted currency, stale price |
| **Credit reservation** | Credit is reserved before the trade goes to a bank; if the reservation fails, that bank is removed from the candidates |
| **Ranking** | Banks are ranked by all-in price first, then adjusted for quality (fill rate, hold time, rejection rate) |
| **Split** | Large orders are split across the top-ranked banks |
| **Failover** | Unfilled or rejected parts move to the next bank, only while total slippage stays within the client's limit |
| **Escalation** | Restricted currency, unusual amount, or no liquidity → send to the dealing desk |
| **Four eyes** | Any change to pricing, limits, or restrictions needs a second person to approve; the maker cannot approve their own change |

---

## 5. Current state (what the repo holds today)

This is a business-level read of the seed and reference data. It is not a technical review.

| Area | Today | Gap |
|---|---|---|
| Currency pairs | EURUSD only | Spec describes 28 pairs |
| Markups | EURUSD only, four client tiers named differently from the spec | Needs one agreed tier list and full pair coverage |
| Settlement rules | EURUSD only | Same as above |
| Restricted currencies | None set (EURUSD marked not restricted) | FR-04 cannot be tested without them |
| Credit limits | Three banks and one default client, hardcoded | Needs real client-by-bank limits from the business |
| Liquidity providers | Not set up as business data | FR-03 needs a provider list with quality profiles |
| Economic calendar | 2026 FOMC, NFP, and ECB dates present | Confirm dates and severity with the business |
| Staff users and roles | Not reviewed | FR-05 needs maker and checker roles |
| Local data persistence | Resets on restart by default | Business test data needs a persistent option |

---

## 6. Open questions for the business owner

1. Which currency pairs and client tiers are in scope for the first release?
2. Who sets the markup numbers, and how often do they change?
3. What is the slippage limit for a failed or partly filled trade?
4. Which currencies are restricted, and which dealer handles each?
5. Who can override a credit limit, and does an override need a second approver?
6. Which banks are live liquidity providers for each pair, and who maintains their quality ratings?
7. Are the economic event dates and severity levels confirmed by the business?

---

## 7. Impact by requirement: services and portals

"Primary" is the service that owns the requirement. "Impacted" services and portals need a change or a display for it to work. Items marked **(confirm)** are inferred from the PRD and need business or architecture sign-off.

| FR | Functions | Main data | Main rules | Primary microservice | Other microservices impacted | Portals impacted |
|---|---|---|---|---|---|---|
| FR-01 Session & regime guardrails | F1, F7, F8 | Markup table, sessions, economic calendar | Markup, session, event window, four eyes | `fx-quote-service` | `fx-admin-service` (macro workflow, arm/clear); `fx-route-exec-service` (last-look penalty); `fx-agent-orchestrator-service` (MacroGuard assist) | **Admin:** Policy Studio, markup schedules, economic calendar, workflows. **Corporate:** blotter shows regime and markup. **Retail:** convert shows regime and markup. |
| FR-02 Credit & exposure | F2, F6 | Credit limits, clients | Knock-out (credit), credit reservation | `fx-qualify-service` | `fx-admin-service` (credit lines, credit override); `fx-route-exec-service` (uses reservation result, **confirm**) | **Admin:** credit lines, credit override. **Corporate:** shows credit reject reason (**confirm**). |
| FR-03 Split & failover | F3, F4 | Liquidity providers, quality data | Ranking, split, failover | `fx-route-exec-service` | `fx-quote-service` (prices feed ranking); `fx-qualify-service` (knock-outs feed candidates); `fx-admin-service` (LP book, LP rankings) | **Corporate:** blotter and execution views show child splits. **Admin:** LP book, LP rankings, TCA. |
| FR-04 Dealing desk escalation | F2, F5 | Restricted currencies, dealers | Knock-out (restricted), escalation | `fx-qualify-service` | `fx-admin-service` (venue halt, dealer queue); `fx-route-exec-service` (escalated remainder, **confirm**) | **Admin:** dealing desk queue, venue halt. **Corporate and Retail:** show escalation status to the client (**confirm**). |
| FR-05 Policy authoring lifecycle | F8, F9 | Staff roles, change history | Four eyes | `fx-admin-service` | `fx-quote-service` (spread schedule draft, submit, approve, live cache); `fx-agent-orchestrator-service` (policy authoring assist) | **Admin:** Policy Studio, workflows master list and executions. |
| Cross-cutting: Initial Data setup (new) | — | Reference data for all services | Setup once, by button only | `fx-admin-service` | `fx-quote-service`, `fx-qualify-service`, `fx-route-exec-service` (remove auto-seeds, accept loads) | **Admin:** Initial Data. |

**Not impacted by these five requirements:** `fx-topology-service`, `fx-topology-mcp-server`. They support observability and root-cause analysis only.
