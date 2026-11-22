# CHANGE_001 — Docs cleanup, compliance wording, and Initial Data setup

**Change ID:** CHANGE_001
**Requested by:** Repo owner
**Date opened:** 2026-11-21
**Branch of record:** `main`
**Backup of pre-change state:** `Cursor_Mess_Before_Claude_Fixes` (points at `d01a665`, the `main` tip before this change)
**Rules applied to every feature:** [DESIGN.md](../../../DESIGN.md) and [ARCHITECTURE.md](../../../ARCHITECTURE.md)

**Status key:** `DONE` · `IN PROGRESS` · `TODO` · `DEFERRED` · `VERIFIED` (tested, not just written)

---

## 1. Epics and features

| Epic | Feature | Summary | Rules checked | Status | Started | Completed |
|---|---|---|---|---|---|---|
| E1 Docs & repo hygiene | F1.1 Remove duplicate specs | Delete byte-identical copies in `Docs/Backup` and `Docs/Architecture` | DESIGN §8.4 (archive, no duplicates) | DONE | 2026-11-21 | 2026-11-21 |
| E1 Docs & repo hygiene | F1.2 Archive superseded drafts | Move four unique Backup drafts to `Docs/Archive/` | DESIGN §8.4 | DONE | 2026-11-21 | 2026-11-21 |
| E1 Docs & repo hygiene | F1.3 Requirement files FR-02 to FR-05 | One file per FR, FR-01 template, status unverified | DESIGN §8.2 | DONE | 2026-11-21 | 2026-11-21 |
| E1 Docs & repo hygiene | F1.4 Working rules for AI agents | `AGENTS.md` at repo root | DESIGN §7, §8 | DONE | 2026-11-21 | 2026-11-21 |
| E1 Docs & repo hygiene | F1.5 Ignore local env files | `.gitignore` rules for `.env` and `.env.*` | DESIGN §7.3 | DONE | 2026-11-21 | 2026-11-21 |
| E2 Business view | F2.1 Functional business view | Personas, functions, data, rules, open questions | DESIGN §8.3 | DONE | 2026-11-21 | 2026-11-21 |
| E2 Business view | F2.2 Impact matrix | Per FR: primary microservice, other microservices, portals | DESIGN §8.1 | DONE | 2026-11-21 | 2026-11-21 |
| E3 Compliance & targets | F3.1 Soften compliance wording | "100% compliance" replaced with applicability wording | DESIGN §8.5 | DONE | 2026-11-21 | 2026-11-21 |
| E3 Compliance & targets | F3.2 Flag numeric targets | PRD and README figures marked as internal targets | DESIGN §8.5 | DONE | 2026-11-21 | 2026-11-21 |
| E3 Compliance & targets | F3.3 Last-look disclosure requirement | Cross-cutting requirement, FX Global Code Principle 17 | DESIGN §8.5 | DONE | 2026-11-21 | 2026-11-21 |
| E4 Initial Data setup | F4.1 Stop startup auto-load of calendar | Remove `@PostConstruct` load in `fx-quote-service`; method kept for its unit test | DESIGN §1.5 (no startup data loads) | IN PROGRESS | 2026-11-21 | |
| E4 Initial Data setup | F4.2 Calendar seed in admin | Copy calendar JSON to `fx-admin-service` `initial-data/` | DESIGN §4.5 (seed data is business data via button) | DONE | 2026-11-21 | 2026-11-21 |
| E4 Initial Data setup | F4.3 Task execution child table | `wf_task_exec_log`, one row per task, JSON input and output, audit columns | DESIGN §4.1 to §4.3, §5.1 | DONE (compiles) | 2026-11-21 | 2026-11-21 |
| E4 Initial Data setup | F4.4 Workflow definition row | `INIT_ECONOMIC_CALENDAR` in master list (ACTIVE, ADMIN) | DESIGN §4.5 | DONE (compiles) | 2026-11-21 | 2026-11-21 |
| E4 Initial Data setup | F4.5 Engine writes one row per task | Input and output recorded for success and failure | DESIGN §5.1, §5.5 | DONE (compiles) | 2026-11-21 | 2026-11-22 |
| E4 Initial Data setup | F4.6 Initial Data start rules | `INIT_*` start needs reason and triggeredBy only; second run refused once COMPLETED (409) | DESIGN §5.2, §5.3 | DONE (compiles) | 2026-11-21 | 2026-11-22 |
| E4 Initial Data setup | F4.7 Calendar load and verify tasks | `CalLoadTask` and `CalVerifyTask` as `WfTask` beans under `initdata/tasks/` | DESIGN §2 (tasks sub-package), §3 (`Cal*Task`), §1.4 | DONE (compiles) | 2026-11-21 | 2026-11-22 |
| E4 Initial Data setup | F4.8 Initial Data API | `GET /api/v1/admin/initial-data`, `POST .../{dataset}/setup`; `InitDataFacade` in `initdata/` | DESIGN §2 (one package per facade), §3 (`Facade`) | DONE (compiles) | 2026-11-21 | 2026-11-22 |
| E4 Initial Data setup | F4.9 Admin portal Initial Data page | Left-nav item; accordion per dataset; Setup button; flow view under `pages/initdata/tasks/` | DESIGN §6, §2 (UI mirror) | TODO | | |
| E4 Initial Data setup | F4.10 Build, test, end-to-end run | Compile and unit tests pass; end-to-end run of the button is not yet done | DESIGN §7.5 | IN PROGRESS (compile and existing test pass) | 2026-11-21 | |
| E4 Initial Data setup | F4.11 Remaining five datasets | Markup schedules, settlement policies, credit lines, LP top-of-book, LP rankings | DESIGN §2 (facade package per dataset) | DEFERRED (business to confirm) | | |

