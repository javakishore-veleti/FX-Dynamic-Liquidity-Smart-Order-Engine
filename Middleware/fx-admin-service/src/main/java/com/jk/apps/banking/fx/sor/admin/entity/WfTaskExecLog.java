package com.jk.apps.banking.fx.sor.admin.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

/** One row per task executed inside a {@code wf_exec_log} run (child table, FK wf_exec_id). */
@Entity
@Table(name = "wf_task_exec_log")
public class WfTaskExecLog {

    @Id
    @Column(name = "task_exec_id", nullable = false)
    private UUID taskExecId;

    @Column(name = "wf_exec_id", nullable = false)
    private UUID wfExecId;

    @Column(name = "task_seq", nullable = false)
    private int taskSeq;

    @Column(name = "task_id", length = 100, nullable = false)
    private String taskId;

    @Column(name = "task_name", length = 200)
    private String taskName;

    @Column(name = "task_type", length = 20)
    private String taskType;

    @Column(name = "status", length = 20, nullable = false)
    private String status;

    @Lob
    @Column(name = "input_json")
    private String inputJson;

    @Lob
    @Column(name = "output_json")
    private String outputJson;

    @Lob
    @Column(name = "error_details")
    private String errorDetails;

    @Column(name = "started_at")
    private Instant startedAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    @Column(name = "duration_ms")
    private Long durationMs;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "created_by", length = 100, nullable = false, updatable = false)
    private String createdBy;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "updated_by", length = 100, nullable = false)
    private String updatedBy;

    public UUID getTaskExecId() {
        return taskExecId;
    }

    public void setTaskExecId(UUID taskExecId) {
        this.taskExecId = taskExecId;
    }

    public UUID getWfExecId() {
        return wfExecId;
    }

    public void setWfExecId(UUID wfExecId) {
        this.wfExecId = wfExecId;
    }

    public int getTaskSeq() {
        return taskSeq;
    }

    public void setTaskSeq(int taskSeq) {
        this.taskSeq = taskSeq;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    public String getTaskType() {
        return taskType;
    }

    public void setTaskType(String taskType) {
        this.taskType = taskType;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getInputJson() {
        return inputJson;
    }

    public void setInputJson(String inputJson) {
        this.inputJson = inputJson;
    }

    public String getOutputJson() {
        return outputJson;
    }

    public void setOutputJson(String outputJson) {
        this.outputJson = outputJson;
    }

    public String getErrorDetails() {
        return errorDetails;
    }

    public void setErrorDetails(String errorDetails) {
        this.errorDetails = errorDetails;
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

    public Long getDurationMs() {
        return durationMs;
    }

    public void setDurationMs(Long durationMs) {
        this.durationMs = durationMs;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }

}
