package com.jk.apps.banking.fx.sor.topology.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jk.apps.banking.fx.sor.topology.domain.entity.TopologyNetworkEdge;
import com.jk.apps.banking.fx.sor.topology.service.EdgeEnrich;
import com.jk.apps.banking.fx.sor.topology.domain.repository.TopologyNetworkEdgeRepository;
import java.time.Instant;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class FlowHopIngestTest {

    @Test
    void resolvesIntermediaryAndWritesOneEdgeInBucket() throws Exception {
        TopologyNetworkEdgeRepository repo = mock(TopologyNetworkEdgeRepository.class);
        FlowHopIngest ingest = new FlowHopIngest(new ObjectMapper(), repo, new EdgeEnrich(Map.of()), Set.of("LB1"), 100);
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
        assertEquals(EdgeEnrich.UNKNOWN, row.getSrcTier());
    }

    @Test
    void enrichesEdgeWithTierAndDomainFromCatalog() throws Exception {
        TopologyNetworkEdgeRepository repo = mock(TopologyNetworkEdgeRepository.class);
        EdgeEnrich enrich = new EdgeEnrich(Map.of(
                "fx-quote-service", new EdgeEnrich.ServiceMeta("TIER_0", "PRICING", "pricing-desk"),
                "fx-qualify-service", new EdgeEnrich.ServiceMeta("TIER_0", "RISK", "risk-desk")));
        FlowHopIngest ingest = new FlowHopIngest(new ObjectMapper(), repo, enrich, Set.of(), 10);
        ingest.accept("{\"src\":\"fx-quote-service\",\"dst\":\"fx-qualify-service\",\"flows\":5}");
        ingest.drainOnce(Instant.parse("2026-11-22T12:00:00Z"));
        ArgumentCaptor<TopologyNetworkEdge> cap = ArgumentCaptor.forClass(TopologyNetworkEdge.class);
        verify(repo, times(1)).save(cap.capture());
        assertEquals("TIER_0", cap.getValue().getSrcTier());
        assertEquals("TIER_0", cap.getValue().getDstTier());
        assertEquals("RISK", cap.getValue().getDomain());
    }

    @Test
    void emptyDrainWritesNothing() {
        TopologyNetworkEdgeRepository repo = mock(TopologyNetworkEdgeRepository.class);
        FlowHopIngest ingest = new FlowHopIngest(new ObjectMapper(), repo, new EdgeEnrich(Map.of()), Set.of(), 10);
        assertEquals(0, ingest.drainOnce(Instant.now()));
        verify(repo, times(0)).save(any());
    }

    @Test
    void rejectsMalformedMessage() {
        FlowHopIngest ingest = new FlowHopIngest(new ObjectMapper(), mock(TopologyNetworkEdgeRepository.class), new EdgeEnrich(Map.of()), Set.of(), 10);
        assertThrows(IllegalArgumentException.class, () -> ingest.parse("{not json"));
    }
}
