package com.jk.apps.banking.fx.sor.quote.business.facade;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.springframework.core.io.ClassPathResource;

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

    private final ObjectMapper mapper;

    public LpBookAdminFacade(QuoteBookCache quoteBookCache, ObjectMapper mapper) {
        this.mapper = mapper;
        this.quoteBookCache = quoteBookCache;
    }

    public List<LpQuote> listAll() {
        return quoteBookCache.listAll().stream()
                .sorted(Comparator.comparing(LpQuote::getCcyPair).thenComparing(LpQuote::getLpId))
                .toList();
    }

    /** Loads the committed LP book starter (master/lp-book.json). Idempotent. Called only by the admin workflow. */
    public int loadCommitted() {
        List<Map<String, String>> rows;
        try (InputStream in = new ClassPathResource("master/lp-book.json").getInputStream()) {
            rows = mapper.readValue(in, new TypeReference<List<Map<String, String>>>() {
            });
        } catch (IOException ex) {
            throw new IllegalStateException("cannot read master/lp-book.json", ex);
        }
        long now = Instant.now().toEpochMilli();
        for (Map<String, String> r : rows) {
            quoteBookCache.put(new LpQuote(
                    r.get("ccyPair"), r.get("lpId"),
                    new BigDecimal(r.get("bid")), new BigDecimal(r.get("ask")),
                    new BigDecimal(r.get("availableSizeUsd")), now, 2_000L));
        }
        return rows.size();
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
