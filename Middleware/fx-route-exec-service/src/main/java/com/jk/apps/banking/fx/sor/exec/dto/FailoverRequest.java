package com.jk.apps.banking.fx.sor.exec.dto;

import java.math.BigDecimal;

public class FailoverRequest {

    private String rejectedLpId;
    private BigDecimal unfilledNotional;
    private String reasonCode;

    public String getRejectedLpId() {
        return rejectedLpId;
    }

    public void setRejectedLpId(String rejectedLpId) {
        this.rejectedLpId = rejectedLpId;
    }

    public BigDecimal getUnfilledNotional() {
        return unfilledNotional;
    }

    public void setUnfilledNotional(BigDecimal unfilledNotional) {
        this.unfilledNotional = unfilledNotional;
    }

    public String getReasonCode() {
        return reasonCode;
    }

    public void setReasonCode(String reasonCode) {
        this.reasonCode = reasonCode;
    }
}
