package com.jk.apps.banking.fx.sor.exec.dto;

import java.math.BigDecimal;

public class RouteExecuteRequest {

    private String clientOrderId;
    private String quoteId;
    private String clientId;
    private String ccyPair;
    private String side;
    private BigDecimal notional;
    private BigDecimal maxSlippageBps;
    /** FR-01 enrichment — from quote regime context */
    private String sessionRegime;
    private BigDecimal lastLookPenaltyWeight;

    public String getClientOrderId() {
        return clientOrderId;
    }

    public void setClientOrderId(String clientOrderId) {
        this.clientOrderId = clientOrderId;
    }

    public String getQuoteId() {
        return quoteId;
    }

    public void setQuoteId(String quoteId) {
        this.quoteId = quoteId;
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

    public BigDecimal getMaxSlippageBps() {
        return maxSlippageBps;
    }

    public void setMaxSlippageBps(BigDecimal maxSlippageBps) {
        this.maxSlippageBps = maxSlippageBps;
    }

    public String getSessionRegime() {
        return sessionRegime;
    }

    public void setSessionRegime(String sessionRegime) {
        this.sessionRegime = sessionRegime;
    }

    public BigDecimal getLastLookPenaltyWeight() {
        return lastLookPenaltyWeight;
    }

    public void setLastLookPenaltyWeight(BigDecimal lastLookPenaltyWeight) {
        this.lastLookPenaltyWeight = lastLookPenaltyWeight;
    }
}
