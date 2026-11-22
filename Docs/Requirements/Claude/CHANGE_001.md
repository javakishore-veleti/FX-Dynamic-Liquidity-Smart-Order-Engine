# CHANGE_001 — Docs cleanup, compliance wording, and Initial Data setup

**Change ID:** CHANGE_001
**Requested by:** Repo owner
**Date opened:** 2026-11-21
**Branch of record:** `main`
**Backup of pre-change state:** `Cursor_Mess_Before_Claude_Fixes` (points at `d01a665`, the `main` tip before this change)

**Status key:** `DONE` · `IN PROGRESS` · `TODO` · `DEFERRED`

---

## 1. Epics and features

| Epic | Feature | Summary | Status | Started | Completed |
|---|---|---|---|---|---|
| E1 Docs & repo hygiene | F1.1 Remove duplicate specs | Delete byte-identical copies in `Docs/Backup` and `Docs/Architecture`; keep the copy in `Docs/Specs` | DONE | 2026-11-21 | 2026-11-21 |
| E1 Docs & repo hygiene | F1.2 Archive superseded drafts | Move four unique Backup drafts to `Docs/Archive/` | DONE | 2026-11-21 | 2026-11-21 |
| E1 Docs & repo hygiene | F1.3 Requirement files FR-02 to FR-05 | Create one requirement file per FR using the FR-01 template, with status marked unverified | DONE | 2026-11-21 | 2026-11-21 |
| E1 Docs & repo hygiene | F1.4 Working rules for AI agents | `AGENTS.md` at repo root: source-of-truth order, one FR at a time | DONE | 2026-11-21 | 2026-11-21 |
| E1 Docs & repo hygiene | F1.5 Ignore local env files | `.gitignore` rules for `.env` and `.env.*`, keeping `.env.example` | DONE | 2026-11-21 | 2026-11-21 |
| E2 Business view | F2.1 Functional business view | Personas, business functions, data, rules, open questions in `Docs/Requirements/Claude/` | DONE | 2026-11-21 | 2026-11-21 |
| E2 Business view | F2.2 Impact matrix | Per FR: primary microservice, other impacted microservices, impacted portals | DONE | 2026-11-21 | 2026-11-21 |
| E3 Compliance & targets | F3.1 Soften compliance wording | Replace "100% compliance" with confirmed-applicability wording in PRD and README | DONE | 2026-11-21 | 2026-11-21 |
| E3 Compliance & targets | F3.2 Flag numeric targets | Note that PRD and README figures are internal targets, not sourced benchmarks | DONE | 2026-11-21 | 2026-11-21 |
| E3 Compliance & targets | F3.3 Last-look disclosure requirement | Cross-cutting requirement citing FX Global Code Principle 17 | DONE | 2026-11-21 | 2026-11-21 |
| E4 Initial Data setup | F4.1 Stop startup auto-load of economic calendar | Remove the `@PostConstruct` load in `fx-quote-service`; data loads only from the setup button | IN PROGRESS | 2026-11-21 | |
| E4 Initial Data setup | F4.2 Calendar seed data in admin | Copy calendar JSON to `fx-admin-service` resources as the source for the setup workflow | IN PROGRESS | 2026-11-21 | |
| E4 Initial Data setup | F4.3 Task execution child table | New `wf_task_exec_log` table, one row per task, with JSON input and output | IN PROGRESS | 2026-11-21 | |
| E4 Initial Data setup | F4.4 Workflow definition for calendar setup | Master-list row `INIT_ECONOMIC_CALENDAR` (ACTIVE, ADMIN scope) | IN PROGRESS | 2026-11-21 | |
| E4 Initial Data setup | F4.5 Engine writes one row per task | Record input and output for every task in every workflow run | TODO | | |
| E4 Initial Data setup | F4.6 Initial Data start rules | Start validation for `INIT_*` workflows (reason and triggered-by only); refuse a second run once COMPLETED | TODO | | |
| E4 Initial Data setup | F4.7 Calendar load and verify tasks | Task 1 upserts each event via quote API; task 2 verifies the row count | TODO | | |
| E4 Initial Data setup | F4.8 Initial Data API | `GET /api/v1/admin/initial-data`, `POST /api/v1/admin/initial-data/{key}/setup` | TODO | | |
| E4 Initial Data setup | F4.9 Admin portal Initial Data page | Left-nav item replacing Reference Data group; accordion per dataset; Setup button; task view | TODO | | |
| E4 Initial Data setup | F4.10 Build and end-to-end test | Compile, unit test, run the button in a local stack, confirm rows in quote | TODO | | |
| E4 Initial Data setup | F4.11 Remaining five datasets | Markup schedules, settlement policies, credit lines, LP top-of-book, LP rankings | DEFERRED | | |

---

## 2. Tasks and dates (completed work)

