# Working rules for AI coding agents (Cursor, Claude Code)

## Source of truth, in order
1. `PRD-FX-SOR-001.md` — what the system must do (FR-01 to FR-05).
2. `Docs/Requirements/FunctionalRequirement-0N.md` — the checklist for one FR, including status.
3. `Docs/Requirements/Claude/functional-requirements-business-view.md` — personas, business functions, data, rules, and which services and portals each FR touches.
4. `Docs/Specs/` and `Docs/Architecture/` — how things are built.

Do not use `Docs/Archive/` — it holds superseded drafts.

## How to work
- Work on **one FR at a time**. Start from its `FunctionalRequirement-0N.md` checklist.
- Change only the primary service for that FR unless the business view lists another service as impacted.
- Do not add reference data that the business has not supplied. Mark it TODO instead.
- Data loads happen only through the Initial Data setup buttons in the admin portal, not at startup.

## Commits
- Use the repo owner's git identity. Do not add `Co-Authored-By` or tool attribution lines.
