package com.jk.apps.banking.fx.sor.admin.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "wf_exec_log")
public class WfExecLog {

    @Id
    @Column(name = "wf_exec_id", nullable = false)
    private UUID wfExecId;

    @Column(name = "workflow_type", length = 50, nullable = false)
    private String workflowType;

    /** FK-style link to master {@code workflows.workflow_key} for BPMN definitions. */
    @Column(name = "workflow_key", length = 80)
    private String workflowKey;

    @Lob
    @Column(name = "dataset_types")
    private String datasetTypes;

    @Column(name = "num_batches", nullable = false)
    private int numBatches;

    @Column(name = "batch_size", nullable = false)
    private int batchSize;

    @Column(name = "batches_completed", nullable = false)
    private int batchesCompleted;

    @Column(name = "records_inserted", nullable = false)
    private long recordsInserted;

    @Lob
    @Column(name = "request_params", nullable = false)
    private String requestParams;

    @Lob
    @Column(name = "execution_summary")
    private String executionSummary;

    @Column(name = "status", length = 20, nullable = false)
    private String status;

    @Column(name = "started_at", nullable = false)
    private Instant startedAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    @Column(name = "duration_ms")
    private Long durationMs;

    @Column(name = "triggered_by", length = 100, nullable = false)
    private String triggeredBy;

    @Lob
    @Column(name = "error_details")
    private String errorDetails;

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

    public String getDatasetTypes() {
        return datasetTypes;
    }

    public void setDatasetTypes(String datasetTypes) {
        this.datasetTypes = datasetTypes;
    }

    public int getNumBatches() {
        return numBatches;
    }

    public void setNumBatches(int numBatches) {
        this.numBatches = numBatches;
    }

    public int getBatchSize() {
        return batchSize;
    }

    public void setBatchSize(int batchSize) {
        this.batchSize = batchSize;
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

    public String getRequestParams() {
        return requestParams;
    }

    public void setRequestParams(String requestParams) {
        this.requestParams = requestParams;
    }

    public String getExecutionSummary() {
        return executionSummary;
    }

    public void setExecutionSummary(String executionSummary) {
        this.executionSummary = executionSummary;
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

    public String getTriggeredBy() {
        return triggeredBy;
    }

    public void setTriggeredBy(String triggeredBy) {
        this.triggeredBy = triggeredBy;
    }

    public String getErrorDetails() {
        return errorDetails;
    }

    public void setErrorDetails(String errorDetails) {
        this.errorDetails = errorDetails;
    }
}