| Task ID | Feature | Task | Status | Started | Completed | Commit |
|---|---|---|---|---|---|---|
| T-01 | F1.1 | Remove 11 duplicate spec files from Docs | DONE | 2026-11-21 | 2026-11-21 | `2e7f1ed` |
| T-02 | F1.2 | Move 4 superseded drafts to `Docs/Archive/` | DONE | 2026-11-21 | 2026-11-21 | `2e7f1ed` |
| T-03 | F1.3 | Write FR-02, FR-03, FR-04, FR-05 files | DONE | 2026-11-21 | 2026-11-21 | `2e7f1ed` |
| T-04 | F1.4 | Write `AGENTS.md` | DONE | 2026-11-21 | 2026-11-21 | `2e7f1ed` |
| T-05 | F1.5 | Add `.env` ignore rules | DONE | 2026-11-21 | 2026-11-21 | `e1fa7dd` (history rewritten, see §4) |
| T-06 | F2.1 | Write functional business view | DONE | 2026-11-21 | 2026-11-21 | `db29d94` |
| T-07 | F2.2 | Add impact matrix to business view | DONE | 2026-11-21 | 2026-11-21 | `2e7f1ed` |
| T-08 | F3.1 | Soften compliance wording (PRD, README) | DONE | 2026-11-21 | 2026-11-21 | `3f8f1ab` |
| T-09 | F3.2 | Add numeric-target note (PRD, README) | DONE | 2026-11-21 | 2026-11-21 | `3f8f1ab` |
| T-10 | F3.3 | Add last-look disclosure requirement to PRD | DONE | 2026-11-21 | 2026-11-21 | `3f8f1ab` |
| T-11 | F4.1 | Remove `@PostConstruct` on `EconomicCalendarFacade.loadCalendar()` | IN PROGRESS (uncommitted) | 2026-11-21 | | |
| T-12 | F4.2 | Copy `economic-calendar.json` to admin `initial-data/` | IN PROGRESS (uncommitted) | 2026-11-21 | | |
| T-13 | F4.3 | Changelog `003-wf-task-exec-log.yaml` (table, FK, index, Postgres JSONB) | IN PROGRESS (uncommitted) | 2026-11-21 | | |
| T-14 | F4.4 | Changelog `004-initial-data-workflows.yaml` (workflow row) | IN PROGRESS (uncommitted) | 2026-11-21 | | |
| T-15 | F4.3 | Register 003 and 004 in admin master changelog | IN PROGRESS (uncommitted) | 2026-11-21 | | |

---

## 3. Impact matrix

Columns follow the requested view: microservices, portals, containers, database tables, Kafka events, Redis cache. "None" means the item does not touch that area.

| Feature | Microservices impacted | Portals impacted | Containers impacted | DB tables impacted | Kafka events impacted | Redis cache impacted |
|---|---|---|---|---|---|---|
| F1.1 to F1.5 (docs and hygiene) | None | None | None | None | None | None |
| F2.1 to F2.2 (business view) | None | None | None | None | None | None |
| F3.1 to F3.3 (PRD, README wording) | None | None | None | None | None | None |
| F4.1 Stop calendar auto-load | `fx-quote-service` | None yet | `fx-quote-service` image (behaviour change at startup) | None (calendar is held in memory) | None | None |
| F4.2 Calendar seed in admin | `fx-admin-service` | None yet | `fx-admin-service` image | None | None | None |
| F4.3 `wf_task_exec_log` | `fx-admin-service` | Admin (later, to show task rows) | Postgres `dlp_sor_system_db` (H2 default locally) | **New:** `wf_task_exec_log`; FK to `wf_exec_log` | None | None |
| F4.4 Workflow row `INIT_ECONOMIC_CALENDAR` | `fx-admin-service` | Admin (Workflows master list will show it) | `dlp_sor_system_db` | `workflows` (one new row) | None | None |
| F4.5 to F4.7 (engine, calendar tasks) | `fx-admin-service` (writes), `fx-quote-service` (receives upserts) | Admin (later) | `fx-admin-service`, `fx-quote-service` | `wf_exec_log`, `wf_task_exec_log` | None | None |
| F4.8 Initial Data API | `fx-admin-service` | Admin (later) | `fx-admin-service` | `wf_exec_log`, `wf_task_exec_log` | None | None |
| F4.9 Admin portal page | None (UI only, calls admin API) | **Admin** | `fx-admin-portal` container, if containerised | None | None | None |
| F4.10 Build and test | All touched services | Admin | Local stack (`docker-all-up` for Postgres) | Schema applied by Liquibase | None | None |
| F4.11 Remaining datasets | `fx-admin-service`, `fx-quote-service`, `fx-qualify-service`, `fx-route-exec-service` | Admin | Same as above | Per dataset, to be confirmed | To confirm per dataset | To confirm (credit lines may use Redis) |

**Note on the calendar:** the calendar in `fx-quote-service` is held in memory (`volatile` list). It has no database table and no Redis key. Any setup run adds rows to memory only, so the rows are lost on a quote-service restart. That is a gap to close before the setup can be relied on.

---

## 4. Commits and branch history

| Commit | Branch | Content | Notes |
|---|---|---|---|
| `db29d94` | `docs/functional-requirements-business-view` | Functional business view | Rewritten: author trailer removed |
| `6224da7` | same | Spread-schedule admin pages (in-progress Cursor work) | Rewritten: author trailer removed |
| `e1fa7dd` | same | `.env` ignore rules | Rewritten: author trailer removed |
| `d01a665` | `main` | Merge of PR #1 (the docs branch) | Merged on GitHub |
| `2e7f1ed` | `main` | Docs cleanup, FR-02 to FR-05, `AGENTS.md`, impact matrix | Pushed |
| `3f8f1ab` | `main` | Compliance wording, target notes, last-look requirement | Pushed |

**History rewrite:** the three docs-branch commits were rewritten to remove a `Co-Authored-By` trailer, and the branch was force-pushed with `--force-with-lease`. The branch was created in this session and had no other users. `main` was not rewritten.

---

## 5. Open items

1. The six Initial Data datasets are not yet confirmed by the business (see functional view §6).
2. Whether the auto-seeds in quote and qualify should be removed for all datasets (decision pending).
3. The quote unit test still calls `loadCalendar()` directly. The method is kept so the test still passes; the test should move to the admin-side loader later.
4. Build status of F4.x is unverified. No compile or test run has been done for the changed Java yet.
5. Old Reference Data pages remain routable but are no longer in the left nav.
