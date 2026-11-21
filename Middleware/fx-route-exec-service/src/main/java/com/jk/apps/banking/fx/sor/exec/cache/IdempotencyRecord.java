package com.jk.apps.banking.fx.sor.exec.cache;

import java.math.BigDecimal;
import java.util.UUID;

public class IdempotencyRecord {

    private String state;
    private UUID orderId;
    private BigDecimal lockedRate;

    public IdempotencyRecord() {
    }

    public IdempotencyRecord(String state, UUID orderId, BigDecimal lockedRate) {
        this.state = state;
        this.orderId = orderId;
        this.lockedRate = lockedRate;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public UUID getOrderId() {
        return orderId;
    }

    public void setOrderId(UUID orderId) {
        this.orderId = orderId;
    }

    public BigDecimal getLockedRate() {
        return lockedRate;
    }

    public void setLockedRate(BigDecimal lockedRate) {
        this.lockedRate = lockedRate;
    }
}
