package com.jk.apps.banking.fx.sor.quote.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "client_tier_spread_schedules")
@IdClass(ClientTierSpreadScheduleId.class)
public class ClientTierSpreadSchedule {

    @Id
    @Column(name = "tier_id", length = 30, nullable = false)
    private String tierId;

    @Id
    @Column(name = "ccy_pair", length = 10, nullable = false)
    private String ccyPair;

    @Id
    @Column(name = "session_regime", length = 30, nullable = false)
    private String sessionRegime;

    @Column(name = "markup_bps", nullable = false, precision = 8, scale = 4)
    private BigDecimal markupBps;

    @Column(name = "news_multiplier", nullable = false, precision = 6, scale = 2)
    private BigDecimal newsMultiplier;

    @Column(name = "effective_from", nullable = false)
    private Instant effectiveFrom;

    @Column(name = "approved_by", length = 100, nullable = false)
    private String approvedBy;

    /** DRAFT | PENDING_APPROVAL | ACTIVE | REJECTED */
    @Column(name = "status", length = 20, nullable = false)
    private String status = "ACTIVE";

    /** Desk-facing purpose — why this tier×pair×session markup exists. */
    @Column(name = "purpose_note", length = 500)
    private String purposeNote;

    public String getTierId() {
        return tierId;
    }

    public void setTierId(String tierId) {
        this.tierId = tierId;
    }

    public String getCcyPair() {
        return ccyPair;
    }

    public void setCcyPair(String ccyPair) {
        this.ccyPair = ccyPair;
    }

    public String getSessionRegime() {
        return sessionRegime;
    }

    public void setSessionRegime(String sessionRegime) {
        this.sessionRegime = sessionRegime;
    }

    public BigDecimal getMarkupBps() {
        return markupBps;
    }

    public void setMarkupBps(BigDecimal markupBps) {
        this.markupBps = markupBps;
    }

    public BigDecimal getNewsMultiplier() {
        return newsMultiplier;
    }

    public void setNewsMultiplier(BigDecimal newsMultiplier) {
        this.newsMultiplier = newsMultiplier;
    }

    public Instant getEffectiveFrom() {
        return effectiveFrom;
    }

    public void setEffectiveFrom(Instant effectiveFrom) {
        this.effectiveFrom = effectiveFrom;
    }

    public String getApprovedBy() {
        return approvedBy;
    }

    public void setApprovedBy(String approvedBy) {
        this.approvedBy = approvedBy;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getPurposeNote() {
        return purposeNote;
    }

    public void setPurposeNote(String purposeNote) {
        this.purposeNote = purposeNote;
    }
}
