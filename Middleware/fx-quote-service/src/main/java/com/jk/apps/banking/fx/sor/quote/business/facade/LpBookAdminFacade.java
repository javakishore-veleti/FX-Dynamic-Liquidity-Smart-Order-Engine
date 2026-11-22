package com.jk.apps.banking.fx.sor.quote.business.facade;

import com.jk.apps.banking.fx.sor.quote.cache.LpQuote;
import com.jk.apps.banking.fx.sor.quote.cache.QuoteBookCache;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

/** CLOSED — CRUD for LP top-of-book seed/config visible to Admin. */
@Component
public class LpBookAdminFacade {

    private final QuoteBookCache quoteBookCache;

    public LpBookAdminFacade(QuoteBookCache quoteBookCache) {
        this.quoteBookCache = quoteBookCache;
    }

    public List<LpQuote> listAll() {
        return quoteBookCache.listAll().stream()
                .sorted(Comparator.comparing(LpQuote::getCcyPair).thenComparing(LpQuote::getLpId))
                .toList();
    }

    public LpQuote upsert(LpQuote request) {
        if (request.getCcyPair() == null || request.getLpId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "ccyPair and lpId required");
        }
        String pair = request.getCcyPair().replace("/", "").toUpperCase(Locale.ROOT);
        String lp = request.getLpId().trim().toUpperCase(Locale.ROOT);
        long now = Instant.now().toEpochMilli();
        LpQuote quote = new LpQuote(
                pair,
                lp,
                request.getBid() == null ? new BigDecimal("1.08480") : request.getBid(),
                request.getAsk() == null ? new BigDecimal("1.08500") : request.getAsk(),
                request.getAvailableSize() == null ? new BigDecimal("5000000") : request.getAvailableSize(),
                request.getQuoteTs() <= 0 ? now : request.getQuoteTs(),
                request.getTtlMs() <= 0 ? 2_000L : request.getTtlMs());
        quoteBookCache.put(quote);
        return quote;
    }

    public void delete(String ccyPair, String lpId) {
        quoteBookCache.delete(
                ccyPair.replace("/", "").toUpperCase(Locale.ROOT),
                lpId.trim().toUpperCase(Locale.ROOT));
    }
}