---

## 2. Tasks and dates

| Task ID | Feature | Task | Status | Started | Completed | Commit |
|---|---|---|---|---|---|---|
| T-01 | F1.1 | Remove 11 duplicate spec files from Docs | DONE | 2026-11-21 | 2026-11-21 | `2e7f1ed` |
| T-02 | F1.2 | Move 4 superseded drafts to `Docs/Archive/` | DONE | 2026-11-21 | 2026-11-21 | `2e7f1ed` |
| T-03 | F1.3 | Write FR-02, FR-03, FR-04, FR-05 files | DONE | 2026-11-21 | 2026-11-21 | `2e7f1ed` |
| T-04 | F1.4 | Write `AGENTS.md` | DONE | 2026-11-21 | 2026-11-21 | `2e7f1ed` |
| T-05 | F1.5 | Add `.env` ignore rules | DONE | 2026-11-21 | 2026-11-21 | `e1fa7dd` (history rewritten) |
| T-06 | F2.1 | Write functional business view | DONE | 2026-11-21 | 2026-11-21 | `db29d94` |
| T-07 | F2.2 | Add impact matrix to business view | DONE | 2026-11-21 | 2026-11-21 | `2e7f1ed` |
| T-08 | F3.1 | Soften compliance wording (PRD, README) | DONE | 2026-11-21 | 2026-11-21 | `3f8f1ab` |
| T-09 | F3.2 | Add numeric-target note (PRD, README) | DONE | 2026-11-21 | 2026-11-21 | `3f8f1ab` |
| T-10 | F3.3 | Add last-look disclosure requirement to PRD | DONE | 2026-11-21 | 2026-11-21 | `3f8f1ab` |
| T-11 | F4.1 | Remove `@PostConstruct` on calendar loader (quote) | DONE | 2026-11-21 | 2026-11-21 | `84c05dc` |
| T-12 | F4.2 | Copy calendar JSON to admin `initial-data/` | DONE | 2026-11-21 | 2026-11-21 | `84c05dc` |
| T-13 | F4.3 | Changelog `003-wf-task-exec-log.yaml` (table, FK, index, JSONB) | DONE | 2026-11-21 | 2026-11-21 | `84c05dc` |
| T-14 | F4.4 | Changelog `004-initial-data-workflows.yaml` (workflow row) | DONE | 2026-11-21 | 2026-11-21 | `84c05dc` |
| T-15 | F4.3 | Register 003 and 004 in admin master changelog | DONE | 2026-11-21 | 2026-11-21 | `84c05dc` |
| T-16 | F4.3 | Entity `WfTaskExecLog`, repository `WfTaskExecLogRepository` | DONE | 2026-11-22 | 2026-11-22 | `9cc99ac` |
| T-17 | F4.5 | `BpmnWorkflowService.recordTask` writes child rows | DONE | 2026-11-22 | 2026-11-22 | `9cc99ac` |
| T-18 | F4.6 | `validateInitialDataStart` and 409 guard in `InitDataFacade` | DONE | 2026-11-22 | 2026-11-22 | `9cc99ac` |
| T-19 | F4.7 | Calendar tasks as `WfTask` beans; engine dispatch by id | DONE | 2026-11-22 | 2026-11-22 | pending commit |
| T-20 | F4.8 | Move Initial Data classes to `initdata/`; short names; controller `InitDataController` | DONE | 2026-11-22 | 2026-11-22 | pending commit |
| T-21 | — | `DESIGN.md` and `ARCHITECTURE.md` written | DONE | 2026-11-22 | 2026-11-22 | pending commit |
| T-22 | F4.10 | `mvn -pl Middleware/fx-admin-service test` passes (existing test) | DONE | 2026-11-22 | 2026-11-22 | — |
| T-23 | F4.10 | End-to-end run: start quote and admin, press Setup, check rows and task log | TODO | | | |

---

## 3. Impact matrix

Columns follow the requested view. "None" means the item does not touch that area.

