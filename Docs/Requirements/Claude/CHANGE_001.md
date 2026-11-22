# CHANGE_001 — Docs cleanup, compliance wording, master data, and Initial Data workflows

**Change ID:** CHANGE_001
**Requested by:** Repo owner
**Date opened:** 2026-11-21
**Last updated:** 2026-11-22
**Branch of record:** `main`
**Backup of pre-change state:** `Cursor_Mess_Before_Claude_Fixes` (points at `d01a665`, the `main` tip before this change)
**Rules applied to every feature:** [DESIGN.md](../../../DESIGN.md) and [ARCHITECTURE.md](../../../ARCHITECTURE.md)

**Status key:** `DONE` · `VERIFIED` (run end to end through the API) · `BLOCKED` (needs an input only the business can give) · `DEFERRED` (agreed to be a later change) · `CANCELLED`

---

## 1. Epics and features

| Epic | Feature | Summary | Rules checked | Status | Started | Completed |
|---|---|---|---|---|---|---|
| E1 Docs & repo hygiene | F1.1 Remove duplicate specs | Byte-identical copies removed from `Docs/Backup` and `Docs/Architecture` | DESIGN §8.4 | DONE | 2026-11-21 | 2026-11-21 |
| E1 Docs & repo hygiene | F1.2 Archive superseded drafts | Four unique drafts moved to `Docs/Archive/` | DESIGN §8.4 | DONE | 2026-11-21 | 2026-11-21 |
| E1 Docs & repo hygiene | F1.3 Requirement files FR-02 to FR-05 | One file per FR, FR-01 template, status unverified | DESIGN §8.2 | DONE | 2026-11-21 | 2026-11-21 |
| E1 Docs & repo hygiene | F1.4 Working rules for AI agents | `AGENTS.md` points to DESIGN and ARCHITECTURE | DESIGN §7, §8 | DONE | 2026-11-21 | 2026-11-22 |
| E1 Docs & repo hygiene | F1.5 Ignore local env files | `.gitignore` rules for `.env` and `.env.*` | DESIGN §7.3 | DONE | 2026-11-21 | 2026-11-21 |
| E2 Business view | F2.1 Functional business view | Personas, functions, data, rules, open questions | DESIGN §8.3 | DONE | 2026-11-21 | 2026-11-21 |
| E2 Business view | F2.2 Impact matrix | Per FR: primary and other microservices, portals | DESIGN §8.1 | DONE | 2026-11-21 | 2026-11-21 |
| E3 Compliance & targets | F3.1 Soften compliance wording | Applicability wording in PRD and README | DESIGN §8.5 | DONE | 2026-11-21 | 2026-11-21 |
| E3 Compliance & targets | F3.2 Flag numeric targets | PRD and README figures marked as internal targets | DESIGN §8.5 | DONE | 2026-11-21 | 2026-11-21 |
| E3 Compliance & targets | F3.3 Last-look disclosure requirement | Cross-cutting requirement, FX Global Code Principle 17 | DESIGN §8.5 | DONE | 2026-11-21 | 2026-11-21 |
| E4 Data classification | F4.0 Master vs transactional rule | Master data: committed JSON, read locally. Transactional: owner API only, HTTP 500 if owner down | DESIGN §9 | DONE | 2026-11-22 | 2026-11-22 |
| E5 Currency pair master | F5.1 Committed pair master file | 28 pairs (`pair_code`, `base_ccy`, `quote_ccy`) in `fx-quote-service` resources | DESIGN §9.3, §9.4 | DONE | 2026-11-22 | 2026-11-22 |
| E5 Currency pair master | F5.2 Pair schema in owner DB | `currency_pairs` table with audit columns (quote changeset 005) | DESIGN §4.1, §4.4 | DONE | 2026-11-22 | 2026-11-22 |
| E5 Currency pair master | F5.3 Owner API | `GET /api/v1/fx/pairs`, `GET /api/v1/fx/pairs/{pairCode}`, `POST /api/v1/fx/pairs/load` | DESIGN §2, §3 | DONE | 2026-11-22 | 2026-11-22 |
| E5 Currency pair master | F5.4 Admin workflow `INIT_CURRENCY_PAIRS` | Load and verify tasks under `initdata/tasks/` | DESIGN §2, §3, §5 | DONE | 2026-11-22 | 2026-11-22 |
| E5 Currency pair master | F5.5 End-to-end test of pair setup | Empty → 28 pairs; second run refused with 409; task rows recorded | DESIGN §7.5 | VERIFIED | 2026-11-22 | 2026-11-22 |
| E6 Markup seed removal | F6.1 Remove SYSTEM_SEED markups | Quote changeset 006 deletes startup markup rows; markups only via maker-checker | DESIGN §9.2 | VERIFIED (0 markups at startup) | 2026-11-22 | 2026-11-22 |
| E4 Initial Data workflows | F4.1 Stop calendar auto-load at startup | `@PostConstruct` seed load removed; cache restored from table at startup (read only) | DESIGN §1.5 | DONE | 2026-11-21 | 2026-11-22 |
| E4 Initial Data workflows | F4.2 Calendar seed in admin | Calendar JSON in admin `initial-data/` | DESIGN §4.5 | DONE | 2026-11-21 | 2026-11-21 |
| E4 Initial Data workflows | F4.3 Task execution child table | `wf_task_exec_log`: one row per task, JSON input and output, audit columns | DESIGN §4, §5.1 | DONE | 2026-11-21 | 2026-11-22 |
| E4 Initial Data workflows | F4.4 Workflow rows with purpose | `INIT_CURRENCY_PAIRS` and `INIT_ECONOMIC_CALENDAR` with `purpose = INITIAL_DATA_SETUP` (changeset 006) | DESIGN §4.5, §5 | DONE | 2026-11-21 | 2026-11-22 |
| E4 Initial Data workflows | F4.5 Engine writes one row per task | Input and output recorded for success and failure | DESIGN §5.1, §5.5 | VERIFIED | 2026-11-21 | 2026-11-22 |
| E4 Initial Data workflows | F4.6 Setup start rules | Seed runs need only `triggeredBy` (no typed reason); second run refused with 409 once COMPLETED | DESIGN §5.2, §5.3 | VERIFIED (pairs and calendar) | 2026-11-21 | 2026-11-22 |
| E4 Initial Data workflows | F4.7 Calendar load and verify tasks | `CalLoadTask`, `CalVerifyTask` as `WfTask` beans; calendar rows persisted in quote | DESIGN §2, §3, §1.4 | VERIFIED (8 rows loaded and verified) | 2026-11-21 | 2026-11-22 |
| E4 Initial Data workflows | F4.8 Initial Data API | `GET /api/v1/admin/initial-data`, `POST /api/v1/admin/initial-data/{dataset}/setup` | DESIGN §2, §3 | VERIFIED | 2026-11-21 | 2026-11-22 |
| E4 Initial Data workflows | F4.9 Admin trigger | Run setup button on Workflows → Master List, shown for rows with purpose `INITIAL_DATA_SETUP`. Built and compiled; clicked through the API, not in a browser. The earlier Initial Data page was removed at the owner's request | DESIGN §6 | DONE (button built; API path verified) | 2026-11-22 | 2026-11-22 |
| E4 Initial Data workflows | F4.10 Build and test | Admin and quote compile and pass tests; pair and calendar setups run end to end | DESIGN §7.5 | VERIFIED | 2026-11-21 | 2026-11-22 |
| E4 Initial Data workflows | F4.11 Remaining datasets | Settlement policies done (synthetic). Credit lines, LP top-of-book, LP rankings remain. Owned by other services; need business confirmation of the data | DESIGN §2 | PARTLY DONE (settlement); rest DEFERRED | 2026-11-22 | 2026-11-22 |
| E7 Economic calendar data | F7.1 Calendar source | Authoritative source per event type (Fed FOMC, BLS for NFP, ECB). Needs a source decision and access to the published schedules | DESIGN §9.3 | DONE (FOMC from Fed sources; NFP and ECB SYNTHETIC) | 2026-11-22 | 2026-11-22 |
| E7 Economic calendar data | F7.2 Calendar seed, 20 years back and 5 years forward | Currently the 8-event 2026 seed is loaded and verified. Full history and forward schedule depend on F7.1 | DESIGN §9.3 | DONE (737 events: FOMC real 2006–2027, 2028–2031 and NFP/ECB SYNTHETIC) | 2026-11-22 | 2026-11-22 |
| E8 Business inputs | F8.1 Pair settlement attributes | SYNTHETIC values for 28 pairs (46 SPOT/TOM rows) loaded through `INIT_SETTLEMENT_POLICIES`. Business values replace the file when supplied | DESIGN §9.3 | DONE as SYNTHETIC (verified: 46 rows) | 2026-11-22 | 2026-11-22 |
| E8 Business inputs | F8.2 Open business questions | Seven questions in functional view §6 answered with ASSUMPTIONS (section 7) so the build can proceed | — | ASSUMED (confirm with business) | 2026-11-22 | 2026-11-22 |
| E9 Downstream validation | F9.1 Dependent services validate pair codes | qualify knocks out unknown pairs (UNKNOWN_PAIR); route-exec rejects unknown pairs with 400. Both read a committed copy of the pair master | DESIGN §9.4 | DONE (compiles, existing tests pass) | 2026-11-22 | 2026-11-22 |

