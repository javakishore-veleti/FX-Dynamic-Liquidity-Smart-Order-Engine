package com.jk.apps.banking.fx.sor.agent.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ChaosSimExecuteRequest {

    private Integer numBatches = 10;
    private Integer batchSize = 1000;
    private List<String> datasets;
    private Integer targetRps = 50_000;
    private Integer durationSec = 60;
    private String ccyPair = "EURUSD";
    private boolean dryRun = true;
    private String approvalToken;

    public Integer getNumBatches() {
        return numBatches;
    }

    public void setNumBatches(Integer numBatches) {
        this.numBatches = numBatches;
    }

    public Integer getBatchSize() {
        return batchSize;
    }

    public void setBatchSize(Integer batchSize) {
        this.batchSize = batchSize;
    }

    public List<String> getDatasets() {
        return datasets;
    }

    public void setDatasets(List<String> datasets) {
        this.datasets = datasets;
    }

    public Integer getTargetRps() {
        return targetRps;
    }

    public void setTargetRps(Integer targetRps) {
        this.targetRps = targetRps;
    }

    public Integer getDurationSec() {
        return durationSec;
    }

    public void setDurationSec(Integer durationSec) {
        this.durationSec = durationSec;
    }

    public String getCcyPair() {
        return ccyPair;
    }

    public void setCcyPair(String ccyPair) {
        this.ccyPair = ccyPair;
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
