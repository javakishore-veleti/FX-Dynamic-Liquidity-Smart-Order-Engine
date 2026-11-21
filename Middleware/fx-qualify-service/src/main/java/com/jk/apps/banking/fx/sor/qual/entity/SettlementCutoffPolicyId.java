package com.jk.apps.banking.fx.sor.qual.entity;

import java.io.Serializable;
import java.util.Objects;

public class SettlementCutoffPolicyId implements Serializable {

    private String ccyPair;
    private String tenor;

    public SettlementCutoffPolicyId() {
    }

    public SettlementCutoffPolicyId(String ccyPair, String tenor) {
        this.ccyPair = ccyPair;
        this.tenor = tenor;
    }

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

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof SettlementCutoffPolicyId that)) {
            return false;
        }
        return Objects.equals(ccyPair, that.ccyPair) && Objects.equals(tenor, that.tenor);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ccyPair, tenor);
    }
}
