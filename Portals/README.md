# Portals

Angular-based UI applications for the FX Dynamic Liquidity & Smart Order Routing Engine.

Each portal is an independent Angular app under this directory (npm workspaces via the repo-root `package.json`). Portals call Middleware APIs over HTTP/WebSocket — they do **not** share Java source with Middleware modules.

## Planned portals

| Portal | Purpose |
| :--- | :--- |
| `fx-dealer-desk-portal` | Dealer escalation, STP exceptions, routing diagnostics |
| `fx-ops-control-portal` | Session calendars, policy maker-checker, LP health |
| `fx-tca-portal` | Transaction cost analysis and best-execution provenance |

Scaffold a portal with Angular CLI from this directory, then register it under the root npm workspaces.
