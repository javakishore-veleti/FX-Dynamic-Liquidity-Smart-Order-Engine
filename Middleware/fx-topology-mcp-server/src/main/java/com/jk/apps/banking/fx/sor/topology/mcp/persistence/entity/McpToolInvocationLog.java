package com.jk.apps.banking.fx.sor.topology.mcp.persistence.entity;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "mcp_tool_invocation_log")
public class McpToolInvocationLog {

    @Id
    @Column(name = "invocation_id", nullable = false)
    private UUID invocationId;

    @Column(name = "tool_name", nullable = false, length = 100)
    private String toolName;

    @Column(name = "request_payload", nullable = false, columnDefinition = "clob")
    private String requestPayload;

    @Column(name = "response_payload", columnDefinition = "clob")
    private String responsePayload;

    @Column(name = "status", nullable = false, length = 20)
    private String status;

    @Column(name = "safety_tier", nullable = false, length = 40)
    private String safetyTier;

    @Column(name = "dry_run", nullable = false)
    private boolean dryRun = true;

    @Column(name = "error_details", columnDefinition = "clob")
    private String errorDetails;

    @Column(name = "duration_ms")
    private Long durationMs;

    @Column(name = "triggered_by", nullable = false, length = 100)
    private String triggeredBy = "MCP_CLIENT";

    @Column(name = "started_at", nullable = false)
    private Instant startedAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    public UUID getInvocationId() {
        return invocationId;
    }

    public void setInvocationId(UUID invocationId) {
        this.invocationId = invocationId;
    }

    public String getToolName() {
        return toolName;
    }

    public void setToolName(String toolName) {
        this.toolName = toolName;
    }

    public String getRequestPayload() {
        return requestPayload;
    }

    public void setRequestPayload(String requestPayload) {
        this.requestPayload = requestPayload;
    }

    public String getResponsePayload() {
        return responsePayload;
    }

    public void setResponsePayload(String responsePayload) {
        this.responsePayload = responsePayload;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getSafetyTier() {
        return safetyTier;
    }

    public void setSafetyTier(String safetyTier) {
        this.safetyTier = safetyTier;
    }

    public boolean isDryRun() {
        return dryRun;
    }

    public void setDryRun(boolean dryRun) {
        this.dryRun = dryRun;
    }

    public String getErrorDetails() {
        return errorDetails;
    }

    public void setErrorDetails(String errorDetails) {
        this.errorDetails = errorDetails;
    }

    public Long getDurationMs() {
        return durationMs;
    }

    public void setDurationMs(Long durationMs) {
        this.durationMs = durationMs;
    }

    public String getTriggeredBy() {
        return triggeredBy;
    }

    public void setTriggeredBy(String triggeredBy) {
        this.triggeredBy = triggeredBy;
    }

    public Instant getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(Instant startedAt) {
        this.startedAt = startedAt;
    }

    public Instant getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(Instant completedAt) {
        this.completedAt = completedAt;
    }
}
