package com.jk.apps.banking.fx.sor.quote.cache;

import java.util.List;

public interface QuoteBookCache {

    void put(LpQuote quote);

    List<LpQuote> getByCcyPair(String ccyPair);

    List<LpQuote> listAll();

    void delete(String ccyPair, String lpId);
}