---

## 2. Tasks and dates

| Task ID | Feature | Task | Status | Started | Completed | Commit |
|---|---|---|---|---|---|---|
| T-01 | F1.1 | Remove 11 duplicate spec files | DONE | 2026-11-21 | 2026-11-21 | `2e7f1ed` |
| T-02 | F1.2 | Move 4 superseded drafts to `Docs/Archive/` | DONE | 2026-11-21 | 2026-11-21 | `2e7f1ed` |
| T-03 | F1.3 | Write FR-02 to FR-05 files | DONE | 2026-11-21 | 2026-11-21 | `2e7f1ed` |
| T-04 | F1.4 | Write `AGENTS.md` | DONE | 2026-11-21 | 2026-11-21 | `2e7f1ed` |
| T-05 | F1.5 | Add `.env` ignore rules | DONE | 2026-11-21 | 2026-11-21 | `e1fa7dd` (history rewritten) |
| T-06 | F2.1 | Write functional business view | DONE | 2026-11-21 | 2026-11-21 | `db29d94` |
| T-07 | F2.2 | Add impact matrix | DONE | 2026-11-21 | 2026-11-21 | `2e7f1ed` |
| T-08 | F3.1 | Soften compliance wording (PRD, README) | DONE | 2026-11-21 | 2026-11-21 | `3f8f1ab` |
| T-09 | F3.2 | Numeric-target note (PRD, README) | DONE | 2026-11-21 | 2026-11-21 | `3f8f1ab` |
| T-10 | F3.3 | Last-look disclosure requirement | DONE | 2026-11-21 | 2026-11-21 | `3f8f1ab` |
| T-11 | F4.1 | Remove `@PostConstruct` seed load on calendar | DONE | 2026-11-21 | 2026-11-21 | `84c05dc` |
| T-12 | F4.2 | Calendar JSON copied to admin `initial-data/` | DONE | 2026-11-21 | 2026-11-21 | `84c05dc` |
| T-13 | F4.3 | Changelog `003-wf-task-exec-log.yaml` | DONE | 2026-11-21 | 2026-11-21 | `84c05dc` |
| T-14 | F4.4 | Changelog `004-initial-data-workflows.yaml` | DONE | 2026-11-21 | 2026-11-21 | `84c05dc` |
| T-15 | F4.3 | Register changelogs 003 and 004 | DONE | 2026-11-21 | 2026-11-21 | `84c05dc` |
| T-16 | F4.3 | Entity and repository for `WfTaskExecLog` | DONE | 2026-11-22 | 2026-11-22 | `9cc99ac` |
| T-17 | F4.5 | `recordTask` writes child rows | DONE | 2026-11-22 | 2026-11-22 | `9cc99ac` |
| T-18 | F4.6 | Start rules and 409 guard | DONE | 2026-11-22 | 2026-11-22 | `9cc99ac` |
| T-19 | F4.7 | Calendar tasks as `WfTask` beans | DONE | 2026-11-22 | 2026-11-22 | `c208e35` |
| T-20 | F4.8 | `initdata/` package, short names, `InitDataController` | DONE | 2026-11-22 | 2026-11-22 | `c208e35` |
| T-21 | — | `DESIGN.md` and `ARCHITECTURE.md` | DONE | 2026-11-22 | 2026-11-22 | `c208e35` |
| T-22 | F4.9 | Admin Initial Data page built | DONE | 2026-11-22 | 2026-11-22 | `08c958e` |
| T-23 | F4.9 | Initial Data page removed; Reference Data nav restored | DONE | 2026-11-22 | 2026-11-22 | `4342787` |
| T-24 | F4.0 | Data classification in `DESIGN.md` §9 and this file | DONE | 2026-11-22 | 2026-11-22 | `6b6ab93` |
| T-25 | F5.1 | `master/currency-pairs.json` (28 pairs) | DONE | 2026-11-22 | 2026-11-22 | `9374a9f` |
| T-26 | F5.2 | Quote changeset 005 `currency_pairs` | DONE | 2026-11-22 | 2026-11-22 | `4738dbe` |
| T-27 | F6.1 | Quote changeset 006 removes SYSTEM_SEED markups | DONE | 2026-11-22 | 2026-11-22 | `4738dbe` |
| T-28 | F5.3 | Entity, repository, facade, DTO, controller for pairs | DONE | 2026-11-22 | 2026-11-22 | `64d2792` |
| T-29 | F5.4 | `INIT_CURRENCY_PAIRS`: catalog, steps, load and verify tasks | DONE | 2026-11-22 | 2026-11-22 | `b857214` |
| T-30 | F5.5 | End-to-end pair setup through the API | DONE | 2026-11-22 | 2026-11-22 | test run only |
| T-31 | F4.0 | Update CHANGE_001 with pair master | DONE | 2026-11-22 | 2026-11-22 | `f655dc2` |
| T-32 | F4.6 | Remove typed reason from seed starts; add the two missing BPMN definitions | DONE | 2026-11-22 | 2026-11-22 | `b43b8da` |
| T-33 | F4.7 | Calendar persisted: changeset 007, `CalendarEvent` entity and repository, facade writes through | DONE | 2026-11-22 | 2026-11-22 | `8c38921` |
| T-34 | F4.9 | Run setup button on Workflows Master List for seed rows | DONE | 2026-11-22 | 2026-11-22 | `3470995` |
| T-35 | F4.4 | `purpose` column on `workflows` (changeset 006); engine and portal filter by purpose | DONE | 2026-11-22 | 2026-11-22 | `c0afc58` |
| T-36 | F4.7 | End-to-end calendar setup: 8 rows loaded and verified; repeat run returns 409 | DONE | 2026-11-22 | 2026-11-22 | test run only |
| T-37 | F4.11 | Other datasets | DEFERRED | | | |
| T-38 | F7.1 | Choose calendar source per event type | DONE | 2026-11-22 | 2026-11-22 | `53356f0` |
| T-39 | F7.2 | 20-year history and 5-year forward calendar seed, from generator and Fed sources | DONE | 2026-11-22 | 2026-11-22 | `53356f0`, `(calendar re-run)` |
| T-40 | F8.1 | Pair settlement attributes (SYNTHETIC until business values arrive) | DONE | 2026-11-22 | 2026-11-22 | `53356f0`, `ce131da` |
| T-41 | F8.2 | Business questions answered with assumptions (section 7) | DONE (assumed) | 2026-11-22 | 2026-11-22 | `f190518` |
| T-42 | F9.1 | Pair-code validation in qualify (UNKNOWN_PAIR) and route-exec (400) | DONE | 2026-11-22 | 2026-11-22 | `809b3f3`, `d9e64fd` |
| T-45 | F4.11 | Credit lines: startup seed removed from qualify; committed `master/credit-lines.json`; `INIT_CREDIT_LINES` workflow (admin changeset 008); end to end: 3 loaded and verified, repeat 409 | DONE | 2026-11-22 | 2026-11-22 | `2ad1e4c` |
| T-43 | F8.1 | Settlement seed (SYNTHETIC, 46 rows): generator, qualify changeset 003 removes startup EURUSD seed, qualify load endpoint, admin workflow `INIT_SETTLEMENT_POLICIES` | DONE | 2026-11-22 | 2026-11-22 | `53356f0`, `ce131da`, `982941f` |
| T-44 | F4.6 | Purpose column: seeds start on `purpose`, not key prefix | DONE | 2026-11-22 | 2026-11-22 | `c0afc58` |

