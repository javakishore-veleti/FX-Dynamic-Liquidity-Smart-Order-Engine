package com.jk.apps.banking.fx.sor.admin.dto;

import java.math.BigDecimal;

public class CreditOverrideRequest {

    private String clientId;
    private String counterpartyLpId;
    private BigDecimal maxCreditUsd;
    private String reason;

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

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
