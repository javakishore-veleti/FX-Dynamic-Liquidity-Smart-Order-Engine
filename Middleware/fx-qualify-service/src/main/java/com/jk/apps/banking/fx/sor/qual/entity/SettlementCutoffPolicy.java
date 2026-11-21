package com.jk.apps.banking.fx.sor.qual.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalTime;

@Entity
@Table(name = "settlement_cutoffs_and_policies")
@IdClass(SettlementCutoffPolicyId.class)
public class SettlementCutoffPolicy {

    @Id
    @Column(name = "ccy_pair", length = 10, nullable = false)
    private String ccyPair;

    @Id
    @Column(name = "tenor", length = 10, nullable = false)
    private String tenor;

    @Column(name = "cls_eligible", nullable = false)
    private boolean clsEligible;

    @Column(name = "cutoff_time_utc", nullable = false)
    private LocalTime cutoffTimeUtc;

    @Column(name = "min_clip_usd", nullable = false, precision = 18, scale = 2)
    private BigDecimal minClipUsd;

    @Column(name = "max_clip_usd", nullable = false, precision = 18, scale = 2)
    private BigDecimal maxClipUsd;

    @Column(name = "restricted_flag", nullable = false)
    private boolean restrictedFlag;

    public String getCcyPair() {
        return ccyPair;
    }

    public void setCcyPair(String ccyPair) {
        this.ccyPair = ccyPair;
    }

    public String getTenor() {
        return tenor;
    }

    public void setTenor(String tenor) {
        this.tenor = tenor;
    }

    public boolean isClsEligible() {
        return clsEligible;
    }

    public void setClsEligible(boolean clsEligible) {
        this.clsEligible = clsEligible;
    }

    public LocalTime getCutoffTimeUtc() {
        return cutoffTimeUtc;
    }

    public void setCutoffTimeUtc(LocalTime cutoffTimeUtc) {
        this.cutoffTimeUtc = cutoffTimeUtc;
    }

    public BigDecimal getMinClipUsd() {
        return minClipUsd;
    }

    public void setMinClipUsd(BigDecimal minClipUsd) {
        this.minClipUsd = minClipUsd;
    }

    public BigDecimal getMaxClipUsd() {
        return maxClipUsd;
    }

    public void setMaxClipUsd(BigDecimal maxClipUsd) {
        this.maxClipUsd = maxClipUsd;
    }

    public boolean isRestrictedFlag() {
        return restrictedFlag;
    }

    public void setRestrictedFlag(boolean restrictedFlag) {
        this.restrictedFlag = restrictedFlag;
    }
}