---

## 3. Impact matrix

"None" means the item does not touch that area.

| Feature | Microservices impacted | Portals impacted | Containers impacted | DB tables impacted | Kafka events impacted | Redis cache impacted |
|---|---|---|---|---|---|---|
| F1.x, F2.x, F3.x, F4.0 (docs, wording) | None | None | None | None | None | None |
| F4.1 Calendar load removed at startup | `fx-quote-service` | None | `fx-quote-service` | None | None | None |
| F4.2 Calendar seed in admin | `fx-admin-service` | None | `fx-admin-service` | None | None | None |
| F4.3 `wf_task_exec_log` | `fx-admin-service` | None | `dlp_sor_system_db` | **New:** `wf_task_exec_log` | None | None |
| F4.4 `workflows.purpose` | `fx-admin-service` | Admin Workflows Master List | `dlp_sor_system_db` | `workflows` (new column, two rows set) | None | None |
| F4.5, F4.6 Engine and start rules | `fx-admin-service` | None | `fx-admin-service` | `wf_exec_log`, `wf_task_exec_log` | None | None |
| F4.7 Calendar tasks and persistence | `fx-admin-service`, `fx-quote-service` | None | Both | `wf_exec_log`, `wf_task_exec_log`; **new:** `economic_calendar_events` (quote) | None | None |
| F4.8 Initial Data API | `fx-admin-service` | None | `fx-admin-service` | `wf_exec_log`, `wf_task_exec_log` | None | None |
| F4.9 Run setup button | None (calls admin API) | **Admin** (Workflows → Master List) | `fx-admin-portal` | None | None | None |
| F5.1 Pair master file | `fx-quote-service` | None | `fx-quote-service` image | None | None | None |
| F5.2 `currency_pairs` | `fx-quote-service` | None | `fx_quote_db` | **New:** `currency_pairs` | None | None |
| F5.3 Pair API | `fx-quote-service` (owner) | None yet | `fx-quote-service` | `currency_pairs` (read) | None | None |
| F5.4 Pair workflow | `fx-admin-service`, `fx-quote-service` | Admin (button) | Both services | `wf_exec_log`, `wf_task_exec_log`, `workflows`, `currency_pairs` | None | None |
| F6.1 Markup seed removed | `fx-quote-service` | None | `fx_quote_db` | `client_tier_spread_schedules` (seed rows deleted) | None | None |
| F7.x Calendar data | `fx-quote-service` | None | Same | `economic_calendar_events` | None | None |
| F9.1 Downstream pair validation | `fx-qualify-service`, `fx-route-exec-service` (later) | None | Both | Pair code column (later) | None | Possibly, for cached master |

