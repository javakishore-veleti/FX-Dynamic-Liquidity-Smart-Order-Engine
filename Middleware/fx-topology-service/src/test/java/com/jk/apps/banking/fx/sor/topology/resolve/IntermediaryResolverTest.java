package com.jk.apps.banking.fx.sor.topology.resolve;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.jk.apps.banking.fx.sor.topology.resolve.IntermediaryResolver.DirectEdge;
import com.jk.apps.banking.fx.sor.topology.resolve.IntermediaryResolver.FlowHop;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

class IntermediaryResolverTest {

    private final IntermediaryResolver resolver = new IntermediaryResolver(Set.of("LB1", "NAT1", "GW1"));

    @Test
    void resolvesLoadBalancerIntoDirectEdge() {
        List<DirectEdge> out = resolver.resolve(List.of(
                new FlowHop("A", "LB1", 100),
                new FlowHop("LB1", "B", 80)));
        assertEquals(1, out.size());
        assertEquals("A", out.get(0).src());
        assertEquals("B", out.get(0).dst());
        assertEquals(80, out.get(0).flows());
        assertEquals(List.of("LB1"), out.get(0).via());
    }

    @Test
    void hopWithoutIntermediaryPassesThrough() {
        List<DirectEdge> out = resolver.resolve(List.of(new FlowHop("A", "B", 42)));
        assertEquals(1, out.size());
        assertEquals(42, out.get(0).flows());
        assertTrue(out.get(0).via().isEmpty());
    }

    @Test
    void resolvesChainOfTwoIntermediaries() {
        List<DirectEdge> out = resolver.resolve(List.of(
                new FlowHop("A", "LB1", 50),
                new FlowHop("LB1", "NAT1", 50),
                new FlowHop("NAT1", "B", 50)));
        assertEquals(1, out.size());
        assertEquals("A", out.get(0).src());
        assertEquals("B", out.get(0).dst());
        assertEquals(List.of("LB1", "NAT1"), out.get(0).via());
    }

    @Test
    void sumsFlowsForSameDirectPairAcrossIntermediaries() {
        List<DirectEdge> out = resolver.resolve(List.of(
                new FlowHop("A", "LB1", 30),
                new FlowHop("LB1", "B", 30),
                new FlowHop("A", "GW1", 20),
                new FlowHop("GW1", "B", 20)));
        assertEquals(1, out.size());
        assertEquals(50, out.get(0).flows());
    }

    @Test
    void intermediaryWithNoOutboundHopIsDropped() {
        List<DirectEdge> out = resolver.resolve(List.of(new FlowHop("A", "LB1", 10)));
        assertTrue(out.isEmpty());
    }

    @Test
    void emptyInputGivesEmptyOutput() {
        assertTrue(resolver.resolve(List.of()).isEmpty());
    }
}