| Feature | Microservices impacted | Portals impacted | Containers impacted | DB tables impacted | Kafka events impacted | Redis cache impacted |
|---|---|---|---|---|---|---|
| F1.1 to F1.5 (docs and hygiene) | None | None | None | None | None | None |
| F2.1 to F2.2 (business view) | None | None | None | None | None | None |
| F3.1 to F3.3 (PRD, README wording) | None | None | None | None | None | None |
| F4.1 Stop calendar auto-load | `fx-quote-service` | None | `fx-quote-service` (startup behaviour) | None (calendar is in memory) | None | None |
| F4.2 Calendar seed in admin | `fx-admin-service` | None | `fx-admin-service` image | None | None | None |
| F4.3 `wf_task_exec_log` | `fx-admin-service` | Admin (later, task view) | Postgres `dlp_sor_system_db` | **New:** `wf_task_exec_log`, FK to `wf_exec_log` | None | None |
| F4.4 Workflow row `INIT_ECONOMIC_CALENDAR` | `fx-admin-service` | Admin (Workflows master list) | `dlp_sor_system_db` | `workflows` (one row) | None | None |
| F4.5 Engine writes task rows | `fx-admin-service` | Admin (later) | `fx-admin-service` | `wf_exec_log`, `wf_task_exec_log` | None | None |
| F4.6 Initial Data start rules | `fx-admin-service` | Admin (later) | `fx-admin-service` | Reads `wf_exec_log` | None | None |
| F4.7 Calendar load and verify tasks | `fx-admin-service` (writes), `fx-quote-service` (receives upserts and lists) | Admin (later) | `fx-admin-service`, `fx-quote-service` | `wf_exec_log`, `wf_task_exec_log`; quote calendar is in memory | None | None |
| F4.8 Initial Data API | `fx-admin-service` | Admin (later) | `fx-admin-service` | `wf_exec_log`, `wf_task_exec_log` | None | None |
| F4.9 Admin portal page | None (UI calls admin API) | **Admin** | `fx-admin-portal` (if containerised) | None | None | None |
| F4.10 Build and test | All touched services | Admin | Local stack (Postgres for profile runs) | Liquibase schema | None | None |
| F4.11 Remaining datasets | `fx-admin-service`, `fx-quote-service`, `fx-qualify-service`, `fx-route-exec-service` | Admin | Same as above | Per dataset, to confirm | To confirm | To confirm (credit lines may use Redis) |

**Calendar gap:** the quote calendar is an in-memory list. A setup run adds rows to memory only, so they are lost on a quote restart. Tracked in [ARCHITECTURE.md §5](../../../ARCHITECTURE.md#5-open-architecture-items).

---

## 4. Commits and branch history

| Commit | Branch | Content | Notes |
|---|---|---|---|
| `db29d94` | `docs/functional-requirements-business-view` | Functional business view | Rewritten: author trailer removed |
| `6224da7` | same | Spread-schedule admin pages (in-progress work) | Rewritten: author trailer removed |
| `e1fa7dd` | same | `.env` ignore rules | Rewritten: author trailer removed |
| `d01a665` | `main` | Merge of PR #1 | Merged on GitHub |
| `2e7f1ed` | `main` | Docs cleanup, FR-02 to FR-05, `AGENTS.md`, impact matrix | Pushed |
| `3f8f1ab` | `main` | Compliance wording, target notes, last-look requirement | Pushed |
| `84c05dc` | `main` | Start Initial Data: calendar auto-load removed, task log schema, workflow row, this file | Pushed |
| `9cc99ac` | `main` | Initial Data backend: task log rows, start rules, calendar workflow, API | Pushed |
| (pending) | `main` | `initdata/` package and short names, `WfTask` beans, DESIGN.md, ARCHITECTURE.md, CHANGE_001 update | To push |

---

## 5. Open items

1. The six Initial Data datasets are not yet confirmed by the business (functional view §6).
2. Whether to remove the remaining auto-seeds in quote and qualify for all datasets (pending decision).
3. The quote unit test still calls `loadCalendar()`. Kept so the test passes; move the test to the admin-side loader later.
4. Admin portal page (F4.9) not started.
5. End-to-end run of the button (F4.10, T-23) not yet done.
6. Old Reference Data pages remain routable but are no longer in the left nav.
7. Admin uses a flat layout; DESIGN.md §2 defines the rules for new admin code.

---

## 6. Decisions recorded 2026-11-22

| Decision | Detail | Status |
|---|---|---|
| Data classification | Master data (committed JSON, initial load) vs transactional data (owner API only, HTTP 500 if owner is down). Recorded in `DESIGN.md` §9. | DONE (docs) |
| Initial data scope | Currency pair master (28 pairs) and economic calendar (last 20 years, next 5 years). Markup schedules are not initial data. | DECIDED |
| Pair master ownership | `fx-quote-service`. Dependent services store the pair code as a plain column; validation by owner API or committed JSON, per DESIGN §9. | DECIDED |
| Remove EURUSD markup seed from quote startup | Markup seed inserts in `002-seed-eurusd-spread-schedules.yaml` and `003-spread-schedule-status-and-macro-seed.yaml` load markups at startup. Should be removed, because markups are not initial data. | PENDING APPROVAL |
| Calendar source | Event dates need an authoritative published source per event type (Fed FOMC, BLS for NFP, ECB). Not yet chosen. | PENDING |
| Pair master settlement attributes | CLS eligibility, cutoff, clip sizes, restricted flag per pair are business data, not in the spec. | PENDING business input |
