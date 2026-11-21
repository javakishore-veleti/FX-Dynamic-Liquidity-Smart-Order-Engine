package com.jk.apps.banking.fx.sor.quote.entity;

import java.io.Serializable;
import java.util.Objects;

public class ClientTierSpreadScheduleId implements Serializable {

    private String tierId;
    private String ccyPair;
    private String sessionRegime;

    public ClientTierSpreadScheduleId() {
    }

    public ClientTierSpreadScheduleId(String tierId, String ccyPair, String sessionRegime) {
        this.tierId = tierId;
        this.ccyPair = ccyPair;
        this.sessionRegime = sessionRegime;
    }

    public String getTierId() {
        return tierId;
    }

    public void setTierId(String tierId) {
        this.tierId = tierId;
    }

    public String getCcyPair() {
        return ccyPair;
    }

    public void setCcyPair(String ccyPair) {
        this.ccyPair = ccyPair;
    }

    public String getSessionRegime() {
        return sessionRegime;
    }

    public void setSessionRegime(String sessionRegime) {
        this.sessionRegime = sessionRegime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ClientTierSpreadScheduleId that)) {
            return false;
        }
        return Objects.equals(tierId, that.tierId)
                && Objects.equals(ccyPair, that.ccyPair)
                && Objects.equals(sessionRegime, that.sessionRegime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tierId, ccyPair, sessionRegime);
    }
}
