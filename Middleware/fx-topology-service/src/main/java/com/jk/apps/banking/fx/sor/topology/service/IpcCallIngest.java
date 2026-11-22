package com.jk.apps.banking.fx.sor.topology.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jk.apps.banking.fx.sor.topology.domain.entity.TopologyIpcEdge;
import com.jk.apps.banking.fx.sor.topology.domain.repository.TopologyIpcEdgeRepository;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import org.springframework.stereotype.Component;

/**
 * Premise P1 (Netflix TechBlog), IPC layer: application-level calls between services, as reported by
 * the calling service. Message on fx.topology.ipc-call.v1: {"caller","callee","p99Ms","observedAt"}.
 * Application-level edges need no intermediary resolution, so each message is written directly.
 * Tier and owner come from the service catalog (premise P9). Stale messages are dropped and counted (P13).
 */
@Component
public class IpcCallIngest {

    static final String WINDOW_FMT = "%s";

    private final ObjectMapper mapper;
    private final TopologyIpcEdgeRepository repository;
    private final EdgeEnrich enrich;
    private final Freshness freshness;
    private final java.util.concurrent.atomic.AtomicLong stale = new java.util.concurrent.atomic.AtomicLong();

    @org.springframework.beans.factory.annotation.Autowired
    public IpcCallIngest(ObjectMapper mapper, TopologyIpcEdgeRepository repository, EdgeEnrich enrich) {
        this(mapper, repository, enrich, 10);
    }

    IpcCallIngest(ObjectMapper mapper, TopologyIpcEdgeRepository repository, EdgeEnrich enrich, long freshnessMinutes) {
        this.mapper = mapper;
        this.repository = repository;
        this.enrich = enrich;
        this.freshness = new Freshness(Duration.ofMinutes(freshnessMinutes));
    }

    public long staleCount() {
        return stale.get();
    }

    /** Parses one IPC call message and writes one edge row. Returns false if it was stale. */
    public boolean accept(String json) {
        try {
            JsonNode n = mapper.readTree(json);
            Instant observed = n.hasNonNull("observedAt") ? Instant.parse(n.get("observedAt").asText()) : Instant.now();
            if (!freshness.isFresh(observed, Instant.now())) {
                stale.incrementAndGet();
                return false;
            }
            String caller = n.get("caller").asText();
            String callee = n.get("callee").asText();
            EdgeEnrich.Enriched meta = enrich.enrich(caller, callee);
            TopologyIpcEdge row = new TopologyIpcEdge();
            row.setId(UUID.randomUUID());
            row.setSrcService(caller);
            row.setDstService(callee);
            row.setWindowLabel(FlowHopIngest.bucketOf(observed));
            row.setAvailabilityTier(meta.dstTier());
            row.setDeskOwner(meta.owner());
            row.setP99Ms(n.hasNonNull("p99Ms") ? n.get("p99Ms").asDouble() : null);
            row.setObservedAt(observed);
            repository.save(row);
            return true;
        } catch (IllegalArgumentException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new IllegalArgumentException("invalid ipc-call message: " + json, ex);
        }
    }
}
