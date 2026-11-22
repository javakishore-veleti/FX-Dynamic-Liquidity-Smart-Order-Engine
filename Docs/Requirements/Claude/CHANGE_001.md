# CHANGE_001 — Docs cleanup, compliance wording, master data, and Initial Data workflows

**Change ID:** CHANGE_001
**Requested by:** Repo owner
**Date opened:** 2026-11-21
**Last updated:** 2026-11-22
**Branch of record:** `main`
**Backup of pre-change state:** `Cursor_Mess_Before_Claude_Fixes` (points at `d01a665`, the `main` tip before this change)
**Rules applied to every feature:** [DESIGN.md](../../../DESIGN.md) and [ARCHITECTURE.md](../../../ARCHITECTURE.md)

**Status key:** `DONE` · `VERIFIED` (run end to end, not only compiled) · `IN PROGRESS` · `TODO` · `DEFERRED` · `CANCELLED`

---

## 1. Epics and features

| Epic | Feature | Summary | Rules checked | Status | Started | Completed |
|---|---|---|---|---|---|---|
| E1 Docs & repo hygiene | F1.1 Remove duplicate specs | Delete byte-identical copies in `Docs/Backup` and `Docs/Architecture` | DESIGN §8.4 | DONE | 2026-11-21 | 2026-11-21 |
| E1 Docs & repo hygiene | F1.2 Archive superseded drafts | Four unique Backup drafts moved to `Docs/Archive/` | DESIGN §8.4 | DONE | 2026-11-21 | 2026-11-21 |
| E1 Docs & repo hygiene | F1.3 Requirement files FR-02 to FR-05 | One file per FR, FR-01 template, status unverified | DESIGN §8.2 | DONE | 2026-11-21 | 2026-11-21 |
| E1 Docs & repo hygiene | F1.4 Working rules for AI agents | `AGENTS.md` at repo root, points to DESIGN and ARCHITECTURE | DESIGN §7, §8 | DONE | 2026-11-21 | 2026-11-22 |
| E1 Docs & repo hygiene | F1.5 Ignore local env files | `.gitignore` rules for `.env` and `.env.*` | DESIGN §7.3 | DONE | 2026-11-21 | 2026-11-21 |
| E2 Business view | F2.1 Functional business view | Personas, functions, data, rules, open questions | DESIGN §8.3 | DONE | 2026-11-21 | 2026-11-21 |
| E2 Business view | F2.2 Impact matrix | Per FR: primary microservice, other microservices, portals | DESIGN §8.1 | DONE | 2026-11-21 | 2026-11-21 |
| E3 Compliance & targets | F3.1 Soften compliance wording | "100% compliance" replaced with applicability wording (PRD, README) | DESIGN §8.5 | DONE | 2026-11-21 | 2026-11-21 |
| E3 Compliance & targets | F3.2 Flag numeric targets | PRD and README figures marked as internal targets | DESIGN §8.5 | DONE | 2026-11-21 | 2026-11-21 |
| E3 Compliance & targets | F3.3 Last-look disclosure requirement | Cross-cutting requirement citing FX Global Code Principle 17 | DESIGN §8.5 | DONE | 2026-11-21 | 2026-11-21 |
| E4 Data classification | F4.0 Master vs transactional rule | Master data: committed JSON, read locally by dependents. Transactional: owner API only, HTTP 500 if owner down | DESIGN §9 | DONE | 2026-11-22 | 2026-11-22 |
| E5 Currency pair master | F5.1 Committed pair master file | 28 pairs (`pair_code`, `base_ccy`, `quote_ccy`), in `fx-quote-service` resources | DESIGN §9.3, §9.4 | DONE | 2026-11-22 | 2026-11-22 |
| E5 Currency pair master | F5.2 Pair schema in owner DB | `currency_pairs` table with audit columns (changeset 005) | DESIGN §4.1, §4.4 | DONE | 2026-11-22 | 2026-11-22 |
| E5 Currency pair master | F5.3 Owner API | `GET /api/v1/fx/pairs`, `GET /api/v1/fx/pairs/{pairCode}`, `POST /api/v1/fx/pairs/load` | DESIGN §2, §3 | DONE | 2026-11-22 | 2026-11-22 |
| E5 Currency pair master | F5.4 Admin workflow `INIT_CURRENCY_PAIRS` | Load task and verify task under `initdata/tasks/` | DESIGN §2, §3, §5 | DONE | 2026-11-22 | 2026-11-22 |
| E5 Currency pair master | F5.5 End-to-end test of pair setup | Empty → 28 pairs; second run refused with 409; task rows recorded | DESIGN §7.5 | VERIFIED | 2026-11-22 | 2026-11-22 |
| E6 Markup seed removal | F6.1 Remove SYSTEM_SEED markups | Changeset 006 deletes startup markup rows; markups created through maker-checker only | DESIGN §9.2 | VERIFIED (0 markups at startup) | 2026-11-22 | 2026-11-22 |
| E4 Initial Data workflows | F4.1 Stop calendar auto-load at startup | `@PostConstruct` removed from `EconomicCalendarFacade`; method kept for its unit test | DESIGN §1.5 | DONE | 2026-11-21 | 2026-11-21 |
| E4 Initial Data workflows | F4.2 Calendar seed in admin | Calendar JSON copied to admin `initial-data/` | DESIGN §4.5 | DONE | 2026-11-21 | 2026-11-21 |
| E4 Initial Data workflows | F4.3 Task execution child table | `wf_task_exec_log`: one row per task, JSON input and output, audit columns | DESIGN §4, §5.1 | DONE | 2026-11-21 | 2026-11-22 |
| E4 Initial Data workflows | F4.4 Workflow definition rows | `INIT_ECONOMIC_CALENDAR` and `INIT_CURRENCY_PAIRS` in master list | DESIGN §4.5 | DONE | 2026-11-21 | 2026-11-22 |
| E4 Initial Data workflows | F4.5 Engine writes one row per task | Input and output recorded for success and failure | DESIGN §5.1, §5.5 | DONE | 2026-11-21 | 2026-11-22 |
| E4 Initial Data workflows | F4.6 Initial Data start rules | `INIT_*` start needs reason and triggeredBy only; second run refused with 409 once COMPLETED | DESIGN §5.2, §5.3 | VERIFIED (pairs) | 2026-11-21 | 2026-11-22 |
| E4 Initial Data workflows | F4.7 Calendar load and verify tasks | `CalLoadTask`, `CalVerifyTask` as `WfTask` beans | DESIGN §2, §3, §1.4 | DONE (compiles; not yet run end to end) | 2026-11-21 | 2026-11-22 |
| E4 Initial Data workflows | F4.8 Initial Data API | `GET /api/v1/admin/initial-data`, `POST /api/v1/admin/initial-data/{dataset}/setup` | DESIGN §2, §3 | VERIFIED (pairs) | 2026-11-21 | 2026-11-22 |
| E4 Initial Data workflows | F4.9 Admin portal Initial Data page | Built, then removed at owner's request. Trigger placement not yet decided | DESIGN §6 | CANCELLED (page removed, commit `4342787`) | 2026-11-21 | 2026-11-22 |
| E4 Initial Data workflows | F4.10 Build and test | Admin and quote compile and pass existing tests; pair flow tested end to end | DESIGN §7.5 | IN PROGRESS | 2026-11-21 | |
| E4 Initial Data workflows | F4.11 Remaining datasets | Settlement policies, credit lines, LP top-of-book, LP rankings | DESIGN §2 | DEFERRED (business to confirm) | | |
| E7 Economic calendar data | F7.1 Calendar source | Authoritative source per event type (Fed FOMC, BLS for NFP, ECB) | DESIGN §9 | PENDING (decision needed) | | |
| E7 Economic calendar data | F7.2 Calendar seed (20 years back, 5 years forward) | Committed JSON, loaded by `INIT_ECONOMIC_CALENDAR` | DESIGN §9.3 | PENDING (depends on F7.1) | | |
| E8 Business inputs | F8.1 Pair settlement attributes | CLS eligibility, cutoff, clip sizes, restricted flag per pair | — | PENDING (business input) | | |
| E8 Business inputs | F8.2 Open business questions | Seven questions in functional view §6 | — | PENDING (business input) | | |

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
| T-11 | F4.1 | Remove `@PostConstruct` on calendar loader | DONE | 2026-11-21 | 2026-11-21 | `84c05dc` |
| T-12 | F4.2 | Calendar JSON copied to admin `initial-data/` | DONE | 2026-11-21 | 2026-11-21 | `84c05dc` |
| T-13 | F4.3 | Changelog `003-wf-task-exec-log.yaml` | DONE | 2026-11-21 | 2026-11-21 | `84c05dc` |
| T-14 | F4.4 | Changelog `004-initial-data-workflows.yaml` | DONE | 2026-11-21 | 2026-11-21 | `84c05dc` |
| T-15 | F4.3 | Register changelogs 003 and 004 | DONE | 2026-11-21 | 2026-11-21 | `84c05dc` |
| T-16 | F4.3 | Entity and repository for `WfTaskExecLog` | DONE | 2026-11-22 | 2026-11-22 | `9cc99ac` |
| T-17 | F4.5 | `recordTask` writes child rows | DONE | 2026-11-22 | 2026-11-22 | `9cc99ac` |
| T-18 | F4.6 | `validateInitialDataStart`; 409 guard | DONE | 2026-11-22 | 2026-11-22 | `9cc99ac` |
| T-19 | F4.7 | Calendar tasks as `WfTask` beans | DONE | 2026-11-22 | 2026-11-22 | `c208e35` |
| T-20 | F4.8 | `initdata/` package, short names, `InitDataController` | DONE | 2026-11-22 | 2026-11-22 | `c208e35` |
| T-21 | — | `DESIGN.md` and `ARCHITECTURE.md` | DONE | 2026-11-22 | 2026-11-22 | `c208e35` |
| T-22 | F4.9 | Admin portal Initial Data page built | DONE | 2026-11-22 | 2026-11-22 | `08c958e` |
| T-23 | F4.9 | Initial Data page removed at owner's request; Reference Data nav restored | DONE | 2026-11-22 | 2026-11-22 | `4342787` |
| T-24 | F4.0 | Data classification in `DESIGN.md` §9 and this file | DONE | 2026-11-22 | 2026-11-22 | `6b6ab93` |
| T-25 | F5.1 | `master/currency-pairs.json` (28 pairs) | DONE | 2026-11-22 | 2026-11-22 | `9374a9f` |
| T-26 | F5.2 | Changeset 005 `currency_pairs` | DONE | 2026-11-22 | 2026-11-22 | `4738dbe` |
| T-27 | F6.1 | Changeset 006 removes SYSTEM_SEED markups | DONE | 2026-11-22 | 2026-11-22 | `4738dbe` |
| T-28 | F5.3 | Entity, repository, facade, DTO, controller for pairs | DONE | 2026-11-22 | 2026-11-22 | `64d2792` |
| T-29 | F5.4 | `INIT_CURRENCY_PAIRS` workflow: catalog, steps, load and verify tasks, changeset 005 (admin) | DONE | 2026-11-22 | 2026-11-22 | `b857214` |
| T-30 | F5.5 | End-to-end test through the API (empty → 28; second run 409; task rows) | DONE | 2026-11-22 | 2026-11-22 | — (test only) |
| T-31 | F4.9 | Admin trigger UI | TODO (placement not decided) | | | |
| T-32 | F4.7 | End-to-end run of `INIT_ECONOMIC_CALENDAR` | TODO (needs F7.2 data) | | | |
| T-33 | F4.11 | Remaining datasets | DEFERRED | | | |
| T-34 | F7.1 | Choose calendar source per event type | PENDING | | | |

