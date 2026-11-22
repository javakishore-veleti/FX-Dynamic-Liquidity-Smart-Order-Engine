package com.jk.apps.banking.fx.sor.quote.dto;

import java.math.BigDecimal;
import java.time.Instant;

public class SpreadScheduleDto {

    private String tierId;
    private String ccyPair;
    private String sessionRegime;
    private BigDecimal markupBps;
    private BigDecimal newsMultiplier;
    private Instant effectiveFrom;
    private String approvedBy;
    private String status;
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
