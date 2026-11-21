package com.jk.apps.banking.fx.sor.agent.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TreasuryHedgeCopilotRequest {

    private String clientId;
    private String ccyPair;
    private BigDecimal notionalUsd;
    private String prompt;
    private boolean dryRun = true;
    private String approvalToken;

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getCcyPair() {
        return ccyPair;
    }

    public void setCcyPair(String ccyPair) {
        this.ccyPair = ccyPair;
    }

    public BigDecimal getNotionalUsd() {
        return notionalUsd;
    }

    public void setNotionalUsd(BigDecimal notionalUsd) {
        this.notionalUsd = notionalUsd;
    }

    public String getPrompt() {
        return prompt;
    }

    public void setPrompt(String prompt) {
        this.prompt = prompt;
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
