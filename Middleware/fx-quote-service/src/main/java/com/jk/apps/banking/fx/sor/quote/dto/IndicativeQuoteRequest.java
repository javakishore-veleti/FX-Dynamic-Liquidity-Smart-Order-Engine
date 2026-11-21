package com.jk.apps.banking.fx.sor.quote.dto;

import java.math.BigDecimal;

public class IndicativeQuoteRequest {

    private String clientId;
    private String clientTier;
    private String ccyPair;
    private String side;
    private BigDecimal notional;
    private String tenor;

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getClientTier() {
        return clientTier;
    }

    public void setClientTier(String clientTier) {
        this.clientTier = clientTier;
    }

    public String getCcyPair() {
        return ccyPair;
    }

    public void setCcyPair(String ccyPair) {
        this.ccyPair = ccyPair;
    }

    public String getSide() {
        return side;
    }

    public void setSide(String side) {
        this.side = side;
    }

    public BigDecimal getNotional() {
        return notional;
    }

    public void setNotional(BigDecimal notional) {
        this.notional = notional;
    }

    public String getTenor() {
        return tenor;
    }

    public void setTenor(String tenor) {
        this.tenor = tenor;
    }
}
