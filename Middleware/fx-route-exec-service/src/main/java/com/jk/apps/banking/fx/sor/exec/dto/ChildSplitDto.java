package com.jk.apps.banking.fx.sor.exec.dto;

import java.math.BigDecimal;
import java.util.UUID;

public class ChildSplitDto {

    private UUID splitId;
    private String lpId;
    private BigDecimal allocatedNotional;
    private BigDecimal fillRate;
    private int holdLatencyMs;
    private String lpStatus;

    public ChildSplitDto() {
    }

    public ChildSplitDto(
            UUID splitId,
            String lpId,
            BigDecimal allocatedNotional,
            BigDecimal fillRate,
            int holdLatencyMs,
            String lpStatus) {
        this.splitId = splitId;
        this.lpId = lpId;
        this.allocatedNotional = allocatedNotional;
        this.fillRate = fillRate;
        this.holdLatencyMs = holdLatencyMs;
        this.lpStatus = lpStatus;
    }

    public UUID getSplitId() {
        return splitId;
    }

    public void setSplitId(UUID splitId) {
        this.splitId = splitId;
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