---

## 4. Commits and branch history

| Commit | Branch | Content | Notes |
|---|---|---|---|
| `db29d94` | `docs/functional-requirements-business-view` | Functional business view | Author trailer removed (history rewritten) |
| `6224da7` | same | Spread-schedule admin pages (earlier work) | Author trailer removed |
| `e1fa7dd` | same | `.env` ignore rules | Author trailer removed |
| `d01a665` | `main` | Merge of PR #1 | Merged on GitHub |
| `2e7f1ed` | `main` | Docs cleanup, FR-02 to FR-05, `AGENTS.md`, impact matrix | |
| `3f8f1ab` | `main` | Compliance wording, target notes, last-look requirement | |
| `84c05dc` | `main` | Calendar auto-load removed; task log schema; calendar workflow row | |
| `9cc99ac` | `main` | Task log rows; start rules; calendar workflow; API | |
| `c208e35` | `main` | `initdata/` package, short names, `WfTask` beans, DESIGN, ARCHITECTURE | |
| `08c958e` | `main` | Admin Initial Data page | Removed in next commit |
| `4342787` | `main` | Initial Data page removed; Reference Data nav restored | |
| `6b6ab93` | `main` | Data classification (DESIGN §9), decisions | |
| `9374a9f` | `main` | Committed currency pair master JSON | |
| `4738dbe` | `main` | Quote changesets 005 (currency pairs) and 006 (remove seeded markups) | |
| `64d2792` | `main` | Quote pair entity, repository, facade, controller | |
| `b857214` | `main` | Admin `INIT_CURRENCY_PAIRS` workflow | |
| `f655dc2` | `main` | CHANGE_001 update with pair master work | |
| `b43b8da` | `main` | Typed reason removed; BPMN definitions added | |
| `8c38921` | `main` | Calendar persisted in quote (changeset 007) | |
| `3470995` | `main` | Run setup button on Workflows Master List | |
| `c0afc58` | `main` | `purpose` column; engine and portal filter by purpose | |

