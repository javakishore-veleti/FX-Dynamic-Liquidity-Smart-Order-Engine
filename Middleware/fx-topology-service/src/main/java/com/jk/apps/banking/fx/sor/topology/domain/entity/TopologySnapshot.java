package com.jk.apps.banking.fx.sor.topology.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "topology_snapshots")
public class TopologySnapshot {

    @Id
    private UUID id;

    @Column(name = "snapshot_ts", nullable = false)
    private Instant snapshotTs;

    @Column(name = "layer", nullable = false, length = 32)
    private String layer;

    @Column(name = "edge_count", nullable = false)
    private int edgeCount;

    @Lob
    @Column(name = "node_summary")
    private String nodeSummary;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Instant getSnapshotTs() {
        return snapshotTs;
    }

    public void setSnapshotTs(Instant snapshotTs) {
        this.snapshotTs = snapshotTs;
    }

    public String getLayer() {
        return layer;
    }

    public void setLayer(String layer) {
        this.layer = layer;
    }

    public int getEdgeCount() {
        return edgeCount;
    }

    public void setEdgeCount(int edgeCount) {
        this.edgeCount = edgeCount;
    }

    public String getNodeSummary() {
        return nodeSummary;
    }

    public void setNodeSummary(String nodeSummary) {
        this.nodeSummary = nodeSummary;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
