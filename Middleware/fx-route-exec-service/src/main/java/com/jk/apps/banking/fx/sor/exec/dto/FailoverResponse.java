package com.jk.apps.banking.fx.sor.exec.dto;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class FailoverResponse {

    private UUID orderId;
    private String status;
    private List<ChildSplitDto> reroutedSplits = new ArrayList<>();

    public UUID getOrderId() {
        return orderId;
    }

    public void setOrderId(UUID orderId) {
        this.orderId = orderId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<ChildSplitDto> getReroutedSplits() {
        return reroutedSplits;
    }

    public void setReroutedSplits(List<ChildSplitDto> reroutedSplits) {
        this.reroutedSplits = reroutedSplits;
    }
}
