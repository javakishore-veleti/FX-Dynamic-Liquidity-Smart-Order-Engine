package com.jk.apps.banking.fx.sor.quote.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;

/** One economic calendar window. event_id = source|start, the system-unique key. */
@Entity
@Table(name = "economic_calendar_events")
public class CalendarEvent {

    @Id
    @Column(name = "event_id", length = 120, nullable = false)
    private String eventId;

    @Column(name = "source", length = 20, nullable = false)
    private String source;

    @Column(name = "severity", precision = 6, scale = 2, nullable = false)
    private BigDecimal severity;

    @Column(name = "start_at", nullable = false)
    private Instant startAt;

    @Column(name = "end_at", nullable = false)
    private Instant endAt;

    @Column(name = "label", length = 200)
    private String label;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "created_by", length = 100, nullable = false, updatable = false)
    private String createdBy;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "updated_by", length = 100, nullable = false)
    private String updatedBy;

    protected CalendarEvent() {
    }

    public CalendarEvent(String source, BigDecimal severity, Instant start, Instant end, String label,
                         String actor, Instant now) {
        this.eventId = source + "|" + start;
        this.source = source;
        this.severity = severity;
        this.startAt = start;
        this.endAt = end;
        this.label = label;
        this.createdAt = now;
        this.createdBy = actor;
        this.updatedAt = now;
        this.updatedBy = actor;
    }

    public static String idOf(String source, Instant start) {
        return source + "|" + start;
    }

    public String getEventId() { return eventId; }
    public String getSource() { return source; }
    public BigDecimal getSeverity() { return severity; }
    public Instant getStartAt() { return startAt; }
    public Instant getEndAt() { return endAt; }
    public String getLabel() { return label; }
}
