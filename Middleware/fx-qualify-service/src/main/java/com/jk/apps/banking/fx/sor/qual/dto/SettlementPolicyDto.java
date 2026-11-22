package com.jk.apps.banking.fx.sor.qual.dto;

import java.math.BigDecimal;
import java.time.LocalTime;

public class SettlementPolicyDto {

    private String ccyPair;
    private String tenor;
    private boolean clsEligible;
    private LocalTime cutoffTimeUtc;
    private BigDecimal minClipUsd;
    private BigDecimal maxClipUsd;
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
