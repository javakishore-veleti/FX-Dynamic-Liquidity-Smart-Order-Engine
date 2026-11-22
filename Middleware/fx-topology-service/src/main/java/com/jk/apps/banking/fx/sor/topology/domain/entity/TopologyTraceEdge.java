package com.jk.apps.banking.fx.sor.topology.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

/** Trace layer edge: a parent span's service calling a child span's service within one trace. */
@Entity
@Table(name = "topology_trace_edges")
public class TopologyTraceEdge {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "src_service", nullable = false, length = 120)
    private String srcService;

    @Column(name = "dst_service", nullable = false, length = 120)
    private String dstService;

    @Column(name = "trace_id", nullable = false, length = 64)
    private String traceId;

    @Column(name = "observed_at", nullable = false)
    private Instant observedAt;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getSrcService() { return srcService; }
    public void setSrcService(String srcService) { this.srcService = srcService; }
    public String getDstService() { return dstService; }
    public void setDstService(String dstService) { this.dstService = dstService; }
    public String getTraceId() { return traceId; }
    public void setTraceId(String traceId) { this.traceId = traceId; }
    public Instant getObservedAt() { return observedAt; }
    public void setObservedAt(Instant observedAt) { this.observedAt = observedAt; }
}
