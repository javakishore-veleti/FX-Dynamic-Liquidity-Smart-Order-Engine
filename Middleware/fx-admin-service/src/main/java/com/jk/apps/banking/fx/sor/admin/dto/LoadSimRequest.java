package com.jk.apps.banking.fx.sor.admin.dto;

public class LoadSimRequest {

    private Integer targetRps;
    private Integer durationSeconds;
    private String regime;
    private String triggeredBy;

    public Integer getTargetRps() {
        return targetRps;
    }

    public void setTargetRps(Integer targetRps) {
        this.targetRps = targetRps;
    }

    public Integer getDurationSeconds() {
        return durationSeconds;
    }

    public void setDurationSeconds(Integer durationSeconds) {
        this.durationSeconds = durationSeconds;
    }

    public String getRegime() {
        return regime;
    }

    public void setRegime(String regime) {
        this.regime = regime;
    }

    public String getTriggeredBy() {
        return triggeredBy;
    }

    public void setTriggeredBy(String triggeredBy) {
        this.triggeredBy = triggeredBy;
    }
}
