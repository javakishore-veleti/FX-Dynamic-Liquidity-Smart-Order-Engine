package com.jk.apps.banking.fx.sor.qual.dto;

public class KnockoutReason {

    private String lpId;
    private String ruleCode;
    private String message;

    public KnockoutReason() {
    }

    public KnockoutReason(String lpId, String ruleCode, String message) {
        this.lpId = lpId;
        this.ruleCode = ruleCode;
        this.message = message;
    }

    public String getLpId() {
        return lpId;
    }

    public void setLpId(String lpId) {
        this.lpId = lpId;
    }

    public String getRuleCode() {
        return ruleCode;
    }

    public void setRuleCode(String ruleCode) {
        this.ruleCode = ruleCode;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
