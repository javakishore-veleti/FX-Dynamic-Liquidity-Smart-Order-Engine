# **Angular AI Portals & Multi-Segment Experience Specification (Admin, Corporate & Retail)**

**Document ID:** Angular-AI-Portals-Admin-Corporate-Retail-Spec.md  
**Repository:** fx-dynamic-liquidity-smart-order-engine  
**Frontend Workspace:** Portals/ (Angular 19+ Standalone Components, Signals & NGRX SignalStore)  
**Backend Integration:** Middleware/ & Agentic-Python/ (REST/SSE \+ Agentic MCP Orchestrators)  
**Architecture Style:** Domain-Driven Multi-Portal Workspace with Strict DTO Contracts & Role-Based Isolation

## **1\. Executive Answer: Does a Dynamic LP & Smart Order Routing Business Serve Retail Customers?**

**Yes—indirectly and directly through tiered channels.** While institutional interbank Smart Order Routing (SOR) is traditionally built for Fortune 500 corporate treasuries and asset managers, Tier-1 commercial and investment banks routinely expose their Dynamic LP & SOR engine to **Retail & Mass-Affluent Customers** across three high-volume channels:

| Retail / Consumer Channel | How Dynamic LP & SOR Powers It Behind the Scenes | Order Aggregation & Execution Pattern |
| :---- | :---- | :---- |
| **1\. Cross-Border Remittances & Global Transfers** | Retail banking customers sending international wires (USD to INR, EUR, GBP, MXN) receive guaranteed multi-hour rate locks backed by the bank's wholesale LP aggregation. | Thousands of small retail tickets ($200–$10,000) are netted internally against opposite retail flow or swept in aggregated wholesale clips via fx-route-exec-service. |
| **2\. Multi-Currency Digital Wallets & Travel FX Cards** | Instant in-app currency conversion with dynamic session-aware spreads (tighter spreads during London/NY overlap, wider weekend buffers). | Sub-second indicative quotes served from fx-quote-service edge/L1 caches; fractional spreads captured cleanly without spread leakage. |
| **3\. Self-Directed Retail FX & Wealth Advisory** | High-net-worth (HNW) and active retail FX traders executing spot/forward conversions with transparent Best-Execution TCA receipts. | Pre-trade compliance and daily limit checks via fx-qualify-service; automated waterfall failover prevents retail order rejections during macro spikes. |

## **2\. Multi-Portal Monorepo Topology (Portals/)**

Aligned with our zero-sharing architectural discipline, each persona operates in a dedicated Angular 19+ application under Portals/ with strict OAuth2/OIDC audience isolation:

| Angular Portal App | Target Persona | Primary Backend & Agentic Endpoints | Core UX & Latency Profile |
| :---- | :---- | :---- | :---- |
| **Portals/fx-admin-portal** | eFX Dealing Desk, Platform SREs, Risk & Policy Admins | fx-admin-service, fx-topology-service, fx-agent-orchestrator-service | Real-time 3-layer topology graph, Drools maker-checker studio, synthetic batch/chaos console, and autonomous SRE/RCA agent command center. |
| **Portals/fx-corporate-portal** | Fortune 500 Treasurers, Importers/Exporters, Institutional Asset Managers | fx-quote-service (SSE), fx-qualify-service, fx-route-exec-service, Corporate Treasury AI Copilot | Live multi-LP depth blotter, block-order sweep visualizer, MiFID II / FX Global Code TCA provenance viewer, and natural-language hedging copilot. |
| **Portals/fx-retail-portal** | Consumer Banking, Cross-Border Remittance & Wealth FX Clients | fx-quote-service (Cached Edge/Indicative), fx-route-exec-service (Netted Retail Flow), Retail FX Coach AI | One-click currency conversion, rate-lock alerts, transparent fee/spread breakdown, international transfer tracker, and conversational FX timing advisor. |

## **3\. Portal 1: Admin & Dealing Desk AI Portal (Portals/fx-admin-portal)**

Built for eFX chief dealers, platform architects, and SREs to govern the 1M RPS engine and interact with the four autonomous agents via Streaming SSE and Generative UI widgets.

| Angular AI Capability | Frontend Implementation (Angular 19+ Signals) | Backend / MCP Agent Integration | Operational Value |
| :---- | :---- | :---- | :---- |
| **1\. Interactive Netflix-Style Service Topology & Blast-Radius Canvas** | Canvas/WebGL force-directed graph (ngx-graph / Cytoscape) driven by Angular Signals, rendering Network (eBPF), IPC, and Trace layers with time-travel scrubber. | Queries fx-topology-service and triggers **Blast-Radius Agent** (POST /api/v1/agents/blast-radius/analyze). | Click any LP or microservice node to highlight downstream STP impact, desk owners, and live p99 latency heatmaps in \< 500 ms. |
| **2\. 3:00 AM Autonomous Incident & RCA Copilot Drawer** | Slide-over conversational AI drawer streaming SSE reasoning steps, Iceberg outlier trace tables, and one-click Maker-Checker approval cards. | Invokes **Incident RCA Agent**; executes approved LP kill switch via POST /api/v1/admin/venues/{lpId}/halt logged in wf\_exec\_log. | Compresses incident triage from 45 minutes to under 60 seconds with full audit provenance. |
| **3\. Natural-Language Drools / DMN Policy Authoring Studio** | Split-pane AI rule generator: dealer types policy intent in plain English; Angular renders generated DMN decision table diff \+ shadow-mode replay P\&L simulation. | Validates rule syntax and runs historical shadow replay before submitting to Maker-Checker approval queue. | Eliminates manual spreadsheet/XML errors while enforcing four-eyes governance. |
| **4\. Synthetic Data & Chaos Simulation Command Center** | Reactive form & live progress tracker polling dlp\_sor\_system\_db.wf\_exec\_log with AI scenario presets (e.g., "Simulate FOMC 100x EUR/USD Skew"). | Calls POST /api/v1/admin/synthetic/datasets/generate & **Synthetic Chaos Agent**. | Lets QA and SREs stress-test backpressure and partition skew with one click. |

