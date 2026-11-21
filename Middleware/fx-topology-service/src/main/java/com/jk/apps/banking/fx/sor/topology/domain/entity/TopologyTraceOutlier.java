package com.jk.apps.banking.fx.sor.topology.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "topology_trace_outliers")
public class TopologyTraceOutlier {

    @Id
    private UUID id;

    @Column(name = "service_name", nullable = false, length = 120)
    private String serviceName;

    @Column(name = "client_order_id", length = 64)
    private String clientOrderId;

    @Column(name = "drools_rule_version", length = 32)
    private String droolsRuleVersion;

    @Column(name = "knockout_reason", length = 128)
    private String knockoutReason;

    @Column(name = "winning_lp_id", length = 64)
    private String winningLpId;

    @Column(name = "latency_ms", nullable = false)
    private double latencyMs;

    @Column(name = "observed_at", nullable = false)
    private Instant observedAt;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getClientOrderId() {
        return clientOrderId;
    }

    public void setClientOrderId(String clientOrderId) {
        this.clientOrderId = clientOrderId;
    }

    public String getDroolsRuleVersion() {
        return droolsRuleVersion;
    }

    public void setDroolsRuleVersion(String droolsRuleVersion) {
        this.droolsRuleVersion = droolsRuleVersion;
    }

    public String getKnockoutReason() {
        return knockoutReason;
    }

    public void setKnockoutReason(String knockoutReason) {
        this.knockoutReason = knockoutReason;
    }

    public String getWinningLpId() {
        return winningLpId;
    }

    public void setWinningLpId(String winningLpId) {
        this.winningLpId = winningLpId;
    }

    public double getLatencyMs() {
        return latencyMs;
    }

    public void setLatencyMs(double latencyMs) {
        this.latencyMs = latencyMs;
    }

    public Instant getObservedAt() {
        return observedAt;
    }

    public void setObservedAt(Instant observedAt) {
        this.observedAt = observedAt;
    }
}
