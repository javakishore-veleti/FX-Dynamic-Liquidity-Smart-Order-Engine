package com.jk.apps.banking.fx.sor.topology.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.jk.apps.banking.fx.sor.topology.service.RootSource.CallEdge;
import java.util.List;
import org.junit.jupiter.api.Test;

class RootSourceTest {

    @Test
    void deepestDegradedServiceIsTheRoot() {
        // A calls B, B calls C. C is slow. B is degraded only because C is. Root is C.
        List<RootSource.Candidate> out = RootSource.rank(List.of(
                new CallEdge("A", "B", 300.0, "CLOSED"),
                new CallEdge("B", "C", 900.0, "CLOSED")), 200);
        assertEquals(1, out.size());
        assertEquals("C", out.get(0).service());
        assertEquals(1, out.get(0).degradedCallers());
    }

    @Test
    void openCircuitCountsAsDegraded() {
        List<RootSource.Candidate> out = RootSource.rank(List.of(
                new CallEdge("A", "B", 10.0, "OPEN")), 200);
        assertEquals("B", out.get(0).service());
    }

    @Test
    void healthyCallPathHasNoRoot() {
        assertTrue(RootSource.rank(List.of(new CallEdge("A", "B", 20.0, "CLOSED")), 200).isEmpty());
    }

    @Test
    void rankedByBlastRadius() {
        // C is degraded and called by two degraded callers; D by one.
        List<RootSource.Candidate> out = RootSource.rank(List.of(
                new CallEdge("A", "C", 500.0, "CLOSED"),
                new CallEdge("B", "C", 500.0, "CLOSED"),
                new CallEdge("E", "D", 500.0, "CLOSED")), 200);
        assertEquals("C", out.get(0).service());
        assertEquals(2, out.get(0).degradedCallers());
    }
}
