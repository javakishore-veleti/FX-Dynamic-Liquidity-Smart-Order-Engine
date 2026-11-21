package com.jk.apps.banking.fx.sor.quote.dto;

import java.math.BigDecimal;

public class IndicativeQuoteResponse {

    private String quoteId;
    private String ccyPair;
    private String side;
    private BigDecimal executableRate;
    private BigDecimal allInSpreadBps;
    private long quoteTtlMs;
    private String sessionRegime;
    private String clientTier;

    public IndicativeQuoteResponse() {
    }

    public IndicativeQuoteResponse(
            String quoteId,
            String ccyPair,
            String side,
            BigDecimal executableRate,
            BigDecimal allInSpreadBps,
            long quoteTtlMs,
            String sessionRegime,
            String clientTier) {
        this.quoteId = quoteId;
        this.ccyPair = ccyPair;
        this.side = side;
        this.executableRate = executableRate;
        this.allInSpreadBps = allInSpreadBps;
        this.quoteTtlMs = quoteTtlMs;
        this.sessionRegime = sessionRegime;
        this.clientTier = clientTier;
    }

    public String getQuoteId() {
        return quoteId;
    }

    public void setQuoteId(String quoteId) {
        this.quoteId = quoteId;
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

    public BigDecimal getExecutableRate() {
        return executableRate;
    }

    public void setExecutableRate(BigDecimal executableRate) {
        this.executableRate = executableRate;
    }

    public BigDecimal getAllInSpreadBps() {
        return allInSpreadBps;
    }

    public void setAllInSpreadBps(BigDecimal allInSpreadBps) {
        this.allInSpreadBps = allInSpreadBps;
    }

    public long getQuoteTtlMs() {
        return quoteTtlMs;
    }

    public void setQuoteTtlMs(long quoteTtlMs) {
        this.quoteTtlMs = quoteTtlMs;
    }

    public String getSessionRegime() {
        return sessionRegime;
    }

    public void setSessionRegime(String sessionRegime) {
        this.sessionRegime = sessionRegime;
    }

    public String getClientTier() {
        return clientTier;
    }

    public void setClientTier(String clientTier) {
        this.clientTier = clientTier;
    }
}