---

## 5. Decisions

| Date | Decision | Detail |
|---|---|---|
| 2026-11-22 | Two data kinds | Master (initial) data is committed JSON. Transactional data is owner API only; HTTP 500 if owner is down. (DESIGN §9) |
| 2026-11-22 | Markups are not initial data | Startup markup seed removed by changeset 006. Markups come through maker-checker. |
| 2026-11-22 | Pair master owner | `fx-quote-service`, keyed by `pair_code`. |
| 2026-11-22 | Pair set | 28 pairs from the Synthetic Data Catalog spec §1. Settlement attributes are not in the spec. |
| 2026-11-22 | Trigger placement | Run setup button on Workflows → Master List, for rows with `purpose = INITIAL_DATA_SETUP`. No separate Initial Data page. |
| 2026-11-22 | Filter by purpose | Seed workflows are identified by `workflows.purpose`, not by key prefix. |
| 2026-11-22 | Seed starts take no typed input | Only `triggeredBy` is recorded. |
| 2026-11-22 | Calendar persistence | Calendar windows live in `economic_calendar_events`; the in-memory list is a cache restored at startup. |
| 2026-11-22 | Pairs before calendar | The calendar depends on the pair master. |
| 2026-11-22 | Calendar seed for now | The committed calendar is the existing 8-event 2026 seed. The 20-year history and 5-year forward dataset is blocked on F7.1, not invented. |
| 2026-11-22 | Remaining datasets | Deferred to a later change, pending business confirmation. |
| 2026-11-22 | Old Reference Data pages | Left as they are. They remain routable but not in the nav. |

