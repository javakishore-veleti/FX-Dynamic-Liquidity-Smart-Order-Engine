package com.jk.apps.banking.fx.sor.admin.dto;

import java.time.Instant;
import java.util.UUID;

public class WorkflowStatusResponse {

    private UUID wfExecId;
    private String workflowType;
    private String workflowKey;
    private String status;
    private Instant startedAt;
    private Instant completedAt;
    private int batchesCompleted;
    private long recordsInserted;
    private String errorDetails;
    private String executionSummary;

    public UUID getWfExecId() {
        return wfExecId;
    }

    public void setWfExecId(UUID wfExecId) {
        this.wfExecId = wfExecId;
    }

    public String getWorkflowType() {
        return workflowType;
    }

    public void setWorkflowType(String workflowType) {
        this.workflowType = workflowType;
    }

    public String getWorkflowKey() {
        return workflowKey;
    }

    public void setWorkflowKey(String workflowKey) {
        this.workflowKey = workflowKey;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
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

    public int getBatchesCompleted() {
        return batchesCompleted;
    }

    public void setBatchesCompleted(int batchesCompleted) {
        this.batchesCompleted = batchesCompleted;
    }

    public long getRecordsInserted() {
        return recordsInserted;
    }

    public void setRecordsInserted(long recordsInserted) {
        this.recordsInserted = recordsInserted;
    }

    public String getErrorDetails() {
        return errorDetails;
    }

    public void setErrorDetails(String errorDetails) {
        this.errorDetails = errorDetails;
    }

    public String getExecutionSummary() {
        return executionSummary;
    }

    public void setExecutionSummary(String executionSummary) {
        this.executionSummary = executionSummary;
    }
}
