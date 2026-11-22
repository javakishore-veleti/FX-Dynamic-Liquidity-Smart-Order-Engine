package com.jk.apps.banking.fx.sor.exec.dto;

public class LpRankingEntryDto {

    private String lpId;
    private int rank;
    private String ruleVersion;
    private boolean enabled;

    public LpRankingEntryDto() {
    }

    public LpRankingEntryDto(String lpId, int rank, String ruleVersion, boolean enabled) {
        this.lpId = lpId;
        this.rank = rank;
        this.ruleVersion = ruleVersion;
        this.enabled = enabled;
    }

    public String getLpId() {
        return lpId;
    }

    public void setLpId(String lpId) {
        this.lpId = lpId;
    }

    public int getRank() {
        return rank;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }

    public String getRuleVersion() {
        return ruleVersion;
    }

    public void setRuleVersion(String ruleVersion) {
        this.ruleVersion = ruleVersion;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