---

## 3. Impact matrix

"None" means the item does not touch that area.

| Feature | Microservices impacted | Portals impacted | Containers impacted | DB tables impacted | Kafka events impacted | Redis cache impacted |
|---|---|---|---|---|---|---|
| F1.x, F2.x, F3.x (docs, wording) | None | None | None | None | None | None |
| F4.0 Data classification | None (docs) | None | None | None | None | None |
| F4.1 Calendar auto-load removed | `fx-quote-service` | None | `fx-quote-service` (startup behaviour) | None (calendar in memory) | None | None |
| F4.2 Calendar seed in admin | `fx-admin-service` | None | `fx-admin-service` | None | None | None |
| F4.3 `wf_task_exec_log` | `fx-admin-service` | None (no UI now) | `dlp_sor_system_db` | **New:** `wf_task_exec_log` (FK to `wf_exec_log`) | None | None |
| F4.4 Workflow rows | `fx-admin-service` | Admin Workflows master list | `dlp_sor_system_db` | `workflows` (two rows) | None | None |
| F4.5, F4.6 Engine and start rules | `fx-admin-service` | None | `fx-admin-service` | `wf_exec_log`, `wf_task_exec_log` | None | None |
| F4.7 Calendar tasks | `fx-admin-service`, `fx-quote-service` | None | Both services | `wf_exec_log`, `wf_task_exec_log`; quote calendar in memory | None | None |
| F4.8 Initial Data API | `fx-admin-service` | None | `fx-admin-service` | `wf_exec_log`, `wf_task_exec_log` | None | None |
| F4.9 Admin page | None (removed) | None | None | None | None | None |
| F5.1 Pair master file | `fx-quote-service` | None | `fx-quote-service` image | None | None | None |
| F5.2 `currency_pairs` table | `fx-quote-service` | None | `fx_quote_db` | **New:** `currency_pairs` | None | None |
| F5.3 Pair API | `fx-quote-service` (owner) | None yet; consumers later: qualify, route-exec, quote pricing | `fx-quote-service` | `currency_pairs` (read) | None | None |
| F5.4 Pair workflow | `fx-admin-service` (orchestrates), `fx-quote-service` (loads) | None | Both services | `wf_exec_log`, `wf_task_exec_log`, `workflows`, `currency_pairs` | None | None |
| F6.1 Markup seed removed | `fx-quote-service` | None | `fx_quote_db` | `client_tier_spread_schedules` (seed rows deleted) | None | None |
| F7.x Calendar data | `fx-quote-service` (when data lands) | None | Same | Calendar in memory; no table | None | None |