## **4\. Portal 2: Corporate Treasury & Institutional AI Portal (Portals/fx-corporate-portal)**

Designed for corporate treasurers, CFOs, and institutional execution desks managing multi-million-dollar hedging, payroll settlements, and block orders.

| Angular AI Capability | Frontend Implementation (Angular 19+ Signals) | Backend & SOR Data Integration | Commercial Value for Corporate Clients |
| :---- | :---- | :---- | :---- |
| **1\. Conversational Treasury Execution & Splitting Copilot** | Embedded AI assistant inside the order ticket: treasurer asks *"What is the lowest-slippage way to buy \$45M EUR/USD before NY close?"*; renders interactive Multi-LP Sweep vs. TWAP preview card. | Calls POST /api/v1/fx/orders/qualify and simulates composite LP depth & markout toxicity without revealing raw wholesale LP identities. | Prevents market impact on block orders and guides treasurers away from illiquid rollover windows. |
| **2\. Automated Cash-Flow Exposure & Hedge Recommender** | Upload ERP payables/receivables CSV or connect treasury feed; Angular AI dashboard groups multi-currency exposures by value date and recommends Spot/Forward hedge tranches. | Fetches forward points and tier spreads from fx-quote-service and validates credit headroom via GET /api/v1/fx/credit/utilization/{clientId}. | Automates repetitive corporate FX hedging workflows and prevents bilateral credit limit surprises. |
| **3\. Plain-English Best Execution & TCA Explainer** | Interactive post-trade execution receipt: click any completed fill to view an AI-generated plain-English summary of spread saved (bps), fill certainty, and regulatory compliance. | Reads sanitized provenance summary from GET /api/v1/fx/orders/{clientOrderId}/trace (MiFID II / FX Global Code). | Gives corporate audit committees instant proof of institutional best execution. |
| **4\. Macro Volatility & Liquidity Regime Alert Assistant** | Proactive banner & Signal-driven alert feed warning treasurers ahead of FOMC, NFP, ECB, or NY 5:00 PM rollover spread widening. | Subscribes to fx-quote-service session regime metadata stream via SSE. | Helps corporate clients lock rates ahead of scheduled macro volatility spikes. |

## **5\. Portal 3: Retail & Consumer FX AI Portal (Portals/fx-retail-portal)**

Designed for retail banking customers, international remitters, expats, and mass-affluent wealth clients—abstracting institutional complexity into transparent, consumer-friendly guidance.

| Angular AI Capability | Frontend Implementation (Angular 19+ Signals) | Backend & SOR Integration | Consumer Experience Value |
| :---- | :---- | :---- | :---- |
| **1\. Conversational "Smart Transfer & Rate Advisor"** | Natural-language chat & quick-prompt widget: *"Send \$2,500 to my family in Mumbai this week—should I convert now or set an auto-lock?"* | Queries GET /api/v1/fx/rates/{ccyPair}, session liquidity calendar, and retail tier markup rules. | Explains live mid-market vs. all-in rate in simple terms and lets users set target-rate auto-conversions. |
| **2\. Transparent "All-In Rate & Savings" Visualizer** | Clean visual breakdown comparing the bank's SOR-powered rate against standard retail bank markups, highlighting zero hidden wire fees. | Uses aggregated retail rate-card quotes from fx-quote-service edge cache (absorbing high consumer read RPS). | Builds consumer trust and drives higher digital remittance conversion rates. |
| **3\. AI Auto-Convert & Recurring Remittance Planner** | Goal-based wizard for tuition payments, overseas mortgages, or monthly family support with automated DCA (Dollar-Cost Averaging) schedules. | Submits netted retail batch orders to fx-route-exec-service during peak liquidity windows (London/NY overlap) for tightest spreads. | Delivers institutional-grade execution efficiency to everyday retail transfers. |
| **4\. Real-Time Cross-Border Payment Tracker & Anomaly Helper** | Visual milestone tracker (Rate Locked \-\> FX Converted \-\> CLS/Local Rail Settled) with an AI helper explaining holiday cutoffs or beneficiary bank delays. | Reads settlement cutoff metadata from fx-qualify-service and sanitized order state. | Reduces retail customer support call volume on international transfer status inquiries. |

## **6\. Angular 19+ Technical & Security Architecture Standards**

> * **Zoneless Change Detection & Angular Signals:** Mandatory across all three portals so high-frequency SSE quote ticks (10–50 updates/sec in Corporate and Admin blotters) update only targeted DOM cells without triggering full component-tree dirty checking.  
> * **Streaming Generative UI (SSE \+ Typed DTOs):** All AI copilot interactions stream structured JSON chunks over Server-Sent Events (SSE) mapped to strongly typed TypeScript interfaces matching backend Pydantic/Java DTOs—rendering interactive charts, approval cards, and order tickets instead of raw walls of text.  
> * **Strict Audience & Information-Barrier Isolation:** Retail and Corporate portals never receive raw wholesale LP names, internal toxicity scores, or counterparty credit internals—fx-quote-service and BFF gateways strip sensitive institutional metadata before serialization.  
> * **Guardrailed AI Action Execution:** In fx-admin-portal, any AI-suggested mutation (venue halt, cache flush, credit override) renders a mandatory confirmation diff card requiring an explicit Maker-Checker JWT signature before calling fx-admin-service.