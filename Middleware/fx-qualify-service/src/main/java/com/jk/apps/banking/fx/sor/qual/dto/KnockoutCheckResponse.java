package com.jk.apps.banking.fx.sor.qual.dto;

import java.util.ArrayList;
import java.util.List;

public class KnockoutCheckResponse {

    private String ccyPair;
    private String tenor;
    private List<VenueEligibility> venues = new ArrayList<>();

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

    public List<VenueEligibility> getVenues() {
        return venues;
    }

    public void setVenues(List<VenueEligibility> venues) {
        this.venues = venues;
    }
}
