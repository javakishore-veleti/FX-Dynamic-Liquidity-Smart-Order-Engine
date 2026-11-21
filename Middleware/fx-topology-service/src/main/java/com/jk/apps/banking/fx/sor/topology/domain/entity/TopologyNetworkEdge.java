package com.jk.apps.banking.fx.sor.topology.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "topology_network_edges")
public class TopologyNetworkEdge {

    @Id
    private UUID id;

    @Column(name = "src_service", nullable = false, length = 120)
    private String srcService;

    @Column(name = "dst_service", nullable = false, length = 120)
    private String dstService;

    @Column(name = "time_bucket", nullable = false, length = 32)
    private String timeBucket;

    @Column(name = "pod_namespace", length = 64)
    private String podNamespace;

    @Column(name = "az_zone", length = 32)
    private String azZone;

    @Column(name = "egress_lp_gateway", length = 64)
    private String egressLpGateway;

    @Column(name = "socket_retransmits", nullable = false)
    private int socketRetransmits;

    @Column(name = "flow_count", nullable = false)
    private long flowCount;

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

    public String getTimeBucket() {
        return timeBucket;
    }

    public void setTimeBucket(String timeBucket) {
        this.timeBucket = timeBucket;
    }

    public String getPodNamespace() {
        return podNamespace;
    }

    public void setPodNamespace(String podNamespace) {
        this.podNamespace = podNamespace;
    }

    public String getAzZone() {
        return azZone;
    }

    public void setAzZone(String azZone) {
        this.azZone = azZone;
    }

    public String getEgressLpGateway() {
        return egressLpGateway;
    }

    public void setEgressLpGateway(String egressLpGateway) {
        this.egressLpGateway = egressLpGateway;
    }

    public int getSocketRetransmits() {
        return socketRetransmits;
    }

    public void setSocketRetransmits(int socketRetransmits) {
        this.socketRetransmits = socketRetransmits;
    }

    public long getFlowCount() {
        return flowCount;
    }

    public void setFlowCount(long flowCount) {
        this.flowCount = flowCount;
    }

    public Instant getObservedAt() {
        return observedAt;
    }

    public void setObservedAt(Instant observedAt) {
        this.observedAt = observedAt;
    }
}
