package com.jk.apps.banking.fx.sor.quote.dto;

/** Currency pair as returned by the owner API. */
public record CurrencyPairDto(String pairCode, String baseCcy, String quoteCcy) {
}
