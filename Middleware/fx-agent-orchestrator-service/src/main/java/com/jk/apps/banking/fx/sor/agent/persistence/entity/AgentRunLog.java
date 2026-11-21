package com.jk.apps.banking.fx.sor.agent.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "agent_run_log")
public class AgentRunLog {

    @Id
    @Column(name = "run_id", nullable = false)
    private UUID runId;

    @Column(name = "agent_type", nullable = false, length = 50)
    private String agentType;

    @Column(name = "dry_run", nullable = false)
    private boolean dryRun = true;

    @Column(name = "status", nullable = false, length = 20)
    private String status;

    @Column(name = "request_params", nullable = false, columnDefinition = "clob")
    private String requestParams;

    @Column(name = "mcp_tools_invoked", columnDefinition = "clob")
    private String mcpToolsInvoked;

    @Column(name = "result_summary", columnDefinition = "clob")
    private String resultSummary;

    @Column(name = "approval_token", length = 200)
    private String approvalToken;

    @Column(name = "triggered_by", nullable = false, length = 100)
    private String triggeredBy = "SPRING_AI_AGENT";

    @Column(name = "started_at", nullable = false)
    private Instant startedAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    @Column(name = "duration_ms")
    private Long durationMs;

    @Column(name = "error_details", columnDefinition = "clob")
    private String errorDetails;

    public UUID getRunId() {
        return runId;
    }

    public void setRunId(UUID runId) {
        this.runId = runId;
    }

    public String getAgentType() {
        return agentType;
    }

    public void setAgentType(String agentType) {
        this.agentType = agentType;
    }

    public boolean isDryRun() {
        return dryRun;
    }

    public void setDryRun(boolean dryRun) {
        this.dryRun = dryRun;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getRequestParams() {
        return requestParams;
    }

    public void setRequestParams(String requestParams) {
        this.requestParams = requestParams;
    }

    public String getMcpToolsInvoked() {
        return mcpToolsInvoked;
    }

    public void setMcpToolsInvoked(String mcpToolsInvoked) {
        this.mcpToolsInvoked = mcpToolsInvoked;
    }

    public String getResultSummary() {
        return resultSummary;
    }

    public void setResultSummary(String resultSummary) {
        this.resultSummary = resultSummary;
    }

    public String getApprovalToken() {
        return approvalToken;
    }

    public void setApprovalToken(String approvalToken) {
        this.approvalToken = approvalToken;
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

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Instant getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(Instant completedAt) {
        this.completedAt = completedAt;
    }

    public Long getDurationMs() {
        return durationMs;
    }

    public void setDurationMs(Long durationMs) {
        this.durationMs = durationMs;
    }

    public String getErrorDetails() {
        return errorDetails;
    }

    public void setErrorDetails(String errorDetails) {
        this.errorDetails = errorDetails;
    }
}
