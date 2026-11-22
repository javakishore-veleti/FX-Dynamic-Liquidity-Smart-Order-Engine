package com.jk.apps.banking.fx.sor.topology.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

/** One property change on a topology edge, e.g. flow_count from 10 to 25 (premise P10). */
@Entity
@Table(name = "topology_edge_mutations")
public class TopologyEdgeMutation {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "src_service", nullable = false, length = 120)
    private String srcService;

    @Column(name = "dst_service", nullable = false, length = 120)
    private String dstService;

    @Column(name = "property", nullable = false, length = 64)
    private String property;

    @Column(name = "old_value", length = 255)
    private String oldValue;

    @Column(name = "new_value", nullable = false, length = 255)
    private String newValue;

    @Column(name = "changed_at", nullable = false)
    private Instant changedAt;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getSrcService() { return srcService; }
    public void setSrcService(String v) { this.srcService = v; }
    public String getDstService() { return dstService; }
    public void setDstService(String v) { this.dstService = v; }
    public String getProperty() { return property; }
    public void setProperty(String v) { this.property = v; }
    public String getOldValue() { return oldValue; }
    public void setOldValue(String v) { this.oldValue = v; }
    public String getNewValue() { return newValue; }
    public void setNewValue(String v) { this.newValue = v; }
    public Instant getChangedAt() { return changedAt; }
    public void setChangedAt(Instant v) { this.changedAt = v; }
}
