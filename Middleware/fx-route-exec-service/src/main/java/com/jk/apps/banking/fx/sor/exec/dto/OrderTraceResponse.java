package com.jk.apps.banking.fx.sor.exec.dto;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class OrderTraceResponse {

    private String clientOrderId;
    private UUID orderId;
    private String status;
    private String executionMode;
    private BigDecimal executedRate;
    private BigDecimal slippageBps;
    private String droolsRuleVersion;
    private List<String> evaluatedLps = new ArrayList<>();
    private List<String> knockoutCodes = new ArrayList<>();
    private List<ChildSplitDto> childSplits = new ArrayList<>();

    public String getClientOrderId() {
        return clientOrderId;
    }

    public void setClientOrderId(String clientOrderId) {
        this.clientOrderId = clientOrderId;
    }

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

    public String getDroolsRuleVersion() {
        return droolsRuleVersion;
    }

    public void setDroolsRuleVersion(String droolsRuleVersion) {
        this.droolsRuleVersion = droolsRuleVersion;
    }

    public List<String> getEvaluatedLps() {
        return evaluatedLps;
    }

    public void setEvaluatedLps(List<String> evaluatedLps) {
        this.evaluatedLps = evaluatedLps;
    }

    public List<String> getKnockoutCodes() {
        return knockoutCodes;
    }

    public void setKnockoutCodes(List<String> knockoutCodes) {
        this.knockoutCodes = knockoutCodes;
    }

    public List<ChildSplitDto> getChildSplits() {
        return childSplits;
    }

    public void setChildSplits(List<ChildSplitDto> childSplits) {
        this.childSplits = childSplits;
    }
}
