package com.jk.apps.banking.fx.sor.qual.cache;

import java.math.BigDecimal;

public class CreditLine {

    private String clientId;
    private String lpId;
    private BigDecimal maxCreditUsd;
    private BigDecimal utilizedUsd;
    private BigDecimal reservedUsd;
    private long resetEpoch;

    public CreditLine() {
    }

    public CreditLine(
            String clientId,
            String lpId,
            BigDecimal maxCreditUsd,
            BigDecimal utilizedUsd,
            BigDecimal reservedUsd,
            long resetEpoch) {
        this.clientId = clientId;
        this.lpId = lpId;
        this.maxCreditUsd = maxCreditUsd;
        this.utilizedUsd = utilizedUsd;
        this.reservedUsd = reservedUsd;
        this.resetEpoch = resetEpoch;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getLpId() {
        return lpId;
    }

    public void setLpId(String lpId) {
        this.lpId = lpId;
    }

    public BigDecimal getMaxCreditUsd() {
        return maxCreditUsd;
    }

    public void setMaxCreditUsd(BigDecimal maxCreditUsd) {
        this.maxCreditUsd = maxCreditUsd;
    }

    public BigDecimal getUtilizedUsd() {
        return utilizedUsd;
    }

    public void setUtilizedUsd(BigDecimal utilizedUsd) {
        this.utilizedUsd = utilizedUsd;
    }

    public BigDecimal getReservedUsd() {
        return reservedUsd;
    }

    public void setReservedUsd(BigDecimal reservedUsd) {
        this.reservedUsd = reservedUsd;
    }

    public long getResetEpoch() {
        return resetEpoch;
    }

    public void setResetEpoch(long resetEpoch) {
        this.resetEpoch = resetEpoch;
    }

    public BigDecimal headroom() {
        return maxCreditUsd.subtract(utilizedUsd).subtract(reservedUsd);
    }
}