---

## 6. Open items and blockers

| # | Item | Blocked by | What unblocks it |
|---|---|---|---|
| 1 | Calendar source per event type (F7.1) | A decision on which published schedule to use for FOMC, NFP, and ECB | Choose the sources |
| 2 | 20-year history and 5-year forward calendar seed (F7.2) | Item 1 | Published schedules for each event type |
| 3 | Pair settlement attributes (F8.1) | Business data: CLS eligibility, cutoff, clip sizes, restricted flag per pair | Business supplies the values |
| 4 | Seven business questions (F8.2) | Business answers | Functional view §6 answered |
| 5 | Remaining datasets (F4.11) | Business confirmation of settlement policies, credit lines, LP top-of-book, LP rankings | Business confirms data |
| 6 | Pair-code validation in qualify and route-exec (F9.1) | Follows item 3 for those services' data | Start after item 3 |
| 7 | Button tested in a browser | Not done; the button was built and its API path verified | A click-through in the admin portal |
| 8 | Admin layout is flat | DESIGN §2 defines rules for new code | None; a restructure would be a separate change |

---

## 7. Assumptions for the open business questions (F8.2)

These answers are the repo owner's delegated decisions, so the build can continue. Each one must be confirmed or replaced by the business. Nothing here is a business sign-off.

| # | Question (functional view §6) | Assumption used in the build | Where it is used |
|---|---|---|---|
| 1 | Which pairs and tiers are in scope for the first release? | All 28 pairs in the spec §1; tiers as the spec defines them | Pair master, settlement seed |
| 2 | Who sets markups and how often do they change? | The pricing desk, through maker-checker. Not part of initial data | Quote spread schedules |
| 3 | What is the slippage limit for a failed or partly filled trade? | **Placeholder: 5 bps.** Not yet in the code | Route-exec failover budget (to add) |
| 4 | Which currencies are restricted, and which dealer handles each? | USD/INR and USD/BRL restricted; NY dealing desk owns both | Settlement seed `restrictedFlag` |
| 5 | Who can override a credit limit, and does it need a second approver? | An admin, with a second approver (four eyes) | Credit override (existing admin endpoint) |
| 6 | Which banks are live liquidity providers per pair, and who maintains their ratings? | The 12 LPs in the Synthetic Data Catalog §2; ratings maintained by the risk desk | LP book and rankings (later dataset) |
| 7 | Are the event dates and severities confirmed? | No. FOMC 2021–2027 from the Federal Reserve calendar; FOMC 2006–2020 from the Hugging Face scrape of the Fed site where loaded; NFP and ECB labelled SYNTHETIC | Calendar seed |

