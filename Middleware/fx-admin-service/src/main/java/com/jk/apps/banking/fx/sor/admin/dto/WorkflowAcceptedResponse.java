package com.jk.apps.banking.fx.sor.admin.dto;

import java.util.UUID;

public class WorkflowAcceptedResponse {

    private UUID wfExecId;
    private String status;
    private String workflowType;

    public WorkflowAcceptedResponse() {
    }

    public WorkflowAcceptedResponse(UUID wfExecId, String status, String workflowType) {
        this.wfExecId = wfExecId;
        this.status = status;
        this.workflowType = workflowType;
    }

    public UUID getWfExecId() {
        return wfExecId;
    }

    public void setWfExecId(UUID wfExecId) {
        this.wfExecId = wfExecId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getWorkflowType() {
        return workflowType;
    }

    public void setWorkflowType(String workflowType) {
        this.workflowType = workflowType;
    }
}
