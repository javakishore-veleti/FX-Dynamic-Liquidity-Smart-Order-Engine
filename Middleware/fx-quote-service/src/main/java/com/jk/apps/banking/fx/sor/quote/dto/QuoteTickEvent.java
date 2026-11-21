package com.jk.apps.banking.fx.sor.quote.dto;

import java.math.BigDecimal;

public class QuoteTickEvent {

    private String ccyPair;
    private String lpId;
    private BigDecimal bid;
    private BigDecimal ask;
    private BigDecimal allInSpreadBps;
    private String clientTier;
    private String sessionRegime;
    private long quoteTs;

    public QuoteTickEvent() {
    }

    public QuoteTickEvent(
            String ccyPair,
            String lpId,
            BigDecimal bid,
            BigDecimal ask,
            BigDecimal allInSpreadBps,
            String clientTier,
            String sessionRegime,
            long quoteTs) {
        this.ccyPair = ccyPair;
        this.lpId = lpId;
        this.bid = bid;
        this.ask = ask;
        this.allInSpreadBps = allInSpreadBps;
        this.clientTier = clientTier;
        this.sessionRegime = sessionRegime;
        this.quoteTs = quoteTs;
    }

    public String getCcyPair() {
        return ccyPair;
    }

    public void setCcyPair(String ccyPair) {
        this.ccyPair = ccyPair;
    }

    public String getLpId() {
        return lpId;
    }

    public void setLpId(String lpId) {
        this.lpId = lpId;
    }

    public BigDecimal getBid() {
        return bid;
    }

    public void setBid(BigDecimal bid) {
        this.bid = bid;
    }

    public BigDecimal getAsk() {
        return ask;
    }

    public void setAsk(BigDecimal ask) {
        this.ask = ask;
    }

    public BigDecimal getAllInSpreadBps() {
        return allInSpreadBps;
    }

    public void setAllInSpreadBps(BigDecimal allInSpreadBps) {
        this.allInSpreadBps = allInSpreadBps;
    }

    public String getClientTier() {
        return clientTier;
    }

    public void setClientTier(String clientTier) {
        this.clientTier = clientTier;
    }

    public String getSessionRegime() {
        return sessionRegime;
    }

    public void setSessionRegime(String sessionRegime) {
        this.sessionRegime = sessionRegime;
    }

    public long getQuoteTs() {
        return quoteTs;
    }

    public void setQuoteTs(long quoteTs) {
        this.quoteTs = quoteTs;
    }
}
