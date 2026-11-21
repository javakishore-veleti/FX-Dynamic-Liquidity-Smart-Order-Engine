package com.jk.apps.banking.fx.sor.agent.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PolicyAuthoringRequest {

    private String intentText;
    private String existingDmnRef;
    private boolean dryRun = true;
    private String approvalToken;

    public String getIntentText() {
        return intentText;
    }

    public void setIntentText(String intentText) {
        this.intentText = intentText;
    }

    public String getExistingDmnRef() {
        return existingDmnRef;
    }

    public void setExistingDmnRef(String existingDmnRef) {
        this.existingDmnRef = existingDmnRef;
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
