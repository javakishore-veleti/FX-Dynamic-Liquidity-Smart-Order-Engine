package com.jk.apps.banking.fx.sor.quote.cache;

import java.math.BigDecimal;

public class LpQuote {

    private String ccyPair;
    private String lpId;
    private BigDecimal bid;
    private BigDecimal ask;
    private BigDecimal availableSize;
    private long quoteTs;
    private long ttlMs;

    public LpQuote() {
    }

    public LpQuote(
            String ccyPair,
            String lpId,
            BigDecimal bid,
            BigDecimal ask,
            BigDecimal availableSize,
            long quoteTs,
            long ttlMs) {
        this.ccyPair = ccyPair;
        this.lpId = lpId;
        this.bid = bid;
        this.ask = ask;
        this.availableSize = availableSize;
        this.quoteTs = quoteTs;
        this.ttlMs = ttlMs;
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

    public BigDecimal getAvailableSize() {
        return availableSize;
    }

    public void setAvailableSize(BigDecimal availableSize) {
        this.availableSize = availableSize;
    }

    public long getQuoteTs() {
        return quoteTs;
    }

    public void setQuoteTs(long quoteTs) {
        this.quoteTs = quoteTs;
    }

    public long getTtlMs() {
        return ttlMs;
    }

    public void setTtlMs(long ttlMs) {
        this.ttlMs = ttlMs;
    }

    public boolean isStale(long nowMs) {
        return nowMs - quoteTs > ttlMs;
    }
}
