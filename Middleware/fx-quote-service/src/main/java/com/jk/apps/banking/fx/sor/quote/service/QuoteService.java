package com.jk.apps.banking.fx.sor.quote.service;

import com.jk.apps.banking.fx.sor.quote.cache.LpQuote;
import com.jk.apps.banking.fx.sor.quote.cache.QuoteBookCache;
import com.jk.apps.banking.fx.sor.quote.dto.IndicativeQuoteRequest;
import com.jk.apps.banking.fx.sor.quote.dto.IndicativeQuoteResponse;
import com.jk.apps.banking.fx.sor.quote.dto.QuoteTickEvent;
import com.jk.apps.banking.fx.sor.quote.dto.RateSnapshotResponse;
import com.jk.apps.banking.fx.sor.quote.entity.ClientTierSpreadSchedule;
import com.jk.apps.banking.fx.sor.quote.metrics.ServiceMetrics;
import com.jk.apps.banking.fx.sor.quote.repository.ClientTierSpreadScheduleRepository;
import jakarta.annotation.PostConstruct;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class QuoteService {

    private static final String DEFAULT_REGIME = "LONDON_NY_OVERLAP";
    private static final long DEFAULT_TTL_MS = 2_000L;

    private final ClientTierSpreadScheduleRepository scheduleRepository;
    private final QuoteBookCache quoteBookCache;
    private final ServiceMetrics metrics;

    public QuoteService(
            ClientTierSpreadScheduleRepository scheduleRepository,
            QuoteBookCache quoteBookCache,
            ServiceMetrics metrics) {
        this.scheduleRepository = scheduleRepository;
        this.quoteBookCache = quoteBookCache;
        this.metrics = metrics;
    }

    @PostConstruct
    void seedReferenceBook() {
        long now = Instant.now().toEpochMilli();
        quoteBookCache.put(new LpQuote(
                "EURUSD", "LP_CITI",
                new BigDecimal("1.08480"), new BigDecimal("1.08500"),
                new BigDecimal("5000000"), now, DEFAULT_TTL_MS));
        quoteBookCache.put(new LpQuote(
                "EURUSD", "LP_JPM",
                new BigDecimal("1.08475"), new BigDecimal("1.08505"),
                new BigDecimal("7500000"), now, DEFAULT_TTL_MS));
        quoteBookCache.put(new LpQuote(
                "EURUSD", "LP_GS",
                new BigDecimal("1.08470"), new BigDecimal("1.08510"),
                new BigDecimal("4000000"), now, DEFAULT_TTL_MS));
    }

    public IndicativeQuoteResponse indicative(IndicativeQuoteRequest request) {
        metrics.quotesIndicativeRequests().increment();
        String ccyPair = normalizePair(request.getCcyPair());
        String tier = request.getClientTier() == null ? "TIER_2" : request.getClientTier();
        String side = request.getSide() == null ? "BUY" : request.getSide().toUpperCase(Locale.ROOT);
        String regime = currentSessionRegime();

        BigDecimal markup = resolveMarkup(tier, ccyPair, regime);
        TopOfBook tob = topOfBook(ccyPair);
        BigDecimal raw = "SELL".equals(side) ? tob.bid() : tob.ask();
        BigDecimal adjustment = raw.multiply(markup)
                .divide(new BigDecimal("10000"), 8, RoundingMode.HALF_UP);
        BigDecimal executable = "SELL".equals(side) ? raw.subtract(adjustment) : raw.add(adjustment);

        return new IndicativeQuoteResponse(
                "Q-" + UUID.randomUUID(),
                ccyPair,
                side,
                executable.setScale(8, RoundingMode.HALF_UP),
                markup,
                DEFAULT_TTL_MS,
                regime,
                tier);
    }

    public RateSnapshotResponse rates(String ccyPair) {
        String pair = normalizePair(ccyPair);
        TopOfBook tob = topOfBook(pair);
        BigDecimal mid = tob.bid().add(tob.ask()).divide(new BigDecimal("2"), 8, RoundingMode.HALF_UP);
        return new RateSnapshotResponse(pair, tob.bid(), tob.ask(), mid, tob.activeLpCount(), currentSessionRegime());
    }

    public List<QuoteTickEvent> streamTicks(String ccyPairs, String clientTier, String tenor) {
        metrics.quotesStreamSubscriptions().increment();
        String tier = clientTier == null ? "TIER_2" : clientTier;
        String regime = currentSessionRegime();
        List<String> pairs = parsePairs(ccyPairs);
        BigDecimal markup = resolveMarkup(tier, pairs.getFirst(), regime);
        long now = Instant.now().toEpochMilli();
        List<QuoteTickEvent> ticks = new ArrayList<>();
        for (String pair : pairs) {
            List<LpQuote> live = freshQuotes(pair, now);
            for (LpQuote q : live) {
                BigDecimal adj = q.getAsk().multiply(markup)
                        .divide(new BigDecimal("10000"), 8, RoundingMode.HALF_UP);
                ticks.add(new QuoteTickEvent(
                        pair,
                        q.getLpId(),
                        q.getBid().subtract(adj).setScale(8, RoundingMode.HALF_UP),
                        q.getAsk().add(adj).setScale(8, RoundingMode.HALF_UP),
                        markup,
                        tier,
                        regime,
                        q.getQuoteTs()));
            }
        }
        return ticks;
    }

    private List<LpQuote> freshQuotes(String ccyPair, long now) {
        List<LpQuote> quotes = quoteBookCache.getByCcyPair(ccyPair);
        if (quotes.isEmpty() && "EURUSD".equals(ccyPair)) {
            seedReferenceBook();
            quotes = quoteBookCache.getByCcyPair(ccyPair);
        }
        List<LpQuote> fresh = quotes.stream()
                .filter(q -> !q.isStale(now))
                .collect(Collectors.toCollection(ArrayList::new));
        long stale = quotes.size() - fresh.size();
        if (stale > 0) {
            metrics.quotesStaleFiltered().increment(stale);
            // refresh stale seed quotes so local demos keep streaming
            for (LpQuote q : quotes) {
                if (q.isStale(now)) {
                    quoteBookCache.put(new LpQuote(
                            q.getCcyPair(), q.getLpId(), q.getBid(), q.getAsk(),
                            q.getAvailableSize(), now, DEFAULT_TTL_MS));
                }
            }
            fresh = quoteBookCache.getByCcyPair(ccyPair);
        }
        return fresh;
    }

    private TopOfBook topOfBook(String ccyPair) {
        long now = Instant.now().toEpochMilli();
        List<LpQuote> quotes = freshQuotes(ccyPair, now);
        if (quotes.isEmpty()) {
            BigDecimal mid = new BigDecimal("1.08500");
            return new TopOfBook(mid, mid, 0);
        }
        BigDecimal bestBid = quotes.stream().map(LpQuote::getBid).max(Comparator.naturalOrder()).orElseThrow();
        BigDecimal bestAsk = quotes.stream().map(LpQuote::getAsk).min(Comparator.naturalOrder()).orElseThrow();
        return new TopOfBook(bestBid, bestAsk, quotes.size());
    }

    private BigDecimal resolveMarkup(String tier, String ccyPair, String regime) {
        return scheduleRepository
                .findByTierIdAndCcyPairAndSessionRegime(tier, ccyPair, regime)
                .map(ClientTierSpreadSchedule::getMarkupBps)
                .orElseGet(() -> scheduleRepository
                        .findByTierIdAndCcyPairAndSessionRegime(tier, ccyPair, DEFAULT_REGIME)
                        .map(ClientTierSpreadSchedule::getMarkupBps)
                        .orElse(new BigDecimal("1.5000")));
    }

    private static String normalizePair(String ccyPair) {
        if (ccyPair == null || ccyPair.isBlank()) {
            return "EURUSD";
        }
        return ccyPair.replace("/", "").toUpperCase(Locale.ROOT);
    }

    private static List<String> parsePairs(String ccyPairs) {
        if (ccyPairs == null || ccyPairs.isBlank()) {
            return List.of("EURUSD");
        }
        return Arrays.stream(ccyPairs.split(","))
                .map(QuoteService::normalizePair)
                .filter(s -> !s.isBlank())
                .toList();
    }

    static String currentSessionRegime() {
        int hour = ZonedDateTime.now(ZoneOffset.UTC).getHour();
        if (hour >= 0 && hour < 7) {
            return "TOKYO";
        }
        if (hour >= 12 && hour < 16) {
            return "LONDON_NY_OVERLAP";
        }
        if (hour >= 21) {
            return "NY_ROLLOVER";
        }
        return DEFAULT_REGIME;
    }

    private record TopOfBook(BigDecimal bid, BigDecimal ask, int activeLpCount) {
    }
}
