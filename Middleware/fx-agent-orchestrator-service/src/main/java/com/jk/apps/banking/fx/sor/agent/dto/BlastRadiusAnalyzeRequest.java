package com.jk.apps.banking.fx.sor.agent.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class BlastRadiusAnalyzeRequest {

    private String serviceName;
    private String ccyPair;
    private String fromTs;
    private String toTs;
    private Integer depth = 3;
    private boolean dryRun = true;
    private String approvalToken;

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getCcyPair() {
        return ccyPair;
    }

    public void setCcyPair(String ccyPair) {
        this.ccyPair = ccyPair;
    }

    public String getFromTs() {
        return fromTs;
    }

    public void setFromTs(String fromTs) {
        this.fromTs = fromTs;
    }

    public String getToTs() {
        return toTs;
    }

    public void setToTs(String toTs) {
        this.toTs = toTs;
    }

    public Integer getDepth() {
        return depth;
    }

    public void setDepth(Integer depth) {
        this.depth = depth;
    }

    public boolean isDryRun() {
        return dryRun;
    }

    public void setDryRun(boolean dryRun) {
        this.dryRun = dryRun;
    }

    public String getApprovalToken() {
        return approvalToken;
    }

    public void setApprovalToken(String approvalToken) {
        this.approvalToken = approvalToken;
    }
}
