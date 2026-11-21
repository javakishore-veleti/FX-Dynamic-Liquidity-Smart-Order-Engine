package com.jk.apps.banking.fx.sor.qual.dto;

import java.math.BigDecimal;

public class EligibleLp {

    private String lpId;
    private int rank;
    private BigDecimal availableCreditUsd;
    private String reservationToken;

    public EligibleLp() {
    }

    public EligibleLp(String lpId, int rank, BigDecimal availableCreditUsd, String reservationToken) {
        this.lpId = lpId;
        this.rank = rank;
        this.availableCreditUsd = availableCreditUsd;
        this.reservationToken = reservationToken;
    }

    public String getLpId() {
        return lpId;
    }

    public void setLpId(String lpId) {
        this.lpId = lpId;
    }

    public int getRank() {
        return rank;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }

    public BigDecimal getAvailableCreditUsd() {
        return availableCreditUsd;
    }

    public void setAvailableCreditUsd(BigDecimal availableCreditUsd) {
        this.availableCreditUsd = availableCreditUsd;
    }

    public String getReservationToken() {
        return reservationToken;
    }

    public void setReservationToken(String reservationToken) {
        this.reservationToken = reservationToken;
    }
}
