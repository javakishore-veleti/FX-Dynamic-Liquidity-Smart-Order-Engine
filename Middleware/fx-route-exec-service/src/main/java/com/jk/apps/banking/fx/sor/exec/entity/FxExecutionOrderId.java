package com.jk.apps.banking.fx.sor.exec.entity;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

public class FxExecutionOrderId implements Serializable {

    private LocalDate tradeDate;
    private UUID orderId;

    public FxExecutionOrderId() {
    }

    public FxExecutionOrderId(LocalDate tradeDate, UUID orderId) {
        this.tradeDate = tradeDate;
        this.orderId = orderId;
    }

    public LocalDate getTradeDate() {
        return tradeDate;
    }

    public void setTradeDate(LocalDate tradeDate) {
        this.tradeDate = tradeDate;
    }

    public UUID getOrderId() {
        return orderId;
    }

    public void setOrderId(UUID orderId) {
        this.orderId = orderId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof FxExecutionOrderId that)) {
            return false;
        }
        return Objects.equals(tradeDate, that.tradeDate) && Objects.equals(orderId, that.orderId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tradeDate, orderId);
    }
}
