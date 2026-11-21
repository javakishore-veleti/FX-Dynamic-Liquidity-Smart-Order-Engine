package com.jk.apps.banking.fx.sor.exec.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "fx_execution_orders")
@IdClass(FxExecutionOrderId.class)
public class FxExecutionOrder {

    @Id
    @Column(name = "trade_date", nullable = false)
    private LocalDate tradeDate;

    @Id
    @Column(name = "order_id", nullable = false)
    private UUID orderId;

    @Column(name = "client_order_id", length = 64, nullable = false)
    private String clientOrderId;

    @Column(name = "client_id", length = 50, nullable = false)
    private String clientId;

    @Column(name = "ccy_pair", length = 10, nullable = false)
    private String ccyPair;

    @Column(name = "side", length = 4, nullable = false)
    private String side;

    @Column(name = "requested_notional", nullable = false, precision = 18, scale = 2)
    private BigDecimal requestedNotional;

    @Column(name = "executed_rate", nullable = false, precision = 18, scale = 8)
    private BigDecimal executedRate;

    @Column(name = "slippage_bps", nullable = false, precision = 8, scale = 4)
    private BigDecimal slippageBps;

    @Column(name = "execution_mode", length = 20, nullable = false)
    private String executionMode;

    @Column(name = "status", length = 20, nullable = false)
    private String status;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

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

    public String getClientOrderId() {
        return clientOrderId;
    }

    public void setClientOrderId(String clientOrderId) {
        this.clientOrderId = clientOrderId;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getCcyPair() {
        return ccyPair;
    }

    public void setCcyPair(String ccyPair) {
        this.ccyPair = ccyPair;
    }

    public String getSide() {
        return side;
    }

    public void setSide(String side) {
        this.side = side;
    }

    public BigDecimal getRequestedNotional() {
        return requestedNotional;
    }

    public void setRequestedNotional(BigDecimal requestedNotional) {
        this.requestedNotional = requestedNotional;
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

    public String getExecutionMode() {
        return executionMode;
    }

    public void setExecutionMode(String executionMode) {
        this.executionMode = executionMode;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
