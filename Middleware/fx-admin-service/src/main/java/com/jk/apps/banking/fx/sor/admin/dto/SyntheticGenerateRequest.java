package com.jk.apps.banking.fx.sor.admin.dto;

import java.util.List;

public class SyntheticGenerateRequest {

    private Integer numBatches;
    private Integer batchSize;
    private List<String> datasetTypes;
    private String targetEnv;
    private String triggeredBy;

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

    public List<String> getDatasetTypes() {
        return datasetTypes;
    }

    public void setDatasetTypes(List<String> datasetTypes) {
        this.datasetTypes = datasetTypes;
    }

    public String getTargetEnv() {
        return targetEnv;
    }

    public void setTargetEnv(String targetEnv) {
        this.targetEnv = targetEnv;
    }

    public String getTriggeredBy() {
        return triggeredBy;
    }

    public void setTriggeredBy(String triggeredBy) {
        this.triggeredBy = triggeredBy;
    }
}
