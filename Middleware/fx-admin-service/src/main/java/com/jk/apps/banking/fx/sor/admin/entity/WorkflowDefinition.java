package com.jk.apps.banking.fx.sor.admin.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "workflows")
public class WorkflowDefinition {

    @Id
    @Column(name = "workflow_key", length = 80, nullable = false)
    private String workflowKey;

    @Column(name = "name", length = 200, nullable = false)
    private String name;

    @Lob
    @Column(name = "description")
    private String description;

    @Column(name = "bpmn_resource", length = 255, nullable = false)
    private String bpmnResource;

    @Column(name = "version", length = 20, nullable = false)
    private String version;

    @Column(name = "portal_scope", length = 40, nullable = false)
    private String portalScope;

    @Column(name = "status", length = 20, nullable = false)
    private String status;

    @Column(name = "fr_id", length = 20)
    private String frId;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    public String getWorkflowKey() {
        return workflowKey;
    }

    public void setWorkflowKey(String workflowKey) {
        this.workflowKey = workflowKey;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getBpmnResource() {
        return bpmnResource;
    }

    public void setBpmnResource(String bpmnResource) {
        this.bpmnResource = bpmnResource;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getPortalScope() {
        return portalScope;
    }

    public void setPortalScope(String portalScope) {
        this.portalScope = portalScope;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getFrId() {
        return frId;
    }

    public void setFrId(String frId) {
        this.frId = frId;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
