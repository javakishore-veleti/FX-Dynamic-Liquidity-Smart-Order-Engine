package com.jk.apps.banking.fx.sor.qual.dto;

import java.util.ArrayList;
import java.util.List;

public class QualifyOrderResponse {

    private String clientOrderId;
    private boolean qualified;
    private String creditReservationToken;
    private List<EligibleLp> eligibleLps = new ArrayList<>();
    private List<KnockoutReason> knockouts = new ArrayList<>();

    public String getClientOrderId() {
        return clientOrderId;
    }

    public void setClientOrderId(String clientOrderId) {
        this.clientOrderId = clientOrderId;
    }

    public boolean isQualified() {
        return qualified;
    }

    public void setQualified(boolean qualified) {
        this.qualified = qualified;
    }

    public String getCreditReservationToken() {
        return creditReservationToken;
    }

    public void setCreditReservationToken(String creditReservationToken) {
        this.creditReservationToken = creditReservationToken;
    }

    public List<EligibleLp> getEligibleLps() {
        return eligibleLps;
    }

    public void setEligibleLps(List<EligibleLp> eligibleLps) {
        this.eligibleLps = eligibleLps;
    }

    public List<KnockoutReason> getKnockouts() {
        return knockouts;
    }

    public void setKnockouts(List<KnockoutReason> knockouts) {
        this.knockouts = knockouts;
    }
}
