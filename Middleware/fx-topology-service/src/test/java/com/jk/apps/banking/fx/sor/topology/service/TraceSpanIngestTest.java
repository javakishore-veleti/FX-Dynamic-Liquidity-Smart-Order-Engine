package com.jk.apps.banking.fx.sor.topology.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jk.apps.banking.fx.sor.topology.domain.entity.TopologyTraceEdge;
import com.jk.apps.banking.fx.sor.topology.domain.repository.TopologyTraceEdgeRepository;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class TraceSpanIngestTest {

    @Test
    void childSpanYieldsParentToChildEdge() {
        TopologyTraceEdgeRepository repo = mock(TopologyTraceEdgeRepository.class);
        TraceSpanIngest ingest = new TraceSpanIngest(new ObjectMapper(), repo);
        String now = Instant.now().toString();
        assertTrue(ingest.accept("{\"traceId\":\"t1\",\"service\":\"fx-qualify-service\",\"parentService\":\"fx-quote-service\",\"observedAt\":\"" + now + "\"}"));
        ArgumentCaptor<TopologyTraceEdge> cap = ArgumentCaptor.forClass(TopologyTraceEdge.class);
        verify(repo, times(1)).save(cap.capture());
        assertEquals("fx-quote-service", cap.getValue().getSrcService());
        assertEquals("fx-qualify-service", cap.getValue().getDstService());
    }

    @Test
    void rootSpanAndStaleSpanYieldNothing() {
        TopologyTraceEdgeRepository repo = mock(TopologyTraceEdgeRepository.class);
        TraceSpanIngest ingest = new TraceSpanIngest(new ObjectMapper(), repo);
        assertFalse(ingest.accept("{\"traceId\":\"t2\",\"service\":\"fx-quote-service\",\"observedAt\":\"" + Instant.now() + "\"}"));
        assertFalse(ingest.accept("{\"traceId\":\"t3\",\"service\":\"A\",\"parentService\":\"B\",\"observedAt\":\"2000-01-01T00:00:00Z\"}"));
        assertEquals(1, ingest.staleCount());
    }
}
