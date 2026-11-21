package com.jk.apps.banking.fx.sor.exec.entity;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

public class ChildVenueSplitId implements Serializable {

    private LocalDate tradeDate;
    private UUID splitId;

    public ChildVenueSplitId() {
    }

    public ChildVenueSplitId(LocalDate tradeDate, UUID splitId) {
        this.tradeDate = tradeDate;
        this.splitId = splitId;
    }

    public LocalDate getTradeDate() {
        return tradeDate;
    }

    public void setTradeDate(LocalDate tradeDate) {
        this.tradeDate = tradeDate;
    }

    public UUID getSplitId() {
        return splitId;
    }

    public void setSplitId(UUID splitId) {
        this.splitId = splitId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ChildVenueSplitId that)) {
            return false;
        }
        return Objects.equals(tradeDate, that.tradeDate) && Objects.equals(splitId, that.splitId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tradeDate, splitId);
    }
}
