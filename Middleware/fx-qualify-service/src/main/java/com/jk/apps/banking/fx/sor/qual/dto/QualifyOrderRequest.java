package com.jk.apps.banking.fx.sor.qual.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public class QualifyOrderRequest {

    private String clientOrderId;
    private String clientId;
    private String ccyPair;
    private BigDecimal notionalUsd;
    private String tenor;
    private LocalDate valueDate;

    public String getClientOrderId() {
        return clientOrderId;
    }

    public void setClientOrderId(String clientOrderId) {
        this.clientOrderId = clientOrderId;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getCcyPair() {
        return ccyPair;
    }

    public void setCcyPair(String ccyPair) {
        this.ccyPair = ccyPair;
    }

    public BigDecimal getNotionalUsd() {
        return notionalUsd;
    }

    public void setNotionalUsd(BigDecimal notionalUsd) {
        this.notionalUsd = notionalUsd;
    }

    public String getTenor() {
        return tenor;
    }

    public void setTenor(String tenor) {
        this.tenor = tenor;
    }

    public LocalDate getValueDate() {
        return valueDate;
    }

    public void setValueDate(LocalDate valueDate) {
        this.valueDate = valueDate;
    }
}
