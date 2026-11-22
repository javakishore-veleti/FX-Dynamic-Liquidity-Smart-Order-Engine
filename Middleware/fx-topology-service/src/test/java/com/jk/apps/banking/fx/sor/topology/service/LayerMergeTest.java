package com.jk.apps.banking.fx.sor.topology.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.jk.apps.banking.fx.sor.topology.service.LayerMerge.Edge;
import com.jk.apps.banking.fx.sor.topology.service.LayerMerge.Merged;
import java.util.List;
import org.junit.jupiter.api.Test;

class LayerMergeTest {

    @Test
    void edgeReportedByTwoLayersHasAgreementTwo() {
        List<Merged> out = LayerMerge.merge(List.of(
                new Edge("network", "A", "B"),
                new Edge("ipc", "A", "B"),
                new Edge("network", "B", "C")));
        Merged ab = out.stream().filter(m -> m.src().equals("A")).findFirst().orElseThrow();
        assertEquals(2, ab.agreement());
        assertEquals(List.of("ipc", "network"), ab.layers());
        Merged bc = out.stream().filter(m -> m.src().equals("B")).findFirst().orElseThrow();
        assertEquals(1, bc.agreement());
    }
}
