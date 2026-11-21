package com.jk.apps.banking.fx.sor.quote.dto;

import java.math.BigDecimal;

public class RateSnapshotResponse {

    private String ccyPair;
    private BigDecimal bid;
    private BigDecimal ask;
    private BigDecimal mid;
    private int activeLpCount;
    private String sessionRegime;

    public RateSnapshotResponse() {
    }

    public RateSnapshotResponse(
            String ccyPair,
            BigDecimal bid,
            BigDecimal ask,
            BigDecimal mid,
            int activeLpCount,
            String sessionRegime) {
        this.ccyPair = ccyPair;
        this.bid = bid;
        this.ask = ask;
        this.mid = mid;
        this.activeLpCount = activeLpCount;
        this.sessionRegime = sessionRegime;
    }

    public String getCcyPair() {
        return ccyPair;
    }

    public void setCcyPair(String ccyPair) {
        this.ccyPair = ccyPair;
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

    public BigDecimal getMid() {
        return mid;
    }

    public void setMid(BigDecimal mid) {
        this.mid = mid;
    }

    public int getActiveLpCount() {
        return activeLpCount;
    }

    public void setActiveLpCount(int activeLpCount) {
        this.activeLpCount = activeLpCount;
    }

    public String getSessionRegime() {
        return sessionRegime;
    }

    public void setSessionRegime(String sessionRegime) {
        this.sessionRegime = sessionRegime;
    }
}
