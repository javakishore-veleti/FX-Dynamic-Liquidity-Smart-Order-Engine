package com.jk.apps.banking.fx.sor.qual.dto;

public class VenueEligibility {

    private String lpId;
    private boolean pass;
    private String ruleCode;

    public VenueEligibility() {
    }

    public VenueEligibility(String lpId, boolean pass, String ruleCode) {
        this.lpId = lpId;
        this.pass = pass;
        this.ruleCode = ruleCode;
    }

    public String getLpId() {
        return lpId;
    }

    public void setLpId(String lpId) {
        this.lpId = lpId;
    }

    public boolean isPass() {
        return pass;
    }

    public void setPass(boolean pass) {
        this.pass = pass;
    }

    public String getRuleCode() {
        return ruleCode;
    }

    public void setRuleCode(String ruleCode) {
        this.ruleCode = ruleCode;
    }
}
