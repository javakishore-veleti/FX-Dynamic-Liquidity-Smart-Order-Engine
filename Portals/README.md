# Portals

Angular UI apps for the FX Dynamic Liquidity & Smart Order Routing Engine.

Each portal is an independent Angular app (npm workspaces via the repo-root `package.json`). Portals call Middleware APIs over HTTP/SSE — they do **not** share Java source with Middleware modules.

Stack: **Angular 21** · **Bootstrap 5** · **Bootstrap Icons** · standalone components · left nav + top menu.

## Apps

| Portal | Persona | Dev URL | Pages |
| :--- | :--- | :--- | :--- |
| `fx-admin-portal` | Dealing desk / SRE / risk | http://localhost:4201 | dashboard, topology, incident-rca, policy-studio, synthetic, agents, sre-dashboards |
| `fx-corporate-portal` | Corporate treasury | http://localhost:4202 | dashboard, blotter, execution-copilot, exposures, tca, alerts |
| `fx-retail-portal` | Retail / remittance | http://localhost:4203 | dashboard, convert, advisor, planner, tracker |

## Run one portal

```bash
npm run run:portal:admin
npm run run:portal:corporate
npm run run:portal:retail
```

Or from each app directory: `npm start` (ports 4201 / 4202 / 4203).

## Build all

```bash
npm run build:portals
```

## Spec

`Docs/Specs/Angular-AI-Portals-Admin-Corporate-Retail-Spec.md`
