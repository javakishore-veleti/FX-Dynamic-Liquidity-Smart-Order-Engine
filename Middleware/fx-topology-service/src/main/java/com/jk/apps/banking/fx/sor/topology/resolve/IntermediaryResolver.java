package com.jk.apps.banking.fx.sor.topology.resolve;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * Premise P4 (Netflix TechBlog): network hops through intermediaries (load balancers, NAT gateways,
 * API gateways, proxies) are resolved into direct application-to-application edges.
 *
 * A flow A->LB and a flow LB->B become one edge A->B. Its flow count is the smaller of the two
 * hops, because each flow crosses both. The intermediaries it passed through are kept on the edge.
 * Intermediaries are resolved one at a time, in sorted order, so chains such as A->LB1->NAT1->B
 * resolve in full. Edges that enter an intermediary with no way out are dropped.
 *
 * Pure function: no I/O, no shared state. Safe to call from any thread.
 */
public final class IntermediaryResolver {

    static final int MAX_ROUNDS = 5;

    public record FlowHop(String src, String dst, long flows) {
    }

    public record DirectEdge(String src, String dst, long flows, List<String> via) {
    }

    private final List<String> intermediaries;

    public IntermediaryResolver(Set<String> intermediaries) {
        this.intermediaries = List.copyOf(new TreeSet<>(intermediaries));
    }

    public List<DirectEdge> resolve(List<FlowHop> hops) {
        List<DirectEdge> edges = new ArrayList<>();
        for (FlowHop h : hops) {
            edges.add(new DirectEdge(h.src(), h.dst(), h.flows(), List.of()));
        }
        for (int round = 0; round < MAX_ROUNDS; round++) {
            boolean touched = false;
            for (String mid : intermediaries) {
                List<DirectEdge> inbound = new ArrayList<>();
                List<DirectEdge> outbound = new ArrayList<>();
                for (DirectEdge e : edges) {
                    if (mid.equals(e.dst())) {
                        inbound.add(e);
                    }
                    if (mid.equals(e.src())) {
                        outbound.add(e);
                    }
                }
                if (inbound.isEmpty() && outbound.isEmpty()) {
                    continue;
                }
                touched = true;
                List<DirectEdge> joined = new ArrayList<>();
                for (DirectEdge in : inbound) {
                    for (DirectEdge out : outbound) {
                        if (in.src().equals(out.dst())) {
                            continue; // a hop that loops back to its own source adds nothing
                        }
                        List<String> via = new ArrayList<>(in.via());
                        via.add(mid);
                        via.addAll(out.via());
                        joined.add(new DirectEdge(in.src(), out.dst(),
                                Math.min(in.flows(), out.flows()), distinct(via)));
                    }
                }
                edges.removeIf(e -> mid.equals(e.src()) || mid.equals(e.dst()));
                edges.addAll(joined);
                edges = merge(edges);
            }
            if (!touched) {
                break;
            }
        }
        return edges.stream()
                .filter(e -> !intermediaries.contains(e.src()) && !intermediaries.contains(e.dst()))
                .toList();
    }

    /** Sums flow counts for edges with the same endpoints, keeping the union of intermediaries. */
    private static List<DirectEdge> merge(List<DirectEdge> edges) {
        Map<String, DirectEdge> byKey = new LinkedHashMap<>();
        for (DirectEdge e : edges) {
            String key = e.src() + "\u0000" + e.dst();
            byKey.merge(key, e, (a, b) -> {
                List<String> via = new ArrayList<>(a.via());
                via.addAll(b.via());
                return new DirectEdge(a.src(), a.dst(), a.flows() + b.flows(), distinct(via));
            });
        }
        return new ArrayList<>(byKey.values());
    }

    private static List<String> distinct(List<String> values) {
        return List.copyOf(new LinkedHashSet<>(values));
    }
}
