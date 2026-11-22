package com.jk.apps.banking.fx.sor.quote.dto;

import java.math.BigDecimal;
import java.time.Instant;

public class EconomicCalendarEventDto {

    private String source;
    private BigDecimal severity;
    private Instant start;
    private Instant end;
    private String label;
    private boolean activeNow;

    public EconomicCalendarEventDto() {
    }

    public EconomicCalendarEventDto(
            String source,
            BigDecimal severity,
            Instant start,
            Instant end,
            String label,
            boolean activeNow) {
        this.source = source;
        this.severity = severity;
        this.start = start;
        this.end = end;
        this.label = label;
        this.activeNow = activeNow;
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

    public Instant getStart() {
        return start;
    }

    public void setStart(Instant start) {
        this.start = start;
    }

    public Instant getEnd() {
        return end;
    }

    public void setEnd(Instant end) {
        this.end = end;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public boolean isActiveNow() {
        return activeNow;
    }

    public void setActiveNow(boolean activeNow) {
        this.activeNow = activeNow;
    }
}