**Notes**
- Pair codes are not foreign keys across services. Dependent services (qualify, route-exec, quote pricing) will store `pair_code` as a plain column and validate it against the owner API or the committed file, per DESIGN §9.4.
- The calendar is held in memory in quote. Rows loaded by a setup are lost on a quote restart. Tracked in [ARCHITECTURE.md §5](../../../ARCHITECTURE.md#5-open-architecture-items).

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
| `6b6ab93` | `main` | Data classification (DESIGN §9), decisions in this file | |
| `9374a9f` | `main` | Committed currency pair master JSON | |
| `4738dbe` | `main` | Changesets 005 (currency pairs) and 006 (remove seeded markups) | |
| `64d2792` | `main` | Quote pair entity, repository, facade, controller | |
| `b857214` | `main` | Admin `INIT_CURRENCY_PAIRS` workflow | |

---

## 5. Decisions

| Date | Decision | Detail |
|---|---|---|
| 2026-11-22 | Two data kinds | Master (initial) data is committed JSON. Transactional data is owner API only; HTTP 500 if owner is down. (DESIGN §9) |
| 2026-11-22 | Markups are not initial data | Startup markup seed removed by changeset 006. Markups come through maker-checker. |
| 2026-11-22 | Pair master owner | `fx-quote-service`, keyed by `pair_code`. |
| 2026-11-22 | Pair set | 28 pairs from the Synthetic Data Catalog spec §1. Settlement attributes are not in the spec and are left for the business. |
| 2026-11-22 | Initial data UI | Initial Data page removed at the owner's request. Trigger placement to be decided. |
| 2026-11-22 | Pairs before calendar | The calendar depends on the pair master, so `INIT_CURRENCY_PAIRS` is listed first. |

---

## 6. Open items

1. **Trigger placement** for Initial Data in the admin portal (F4.9). Not decided; the page was removed.
2. **Calendar source** per event type (F7.1): Fed FOMC, BLS for NFP, ECB. Needed before the calendar seed (F7.2) can be written.
3. **Calendar seed** of 20 years back and 5 years forward (F7.2). Depends on F7.1.
4. **Pair settlement attributes** (F8.1): CLS eligibility, cutoff, clip sizes, restricted flag per pair. Business input.
5. **Seven business questions** in functional view §6 (F8.2).
6. **Calendar in memory** in quote: rows lost on restart. Needs a table or Redis key.
7. **Calendar end-to-end run** (T-32) not yet done.
8. **Workflow resource files:** `bpmn/init-currency-pairs.bpmn20.xml` and `bpmn/init-economic-calendar.bpmn20.xml` are named in the workflow rows but do not exist. The engine does not read them, so runs are unaffected; the files should be added or the rows corrected.
9. **Reason field:** the Initial Data start rule still requires a typed reason (min 8 chars). The owner has said seed loads should need no typing; this rule should be removed.
10. **Old Reference Data pages** remain routable but are not in the nav.
11. **Admin layout:** flat packages. DESIGN §2 defines the rules for new admin code.
12. **Other services** (qualify, route-exec) do not yet validate pair codes against the owner.
