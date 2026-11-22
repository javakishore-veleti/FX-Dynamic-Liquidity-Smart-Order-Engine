# DESIGN.md — Rules for building in this repo

Read this before any story or feature. Every change must follow these rules. Read [ARCHITECTURE.md](ARCHITECTURE.md) for the system layout and [PRD-FX-SOR-001.md](PRD-FX-SOR-001.md) for what to build.

---

## 1. Architecture rules

1. **Zero sharing between microservices.** Each service is self-contained: its own Java code, its own database, its own Liquibase changelog. Services talk over HTTP (`RestClient`) or Kafka events. No shared library.
2. **Closed layers (Neal Ford).** Dependencies point one way: presentation → business (facade) → services/adapters → persistence. Transport details (HTTP, Kafka, FIX) never leak into business rules. Persistence never calls up.
3. **Facade is the unit of business capability.** Each facade has its own package. Its internal tasks live in a `tasks` sub-package.
4. **Workflow tasks are beans.** A BPMN step is a Spring bean implementing `WfTask` (in `wf/`). The engine dispatches by task id. The engine holds no task logic.
5. **Reference data is loaded by button only.** No startup auto-seeds, no `@PostConstruct` data loads. Initial Data is loaded once by a setup workflow started from the admin portal.
6. **Hot path stays local.** Caches (Caffeine L1, Redis L2) sit in front of the quote path. Audit writes to Kafka or to the control-plane database are never blocking on the critical path.

## 2. Package and folder rules

Admin service (`Middleware/fx-admin-service`) uses this layout today:

| Package | Holds |
|---|---|
| `controller/` | REST controllers (presentation) |
| `service/` | Orchestration services and outbound clients (`QuoteScheduleClient`) |
| `initdata/` | Initial Data facade, view, catalog. Sub-package `initdata/tasks/` for its workflow tasks |
| `bpmn/` | Step catalog for FR-01 workflows |
| `wf/` | Workflow engine contracts (`WfTask`) |
| `entity/`, `repository/` | JPA entities and repositories |
| `dto/` | Request and response DTOs shared by admin endpoints |
| `eventing/`, `metrics/`, `config/` | Cross-cutting infrastructure |

Rules:
- **One package per facade.** A new capability gets its own package under the service root, e.g. `initdata/`, `<capability>/`.
- **Tasks under `tasks/`.** Workflow tasks for a facade go in `<facade>/tasks/`.
- **Frontend mirrors this.** Each facade gets a UI folder under `Portals/<portal>/src/app/pages/<facade>/`, with a `tasks/` sub-folder for its flow views.
- Quote, qualify, and route-exec use `business/facade`, `presentation/controllers`, `persistence/entities`, and `services/adapters`. Follow the layout of the service you are changing; do not mix layouts inside one service.

## 3. Naming rules

Keep class names short. Use the abbreviations below. Do not write long names.

| Abbreviation | Meaning | Example |
|---|---|---|
| `Wf` | workflow | `WfTask`, `WfExecLog`, `WfTaskExecLog` |
| `Svc` | outbound service call (HTTP client to another service) | `QuoteSvc` (field name), `QuoteScheduleClient` (existing class) |
| `Acct` | account / client account | `AcctCreditLine` |
| `Ctl` | controller, when a new one is needed | (existing controllers keep `Controller`) |
| `Repo` | repository, when a new one is needed | (existing repositories keep `Repository`) |

Rules:
- Facade: `<Capability>Facade` (e.g. `InitDataFacade`).
- Task: `<Verb><Noun>Task` (e.g. `CalLoadTask`, `CalVerifyTask`).
- View/DTO: `<Capability>View` or `<Name>Req` / `<Name>Resp`.
- Catalog: `<Capability>Catalog`.
- Field names follow the same short forms (`quoteSvc`, `execRepo`).
- Do not rename existing classes just to shorten them. Apply the rules to new code.

## 4. Data rules

1. **Audit columns on every new table:** `created_at`, `created_by`, `updated_at`, `updated_by`.
2. **JSON columns:** store as `clob` in the Liquibase changelog and promote to `jsonb` on PostgreSQL in a separate changeset (`dbms: postgresql`). Follow `001-wf-exec-log.yaml`.
3. **Child tables** reference their parent with a foreign key and `ON DELETE CASCADE`, and carry an index on `(parent_id, seq)`.
4. **Liquibase only.** Schema changes go in `db/changelog/changes/NNN-*.yaml`, registered in the service's master changelog. Never edit a changeset that has been applied.
5. **Seed data is metadata only.** A seed changeset may insert a workflow definition row. It must not insert business reference data. Business reference data comes from the Initial Data buttons.

## 5. Workflow rules

1. Every BPMN run writes one `wf_exec_log` row (parent) and one `wf_task_exec_log` row per executed task (child), with input JSON, output JSON, or error details.
2. `INIT_*` workflows (Initial Data) require only a business reason (min 8 chars) and `triggeredBy`. Other workflows keep their trade validation.
3. An Initial Data dataset is loaded once. After a `COMPLETED` run the setup is refused with HTTP 409. A failed run can be retried.
4. A USER task waits for a person. SERVICE tasks run automatically. GATEWAY tasks decide which path runs.
5. Task output is recorded even when the task fails.

## 6. UI rules (admin portal)

1. Left nav lists capabilities, not tables. Initial Data is one nav item.
2. Initial Data: one accordion per dataset. A **Setup** button runs the dataset's workflow. No other control loads data.
3. If more than five datasets exist, show a dropdown at the top that selects one dataset; show only that dataset's accordion.
4. After a run, show the run's tasks as a flow (BPMN view) inside the accordion, with each task's status, input, and output.
5. Once a dataset is COMPLETED, the Setup button is replaced by a completed label. Do not show a reload control.

## 7. Git and delivery rules

1. Commit under the repo owner's git identity. No `Co-Authored-By` or tool attribution lines in commits or PRs.
2. Commit at each strategic point (a working build, a finished feature, a docs change). Push after each commit so work is not lost.
3. Do not commit secrets. `.env` and `.env.*` are ignored; `.env.example` is allowed.
4. Work on `main` only when the owner asks. Keep the `Cursor_Mess_Before_Claude_Fixes` branch as the pre-cleanup record.
5. Before a feature is marked done, compile and run the service's tests (`mvn -pl <module> test`).

## 8. Documentation rules

1. Every feature is tracked in `Docs/Requirements/Claude/CHANGE_001.md`. Each feature row states the rules it was checked against, its microservices, portals, containers, DB tables, Kafka events, and Redis use.
2. Each FR has a file in `Docs/Requirements/` (`FunctionalRequirement-0N.md`) with its checklist and status. Status is `VERIFIED` only after a test run, not after code is written.
3. Business rules and personas live in `Docs/Requirements/Claude/functional-requirements-business-view.md`.
4. Superseded documents go to `Docs/Archive/`. Do not keep duplicate copies.
5. Do not state a compliance claim as fact unless the business has confirmed it (see PRD §1 note on targets).
