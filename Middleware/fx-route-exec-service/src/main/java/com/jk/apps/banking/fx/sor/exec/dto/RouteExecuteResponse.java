package com.jk.apps.banking.fx.sor.exec.dto;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class RouteExecuteResponse {

    private UUID orderId;
    private String clientOrderId;
    private String status;
    private String executionMode;
    private BigDecimal executedRate;
    private BigDecimal slippageBps;
    private List<ChildSplitDto> childSplits = new ArrayList<>();
    private boolean idempotentReplay;

    public UUID getOrderId() {
        return orderId;
    }

    public void setOrderId(UUID orderId) {
        this.orderId = orderId;
    }

    public String getClientOrderId() {
        return clientOrderId;
    }

    public void setClientOrderId(String clientOrderId) {
        this.clientOrderId = clientOrderId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getExecutionMode() {
        return executionMode;
    }

    public void setExecutionMode(String executionMode) {
        this.executionMode = executionMode;
    }

    public BigDecimal getExecutedRate() {
        return executedRate;
    }

    public void setExecutedRate(BigDecimal executedRate) {
        this.executedRate = executedRate;
    }

    public BigDecimal getSlippageBps() {
        return slippageBps;
    }

    public void setSlippageBps(BigDecimal slippageBps) {
        this.slippageBps = slippageBps;
    }

    public List<ChildSplitDto> getChildSplits() {
        return childSplits;
    }

    public void setChildSplits(List<ChildSplitDto> childSplits) {
        this.childSplits = childSplits;
    }

    public boolean isIdempotentReplay() {
        return idempotentReplay;
    }

    public void setIdempotentReplay(boolean idempotentReplay) {
        this.idempotentReplay = idempotentReplay;
    }
}
