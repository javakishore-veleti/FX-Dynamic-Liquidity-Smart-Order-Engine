package com.jk.apps.banking.fx.sor.exec.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "child_venue_splits")
@IdClass(ChildVenueSplitId.class)
public class ChildVenueSplit {

    @Id
    @Column(name = "trade_date", nullable = false)
    private LocalDate tradeDate;

    @Id
    @Column(name = "split_id", nullable = false)
    private UUID splitId;

    @Column(name = "order_id", nullable = false)
    private UUID orderId;

    @Column(name = "lp_id", length = 30, nullable = false)
    private String lpId;

    @Column(name = "allocated_notional", nullable = false, precision = 18, scale = 2)
    private BigDecimal allocatedNotional;

    @Column(name = "fill_rate", precision = 18, scale = 8)
    private BigDecimal fillRate;

    @Column(name = "hold_latency_ms", nullable = false)
    private int holdLatencyMs;

    @Column(name = "lp_status", length = 20, nullable = false)
    private String lpStatus;

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

    public UUID getOrderId() {
        return orderId;
    }

    public void setOrderId(UUID orderId) {
        this.orderId = orderId;
    }

    public String getLpId() {
        return lpId;
    }

    public void setLpId(String lpId) {
        this.lpId = lpId;
    }

    public BigDecimal getAllocatedNotional() {
        return allocatedNotional;
    }

    public void setAllocatedNotional(BigDecimal allocatedNotional) {
        this.allocatedNotional = allocatedNotional;
    }

    public BigDecimal getFillRate() {
        return fillRate;
    }

    public void setFillRate(BigDecimal fillRate) {
        this.fillRate = fillRate;
    }

    public int getHoldLatencyMs() {
        return holdLatencyMs;
    }

    public void setHoldLatencyMs(int holdLatencyMs) {
        this.holdLatencyMs = holdLatencyMs;
    }

    public String getLpStatus() {
        return lpStatus;
    }

    public void setLpStatus(String lpStatus) {
        this.lpStatus = lpStatus;
    }
}
