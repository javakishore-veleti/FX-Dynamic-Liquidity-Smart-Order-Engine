package com.jk.apps.banking.fx.sor.qual.dto;

import java.math.BigDecimal;

public class CreditUtilizationResponse {

    private String clientId;
    private String counterpartyLpId;
    private BigDecimal maxCreditUsd;
    private BigDecimal utilizedUsd;
    private BigDecimal reservedUsd;
    private BigDecimal headroomUsd;
    private long resetEpoch;

    public CreditUtilizationResponse() {
    }

    public CreditUtilizationResponse(
            String clientId,
            String counterpartyLpId,
            BigDecimal maxCreditUsd,
            BigDecimal utilizedUsd,
            BigDecimal reservedUsd,
            BigDecimal headroomUsd,
            long resetEpoch) {
        this.clientId = clientId;
        this.counterpartyLpId = counterpartyLpId;
        this.maxCreditUsd = maxCreditUsd;
        this.utilizedUsd = utilizedUsd;
        this.reservedUsd = reservedUsd;
        this.headroomUsd = headroomUsd;
        this.resetEpoch = resetEpoch;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getCounterpartyLpId() {
        return counterpartyLpId;
    }

    public void setCounterpartyLpId(String counterpartyLpId) {
        this.counterpartyLpId = counterpartyLpId;
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

    public BigDecimal getHeadroomUsd() {
        return headroomUsd;
    }

    public void setHeadroomUsd(BigDecimal headroomUsd) {
        this.headroomUsd = headroomUsd;
    }

    public long getResetEpoch() {
        return resetEpoch;
    }

    public void setResetEpoch(long resetEpoch) {
        this.resetEpoch = resetEpoch;
    }
}
