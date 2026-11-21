package com.jk.apps.banking.fx.sor.topology.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "topology_ipc_edges")
public class TopologyIpcEdge {

    @Id
    private UUID id;

    @Column(name = "src_service", nullable = false, length = 120)
    private String srcService;

    @Column(name = "dst_service", nullable = false, length = 120)
    private String dstService;

    @Column(name = "ccy_pair", length = 16)
    private String ccyPair;

    @Column(name = "window_label", nullable = false, length = 16)
    private String windowLabel;

    @Column(name = "availability_tier", length = 32)
    private String availabilityTier;

    @Column(name = "desk_owner", length = 32)
    private String deskOwner;

    @Column(name = "p99_ms")
    private Double p99Ms;

    @Column(name = "stp_fill_pct")
    private Double stpFillPct;

    @Column(name = "circuit_state", length = 32)
    private String circuitState;

    @Column(name = "observed_at", nullable = false)
    private Instant observedAt;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getSrcService() {
        return srcService;
    }

    public void setSrcService(String srcService) {
        this.srcService = srcService;
    }

    public String getDstService() {
        return dstService;
    }

    public void setDstService(String dstService) {
        this.dstService = dstService;
    }

    public String getCcyPair() {
        return ccyPair;
    }

    public void setCcyPair(String ccyPair) {
        this.ccyPair = ccyPair;
    }

    public String getWindowLabel() {
        return windowLabel;
    }

    public void setWindowLabel(String windowLabel) {
        this.windowLabel = windowLabel;
    }

    public String getAvailabilityTier() {
        return availabilityTier;
    }

    public void setAvailabilityTier(String availabilityTier) {
        this.availabilityTier = availabilityTier;
    }

    public String getDeskOwner() {
        return deskOwner;
    }

    public void setDeskOwner(String deskOwner) {
        this.deskOwner = deskOwner;
    }

    public Double getP99Ms() {
        return p99Ms;
    }

    public void setP99Ms(Double p99Ms) {
        this.p99Ms = p99Ms;
    }

    public Double getStpFillPct() {
        return stpFillPct;
    }

    public void setStpFillPct(Double stpFillPct) {
        this.stpFillPct = stpFillPct;
    }

    public String getCircuitState() {
        return circuitState;
    }

    public void setCircuitState(String circuitState) {
        this.circuitState = circuitState;
    }

    public Instant getObservedAt() {
        return observedAt;
    }

    public void setObservedAt(Instant observedAt) {
        this.observedAt = observedAt;
    }
}
