package com.jk.apps.banking.fx.sor.admin.dto;

public class VenueHaltRequest {

    private String reason;
    private boolean halt = true;

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public boolean isHalt() {
        return halt;
    }

    public void setHalt(boolean halt) {
        this.halt = halt;
    }
}
