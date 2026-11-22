package com.jk.apps.banking.fx.sor.quote.dto;

import java.math.BigDecimal;

public class MacroFlagRequest {

    private String source;
    private BigDecimal severity;
    private Long ttlSeconds;

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

    public Long getTtlSeconds() {
        return ttlSeconds;
    }

    public void setTtlSeconds(Long ttlSeconds) {
        this.ttlSeconds = ttlSeconds;
    }
}
