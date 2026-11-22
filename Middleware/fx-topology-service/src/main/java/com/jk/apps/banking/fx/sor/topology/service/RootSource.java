package com.jk.apps.banking.fx.sor.topology.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Premise P11 (Netflix TechBlog): health overlay on the call path and root-source analysis.
 *
 * A call edge is degraded when its circuit is OPEN or its p99 latency is above a threshold.
 * A service is degraded when any edge into it is degraded. A root source is a degraded service
 * whose own downstream calls are all healthy: the failure starts there, not further down.
 * Root sources are ranked by how many degraded callers sit above them (blast radius).
 *
 * Pure function: no I/O.
 */
public final class RootSource {

    public record CallEdge(String caller, String callee, Double p99Ms, String circuitState) {
    }

    public record Candidate(String service, int degradedCallers) {
    }

    private RootSource() {
    }

    public static boolean degraded(CallEdge e, double thresholdMs) {
        if ("OPEN".equalsIgnoreCase(e.circuitState())) {
            return true;
        }
        return e.p99Ms() != null && e.p99Ms() > thresholdMs;
    }

    public static List<Candidate> rank(List<CallEdge> edges, double thresholdMs) {
        Map<String, Set<String>> degradedOut = new HashMap<>();
        Map<String, Set<String>> degradedIn = new HashMap<>();
        Set<String> degradedServices = new HashSet<>();
        for (CallEdge e : edges) {
            if (degraded(e, thresholdMs)) {
                degradedOut.computeIfAbsent(e.caller(), k -> new HashSet<>()).add(e.callee());
                degradedIn.computeIfAbsent(e.callee(), k -> new HashSet<>()).add(e.caller());
                degradedServices.add(e.callee());
                degradedServices.add(e.caller());
            }
        }
        List<Candidate> out = new ArrayList<>();
        for (String svc : degradedServices) {
            // Root: degraded, and none of its downstream calls is itself degraded.
            if (!degradedOut.getOrDefault(svc, Set.of()).isEmpty()) {
                continue;
            }
            Set<String> callers = degradedIn.getOrDefault(svc, Set.of());
            if (callers.isEmpty()) {
                continue; // degraded only as a caller with no degraded callee: not a root
            }
            out.add(new Candidate(svc, callers.size()));
        }
        out.sort((a, b) -> Integer.compare(b.degradedCallers(), a.degradedCallers()));
        return out;
    }
}
