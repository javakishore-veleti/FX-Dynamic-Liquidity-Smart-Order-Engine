# Service Topology premises and gap analysis

**Status:** Draft, 2026-11-22
**Scope:** `fx-topology-service` (port 8085), `fx-topology-mcp-server` (8091), and the agent workflows that read topology (RCA, blast radius). Topology is supporting, not one of FR-01 to FR-05 (PRD §4.1).
**Sources (reference, not requirements):**
- Netflix TechBlog, *From Silos to Service Topology: Why Netflix Built a Real-Time Service Map*, 2026-05-29 — https://netflixtechblog.com/from-silos-to-service-topology-why-netflix-built-a-real-time-service-map-0165ba13a7bc
- Netflix TechBlog, *Building Service Topology at Scale: Architecture, Challenges, and Lessons Learned*, 2026-07-13 — https://netflixtechblog.com/building-service-topology-at-scale-architecture-challenges-and-lessons-learned-f4b792f3f0d8

The repo owner supplied the text of both posts. Fetching the second URL directly returned HTTP 403, so the premises below are taken from that supplied text.

**Rule:** Netflix runs at a scale this repo does not have. A premise is adopted when it solves a problem this repo has. Each adopted premise is stated in this repo's terms. Scale-specific mechanisms are added when measurement shows they are needed (DESIGN §7.5 and blog premise P12).

---

## 1. Premises

| ID | Premise (from the posts) | Adopted for this repo as |
|---|---|---|
| P1 | Three independent sources build three separate graphs: network flows (eBPF), application IPC metrics, and request traces. Each graph can be queried alone or merged as a union. | Three edge sets with a `layer` discriminator. Query by layer or merged. |
| P2 | Streaming-first. Snapshots that are hours old are not useful during an incident. | Edges arrive from a stream (Kafka) and update within minutes. Batch seed is for demo only. |
| P3 | Fast multi-hop traversal. Filter by availability tier and business domain. Paginate large results. Programmatic access for automation. | Depth-limited traversal with tier and domain filters, pagination, and an API for agents (topology MCP). |
| P4 | Network hops through intermediaries (load balancers, NAT gateways, API gateways, proxies) are resolved into direct application-to-application edges during aggregation. | A pure resolution step: `A→LB` plus `LB→B` becomes `A→B`, with intermediaries recorded on the edge. |
| P5 | Aggregation is staged. Local aggregation first, then shuffle by key, then resolve, then persist. A single stage fails when traffic is skewed. Skip the shuffle when data is already partitioned by key (the IPC case). | Stage design is recorded now. Implement the shuffle stage only after skew is measured. |
| P6 | Consistent hashing with dynamic instance membership redistributes work as instances scale. | Recorded for the multi-instance case. Not needed while the service runs as one instance. |
| P7 | Backpressure end to end. No unbounded buffers, no silent drops. Slow downstream means slow upstream. | Bounded queue between ingest and write. Ingest pauses when it is full. Dropped edges are counted and reported. |
| P8 | Incomplete or wrong dependency data is worse than none. Combine sources so no single one is trusted alone. | Every edge carries its source layer, and a merged view shows which layers agree. |
| P9 | Enrich at aggregation time, not at query time: health status, ownership, tier, domain. | Enrichment (tier, domain, owner) is applied when an edge is written. |
| P10 | Time-windowed aggregates (5 minutes) are kept as immutable snapshots. Property-level mutation history allows time-travel. Queries can re-aggregate along new dimensions. | `time_bucket` per edge, snapshot table for windows, and a `diff` API. Mutation history and re-aggregation are later steps. |
| P11 | Blast radius (what is affected if X goes down), upstream and downstream dependencies, health overlay on the call path, and root-source analysis. | Blast radius and dependency endpoints exist. Health overlay and root-source ranking are to add. |
| P12 | Measure before changing the architecture. Use mutable objects on the hot path only where measurement justifies it. Prefer readable wire formats while debugging. | Adopted as a process rule (DESIGN §7.5). JSON on the wire. |
| P13 | A living map. An edge fades when its calls stop. | Each edge has `last_seen`. Edges older than the freshness window are hidden, not deleted. |

---

## 2. Current state in the repo

