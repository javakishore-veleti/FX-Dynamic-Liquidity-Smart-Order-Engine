package com.jk.apps.banking.fx.sor.topology.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jk.apps.banking.fx.sor.topology.domain.entity.TopologyTraceEdge;
import com.jk.apps.banking.fx.sor.topology.domain.repository.TopologyTraceEdgeRepository;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.stereotype.Component;

/**
 * Premise P1, trace layer: request-level edges from distributed-trace spans. Message on
 * fx.topology.span.v1: {"traceId","service","parentService","observedAt"}. A span whose parent lives
 * in another service yields one edge parentService to service. Root spans (no parent) yield nothing.
 * Stale spans are dropped and counted (P13). Trace data is sampled upstream, so the layer shows
 * common request paths, not every request.
 */
@Component
public class TraceSpanIngest {

    private final ObjectMapper mapper;
    private final TopologyTraceEdgeRepository repository;
    private final Freshness freshness;
    private final AtomicLong stale = new AtomicLong();

    public TraceSpanIngest(ObjectMapper mapper, TopologyTraceEdgeRepository repository) {
        this(mapper, repository, 10);
    }

    TraceSpanIngest(ObjectMapper mapper, TopologyTraceEdgeRepository repository, long freshnessMinutes) {
        this.mapper = mapper;
        this.repository = repository;
        this.freshness = new Freshness(Duration.ofMinutes(freshnessMinutes));
    }

    public long staleCount() {
        return stale.get();
    }

    /** Returns true when an edge was written. */
    public boolean accept(String json) {
        try {
            JsonNode n = mapper.readTree(json);
            Instant observed = n.hasNonNull("observedAt") ? Instant.parse(n.get("observedAt").asText()) : Instant.now();
            if (!freshness.isFresh(observed, Instant.now())) {
                stale.incrementAndGet();
                return false;
            }
            if (!n.hasNonNull("parentService")) {
                return false;
            }
            String parent = n.get("parentService").asText();
            String service = n.get("service").asText();
            if (parent.equals(service)) {
                return false;
            }
            TopologyTraceEdge row = new TopologyTraceEdge();
            row.setId(UUID.randomUUID());
            row.setSrcService(parent);
            row.setDstService(service);
            row.setTraceId(n.hasNonNull("traceId") ? n.get("traceId").asText() : "unknown");
            row.setObservedAt(observed);
            repository.save(row);
            return true;
        } catch (Exception ex) {
            throw new IllegalArgumentException("invalid span message: " + json, ex);
        }
    }
}
