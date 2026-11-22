package com.jk.apps.banking.fx.sor.admin.dto;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** Portal-facing BPMN process + task execution view (backed by wf_exec_log). */
public class BpmnProcessView {

    private UUID wfExecId;
    private String processKey;
    private String workflowType;
    private String status;
    private String currentTaskId;
    private String currentTaskName;
    private String triggeredBy;
    private Instant startedAt;
    private Instant completedAt;
    private String bpmnResource;
    private List<BpmnTaskView> tasks = new ArrayList<>();
    private Map<String, Object> variables;

    public UUID getWfExecId() {
        return wfExecId;
    }

    public void setWfExecId(UUID wfExecId) {
        this.wfExecId = wfExecId;
    }

    public String getProcessKey() {
        return processKey;
    }

    public void setProcessKey(String processKey) {
        this.processKey = processKey;
    }

    public String getWorkflowType() {
        return workflowType;
    }

    public void setWorkflowType(String workflowType) {
        this.workflowType = workflowType;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCurrentTaskId() {
        return currentTaskId;
    }

    public void setCurrentTaskId(String currentTaskId) {
        this.currentTaskId = currentTaskId;
    }

    public String getCurrentTaskName() {
        return currentTaskName;
    }

    public void setCurrentTaskName(String currentTaskName) {
        this.currentTaskName = currentTaskName;
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

    public String getBpmnResource() {
        return bpmnResource;
    }

    public void setBpmnResource(String bpmnResource) {
        this.bpmnResource = bpmnResource;
    }

    public List<BpmnTaskView> getTasks() {
        return tasks;
    }

    public void setTasks(List<BpmnTaskView> tasks) {
        this.tasks = tasks;
    }

    public Map<String, Object> getVariables() {
        return variables;
    }

    public void setVariables(Map<String, Object> variables) {
        this.variables = variables;
    }

    public static class BpmnTaskView {
        private String taskId;
        private String name;
        private String type;
        private String status;
        private Instant startedAt;
        private Instant completedAt;
        private String detail;

        public String getTaskId() {
            return taskId;
        }

        public void setTaskId(String taskId) {
            this.taskId = taskId;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
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

        public String getDetail() {
            return detail;
        }

        public void setDetail(String detail) {
            this.detail = detail;
        }
    }
}