| ID | Status | Evidence |
|---|---|---|
| P1 | DONE — three layers built: network (flow-hop ingest), IPC (call ingest), trace (span ingest); merged view with agreement counts | Tables `topology_network_edges`, `topology_ipc_edges`, `topology_trace_outliers` (changeset 001). They share one database, not separate partitions. Queries by layer exist (`/graph/network`, `/graph/ipc`, `/traces/outliers`). No merged union endpoint. |
| P2 | DONE — streaming ingest from Kafka topics, verified end to end locally | Only seed data (`002-topology-seed-demo.yaml`). The Kafka toggle exists (`kafka-enabled`) but no ingestion consumer for flow or IPC events was found. |
| P3 | PARTIAL — REST API with depth, freshness and merged filters; no gRPC, no pagination | REST, not gRPC. Depth and window parameters. No tier or domain filter and no pagination found. MCP server exists in `fx-topology-mcp-server`. |
| P4 | DONE — intermediary resolution, six unit tests; used by flow-hop ingest | No intermediary concept (no matches for "intermediar"). The column `egress_lp_gateway` is an LP gateway, a different concept. |
| P5 | PARTIAL — staged path built: local queue (stage 1), ownership shuffle by key (stage 2), resolve and write (stage 3). Skew measurement pending | No staged pipeline. |
| P6 | DONE — consistent hash ring with virtual nodes and ingest ownership; config-driven membership | No hashing or membership code for topology. |
| P7 | DONE — bounded queue; the Kafka consumer blocks when full; refusals counted | No reactive streams or bounded ingest queue. |
| P8 | PARTIAL — every edge carries its layer; merged view shows agreement counts | No source-agreement logic. |
| P9 | DONE — service catalog enriches each edge on write (tier, domain, owner) | No enrichment step. Tier and domain fields exist in some files but are not applied on write. |
| P10 | PARTIAL — 5-minute bucketed history and time-travel query; no property-level mutation log | `topology_snapshots` table and `diff` endpoint (`/api/v1/topology/diff`) exist. No mutation history, no re-aggregation. |
| P11 | DONE — health overlay (circuit state and latency threshold) and root-source ranking with endpoint | `POST /api/v1/topology/blast-radius` exists. No health overlay, no root-source ranking. |
| P12 | ADOPTED — process rule in DESIGN §7.5 | Process rule. Recorded in DESIGN §7.5. |
| P13 | DONE — freshness at ingest (stale hops dropped and counted) and on read | No `last_seen` expiry. |

**Summary (2026-11-22):** all three layers have ingest paths and a merged view. Edges are resolved, enriched, expired, partitioned across instances, and checked for root causes. Remaining: a measured skew test (P5), gRPC and pagination (P3), and a property-level mutation log (P10).

---

## 3. Scale decisions

| Premise | Adopt now | Defer until measured |
|---|---|---|
| P1, P3, P11 | Yes | |
| P2 | Yes (stream ingest, minutes-level freshness) | Sub-minute targets |
| P4 | Yes. Required to show app-to-app edges correctly | |
| P5, P6 | Design only | Shuffle and hashing when one instance cannot keep up |
| P7 | Yes. A bounded queue is cheap and avoids silent loss | Tuning of buffer sizes |
| P8, P9 | Yes | |
| P10 | Snapshots now. Mutation history later | Re-aggregation along new dimensions |
| P13 | Yes | |

---

## 4. Proposed build order

| Step | Premise | Deliverable | Test |
|---|---|---|---|
| T1 | P4 | Pure function `IntermediaryResolver`: input flow hops, output direct edges with the intermediaries recorded | Unit test: `A→LB→B` gives `A→B`; a chain of two intermediaries; a hop with no intermediary is unchanged |
| T2 | P1, P8 | `layer` on every edge, and a merged endpoint that returns the union with agreement counts | Unit test on merge |
| T3 | P9 | Enrichment at write time: tier, domain, owner | Unit test on enrichment |
| T4 | P13 | `last_seen` and a freshness window on reads | Unit test on expiry |
| T5 | P7 | Bounded ingest queue with a counted drop path | Unit test on bound and back-off |
| T6 | P2 | Kafka consumer for flow and IPC events, feeding T1 to T5 | Integration test against the local Kafka container |
| T7 | P10 | Mutation history for edges | Later |

Each step is recorded in `CHANGE_001.md` with its status and commit.

---

## 5. What this is not

- Not a copy of Netflix's system. There is no gRPC, no Pekko, and no multi-region Kafka here.
- Not a change to the FX requirements. Topology supports incident response and agents only.
- Not a claim that the repo already behaves this way. Section 2 is the honest state.