## 8. Calendar data, final state

| Event | Source | Labelling |
|---|---|---|
| FOMC 2021–2027 | Federal Reserve FOMC calendar (pasted page) | Real dates; 2027 marked [TENTATIVE] |
| FOMC 2006–2020 | Hugging Face `vtasca/fomc-statements-minutes`, a scrape of the Fed site | Real dates once the CSV is read; otherwise SYNTHETIC |
| NFP, all years | Generated: first Friday of each month | [SYNTHETIC] |
| ECB, all years | Generated: fixed week-of-year slots | [SYNTHETIC] |
| Settlement policies | Generated for 28 pairs | SYNTHETIC file name `settlement-policies.synthetic.json` |

Replacing synthetic data with business or published data is a file change: regenerate or edit the committed JSON, then run the setup again on a fresh environment.

## 9. Final state (2026-11-22)

| Area | State | Verification |
|---|---|---|
| Currency pair master (28 pairs) | DONE | Setup run through the API: 28 loaded, 28 found |
| Settlement policies (46 rows, SYNTHETIC) | DONE | Setup run through the API: 46 loaded, 46 found |
| Economic calendar (737 events) | DONE | Setup run through the API: 737 upserted, 737 found |
| Markup seed removal | DONE | 0 markups after restart |
| Trigger for each setup | DONE | Run setup button on Workflows → Master List (purpose `INITIAL_DATA_SETUP`) |
| Repeat setup refused | DONE | HTTP 409 for pairs, settlement, and calendar |
| Pair-code validation | DONE | qualify: UNKNOWN_PAIR knock-out; route-exec: HTTP 400 |
| Typed reason removed | DONE | Seed starts need only `triggeredBy` |
| BPMN definitions | DONE | Three files: pairs, calendar, settlement |
| Calendar persisted | DONE | `economic_calendar_events` in quote (changeset 007) |
| Business assumptions | ASSUMED | Section 7 |

### Still open (real gaps, not hidden)
1. **NFP and ECB dates are SYNTHETIC.** Replace with published schedules by editing the committed calendar file.
2. **Settlement values are SYNTHETIC.** Replace with business values in the committed file.
3. **Slippage limit is a placeholder (5 bps)** and is not yet enforced in route-exec.
4. CLOSED: credit-line startup seed moved to `master/credit-lines.json` and the `INIT_CREDIT_LINES` workflow (verified: 3 loaded, 3 found, repeat 409).
5. **Remaining datasets (F4.11):** credit lines, LP top-of-book, LP rankings. Each needs its own file and loader.
6. **Browser click-through** of the Run setup button has not been done. The API path is verified.

## 10. Stored source files (no re-download)

| Dataset | Stored at (outside the repo) | Used by |
|---|---|---|
| FOMC statements and minutes (Hugging Face `vtasca/fomc-statements-minutes`) | `~/kishore/runtime_data/Github_projects/Datasets/FMOC-Meeings/2026-11-21-23-07-36/communications.csv` (with `SOURCE.md`) | `generate_synthetic_calendar.py --fomc-csv <path>` |

New downloads go to `FMOC-Meeings/YYYY-MM-DD-HH-MM-SS/`, one folder per download, with a `SOURCE.md` recording the URL and date.

## 11. Progress log

| Date | Change | Commit |
|---|---|---|
| 2026-11-22 | Credit lines moved to committed master data and workflow; qualify startup seed removed | `2ad1e4c` |
| 2026-11-22 | LP top-of-book starter moved to committed master data; startup seed and hidden EURUSD fallback removed in quote; `INIT_LP_BOOK` workflow (changeset 009) | `dd47688` (quote), admin commit below |
