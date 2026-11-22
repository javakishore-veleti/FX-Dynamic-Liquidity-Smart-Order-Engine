package com.jk.apps.banking.fx.sor.quote.cache;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryQuoteBookCache implements QuoteBookCache {

    private final ConcurrentHashMap<String, ConcurrentHashMap<String, LpQuote>> books =
            new ConcurrentHashMap<>();

    @Override
    public void put(LpQuote quote) {
        books.computeIfAbsent(quote.getCcyPair(), k -> new ConcurrentHashMap<>())
                .put(quote.getLpId(), quote);
    }

    @Override
    public List<LpQuote> getByCcyPair(String ccyPair) {
        Map<String, LpQuote> book = books.get(ccyPair);
        if (book == null || book.isEmpty()) {
            return List.of();
        }
        return new ArrayList<>(book.values());
    }

    @Override
    public List<LpQuote> listAll() {
        List<LpQuote> all = new ArrayList<>();
        for (Map<String, LpQuote> book : books.values()) {
            all.addAll(book.values());
        }
        return all;
    }

    @Override
    public void delete(String ccyPair, String lpId) {
        Map<String, LpQuote> book = books.get(ccyPair);
        if (book != null) {
            book.remove(lpId);
        }
    }
}
