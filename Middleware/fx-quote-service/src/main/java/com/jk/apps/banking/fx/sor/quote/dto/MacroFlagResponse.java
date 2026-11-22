package com.jk.apps.banking.fx.sor.quote.dto;

import java.math.BigDecimal;
import java.time.Instant;

public class MacroFlagResponse {

    private boolean active;
    private String regime;
    private String source;
    private BigDecimal severity;
    private Instant expiresAt;
    private BigDecimal lastLookPenaltyWeight;

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getRegime() {
        return regime;
    }

    public void setRegime(String regime) {
        this.regime = regime;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public BigDecimal getSeverity() {
        return severity;
    }

    public void setSeverity(BigDecimal severity) {
        this.severity = severity;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(Instant expiresAt) {
        this.expiresAt = expiresAt;
    }

    public BigDecimal getLastLookPenaltyWeight() {
        return lastLookPenaltyWeight;
    }

    public void setLastLookPenaltyWeight(BigDecimal lastLookPenaltyWeight) {
        this.lastLookPenaltyWeight = lastLookPenaltyWeight;
    }
}
