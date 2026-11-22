package com.jk.apps.banking.fx.sor.quote.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

/** Currency pair master. pair_code is the system-unique key; other services store it as a plain column. */
@Entity
@Table(name = "currency_pairs")
public class CurrencyPair {

    @Id
    @Column(name = "pair_code", length = 10, nullable = false)
    private String pairCode;

    @Column(name = "base_ccy", length = 3, nullable = false)
    private String baseCcy;

    @Column(name = "quote_ccy", length = 3, nullable = false)
    private String quoteCcy;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "created_by", length = 100, nullable = false, updatable = false)
    private String createdBy;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "updated_by", length = 100, nullable = false)
    private String updatedBy;

    protected CurrencyPair() {
    }

    public CurrencyPair(String pairCode, String baseCcy, String quoteCcy, String actor, Instant now) {
        this.pairCode = pairCode;
        this.baseCcy = baseCcy;
        this.quoteCcy = quoteCcy;
        this.createdAt = now;
        this.createdBy = actor;
        this.updatedAt = now;
        this.updatedBy = actor;
    }

    public String getPairCode() {
        return pairCode;
    }

    public String getBaseCcy() {
        return baseCcy;
    }

    public String getQuoteCcy() {
        return quoteCcy;
    }
}
