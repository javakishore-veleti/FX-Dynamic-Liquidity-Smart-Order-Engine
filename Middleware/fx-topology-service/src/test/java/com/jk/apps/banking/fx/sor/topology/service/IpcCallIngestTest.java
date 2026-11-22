package com.jk.apps.banking.fx.sor.topology.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jk.apps.banking.fx.sor.topology.domain.entity.TopologyIpcEdge;
import com.jk.apps.banking.fx.sor.topology.domain.repository.TopologyIpcEdgeRepository;
import java.time.Instant;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class IpcCallIngestTest {

    @Test
    void writesCallerCalleeTierOwnerAndP99() {
        TopologyIpcEdgeRepository repo = mock(TopologyIpcEdgeRepository.class);
        EdgeEnrich enrich = new EdgeEnrich(Map.of(
                "fx-quote-service", new EdgeEnrich.ServiceMeta("TIER_0", "PRICING", "pricing-desk"),
                "fx-qualify-service", new EdgeEnrich.ServiceMeta("TIER_0", "RISK", "risk-desk")));
        IpcCallIngest ingest = new IpcCallIngest(new ObjectMapper(), repo, enrich);
        String now = Instant.now().toString();
        ingest.accept("{\"caller\":\"fx-quote-service\",\"callee\":\"fx-qualify-service\",\"p99Ms\":12.5,\"observedAt\":\"" + now + "\"}");
        ArgumentCaptor<TopologyIpcEdge> cap = ArgumentCaptor.forClass(TopologyIpcEdge.class);
        verify(repo, times(1)).save(cap.capture());
        TopologyIpcEdge row = cap.getValue();
        assertEquals("fx-quote-service", row.getSrcService());
        assertEquals("fx-qualify-service", row.getDstService());
        assertEquals("TIER_0", row.getAvailabilityTier());
        assertEquals("risk-desk", row.getDeskOwner());
        assertEquals(12.5, row.getP99Ms());
    }

    @Test
    void dropsStaleCallAndCountsIt() {
        TopologyIpcEdgeRepository repo = mock(TopologyIpcEdgeRepository.class);
        IpcCallIngest ingest = new IpcCallIngest(new ObjectMapper(), repo, new EdgeEnrich(Map.of()));
        assertFalse(ingest.accept("{\"caller\":\"A\",\"callee\":\"B\",\"observedAt\":\"2000-01-01T00:00:00Z\"}"));
        assertEquals(1, ingest.staleCount());
        verify(repo, times(0)).save(any());
    }
}
