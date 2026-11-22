package com.jk.apps.banking.fx.sor.topology.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jk.apps.banking.fx.sor.topology.domain.entity.TopologyNetworkEdge;
import com.jk.apps.banking.fx.sor.topology.domain.repository.TopologyNetworkEdgeRepository;
import java.time.Instant;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class FlowHopIngestTest {

    @Test
    void resolvesIntermediaryAndWritesOneEdgeInBucket() throws Exception {
        TopologyNetworkEdgeRepository repo = mock(TopologyNetworkEdgeRepository.class);
        FlowHopIngest ingest = new FlowHopIngest(new ObjectMapper(), repo, Set.of("LB1"), 100);
        ingest.accept("{\"src\":\"A\",\"dst\":\"LB1\",\"flows\":100}");
        ingest.accept("{\"src\":\"LB1\",\"dst\":\"B\",\"flows\":80}");

        int written = ingest.drainOnce(Instant.parse("2026-11-22T12:03:30Z"));

        assertEquals(1, written);
        ArgumentCaptor<TopologyNetworkEdge> cap = ArgumentCaptor.forClass(TopologyNetworkEdge.class);
        verify(repo, times(1)).save(cap.capture());
        TopologyNetworkEdge row = cap.getValue();
        assertEquals("A", row.getSrcService());
        assertEquals("B", row.getDstService());
        assertEquals(80, row.getFlowCount());
        assertEquals("2026-11-22T12:00", row.getTimeBucket());
    }

    @Test
    void emptyDrainWritesNothing() {
        TopologyNetworkEdgeRepository repo = mock(TopologyNetworkEdgeRepository.class);
        FlowHopIngest ingest = new FlowHopIngest(new ObjectMapper(), repo, Set.of(), 10);
        assertEquals(0, ingest.drainOnce(Instant.now()));
        verify(repo, times(0)).save(any());
    }

    @Test
    void rejectsMalformedMessage() {
        FlowHopIngest ingest = new FlowHopIngest(new ObjectMapper(), mock(TopologyNetworkEdgeRepository.class), Set.of(), 10);
        assertThrows(IllegalArgumentException.class, () -> ingest.parse("{not json"));
    }
}
