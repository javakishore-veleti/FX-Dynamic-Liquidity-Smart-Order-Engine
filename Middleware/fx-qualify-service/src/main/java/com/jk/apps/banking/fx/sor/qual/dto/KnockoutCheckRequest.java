package com.jk.apps.banking.fx.sor.qual.dto;

import java.math.BigDecimal;

public class KnockoutCheckRequest {

    private String ccyPair;
    private String tenor;
    private BigDecimal notionalUsd;

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

    public BigDecimal getNotionalUsd() {
        return notionalUsd;
    }

    public void setNotionalUsd(BigDecimal notionalUsd) {
        this.notionalUsd = notionalUsd;
    }
}
