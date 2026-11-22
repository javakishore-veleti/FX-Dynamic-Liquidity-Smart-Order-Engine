package com.jk.apps.banking.fx.sor.quote.dto;

import java.math.BigDecimal;

public class RegimeContextResponse {

    private String sessionRegime;
    private boolean macroActive;
    private BigDecimal lastLookPenaltyWeight;
    private BigDecimal sampleMarkupBps;

    public RegimeContextResponse() {
    }

    public RegimeContextResponse(
            String sessionRegime,
            boolean macroActive,
            BigDecimal lastLookPenaltyWeight,
            BigDecimal sampleMarkupBps) {
        this.sessionRegime = sessionRegime;
        this.macroActive = macroActive;
        this.lastLookPenaltyWeight = lastLookPenaltyWeight;
        this.sampleMarkupBps = sampleMarkupBps;
    }

    public String getSessionRegime() {
        return sessionRegime;
    }

    public void setSessionRegime(String sessionRegime) {
        this.sessionRegime = sessionRegime;
    }

    public boolean isMacroActive() {
        return macroActive;
    }

    public void setMacroActive(boolean macroActive) {
        this.macroActive = macroActive;
    }

    public BigDecimal getLastLookPenaltyWeight() {
        return lastLookPenaltyWeight;
    }

    public void setLastLookPenaltyWeight(BigDecimal lastLookPenaltyWeight) {
        this.lastLookPenaltyWeight = lastLookPenaltyWeight;
    }

    public BigDecimal getSampleMarkupBps() {
        return sampleMarkupBps;
    }

    public void setSampleMarkupBps(BigDecimal sampleMarkupBps) {
        this.sampleMarkupBps = sampleMarkupBps;
    }
}
