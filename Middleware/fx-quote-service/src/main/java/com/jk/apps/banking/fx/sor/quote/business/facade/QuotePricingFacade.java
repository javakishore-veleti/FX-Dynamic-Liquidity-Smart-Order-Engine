package com.jk.apps.banking.fx.sor.quote.business.facade;

import com.jk.apps.banking.fx.sor.quote.cache.LpQuote;
import com.jk.apps.banking.fx.sor.quote.cache.QuoteBookCache;
import com.jk.apps.banking.fx.sor.quote.cache.SpreadScheduleCache;
import com.jk.apps.banking.fx.sor.quote.dto.IndicativeQuoteRequest;
import com.jk.apps.banking.fx.sor.quote.dto.IndicativeQuoteResponse;
import com.jk.apps.banking.fx.sor.quote.dto.QuoteTickEvent;
import com.jk.apps.banking.fx.sor.quote.dto.RateSnapshotResponse;
import com.jk.apps.banking.fx.sor.quote.dto.RegimeContextResponse;
import com.jk.apps.banking.fx.sor.quote.entity.ClientTierSpreadSchedule;
import com.jk.apps.banking.fx.sor.quote.eventing.DomainEventPublisher;
import com.jk.apps.banking.fx.sor.quote.metrics.ServiceMetrics;
import jakarta.annotation.PostConstruct;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

/**
 * CLOSED business orchestration — FR-01 session/regime markup + pricing hot path.
 */
@Component
public class QuotePricingFacade {

    private static final String DEFAULT_REGIME = "LONDON_NY_OVERLAP";
    private static final long DEFAULT_TTL_MS = 2_000L;
    private static final String TOPIC_REGIME_SHIFTED = "fx.quote.regime-shifted.v1";
    private static final String TOPIC_STALE_FEED = "fx.quote.stale-feed-detected.v1";
    private static final String TOPIC_LAST_LOOK_HINT = "fx.quote.last-look-penalty-hint.v1";

    private final SpreadScheduleCache scheduleCache;
    private final QuoteBookCache quoteBookCache;
    private final MacroRegimeFacade macroRegimeFacade;
    private final ServiceMetrics metrics;
    private final DomainEventPublisher domainEvents;
    private final AtomicReference<String> lastObservedRegime = new AtomicReference<>();

    public QuotePricingFacade(
            SpreadScheduleCache scheduleCache,
            QuoteBookCache quoteBookCache,
            MacroRegimeFacade macroRegimeFacade,
            ServiceMetrics metrics,
            DomainEventPublisher domainEvents) {
        this.scheduleCache = scheduleCache;
        this.quoteBookCache = quoteBookCache;
        this.macroRegimeFacade = macroRegimeFacade;
        this.metrics = metrics;
        this.domainEvents = domainEvents;
    }

    public IndicativeQuoteResponse priceIndicative(IndicativeQuoteRequest request) {
        metrics.quotesIndicativeRequests().increment();
        String ccyPair = normalizePair(request.getCcyPair());
        String tier = request.getClientTier() == null ? "TIER_2" : request.getClientTier();
        String side = request.getSide() == null ? "BUY" : request.getSide().toUpperCase(Locale.ROOT);
        String regime = resolveActiveRegime();

        BigDecimal markup = resolveEffectiveMarkup(tier, ccyPair, regime);
        maybeEmitRegimeShift(ccyPair, regime, markup);
        publishLastLookPenaltyHint(regime);

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
        return new RateSnapshotResponse(pair, tob.bid(), tob.ask(), mid, tob.activeLpCount(), resolveActiveRegime());
    }

    public List<QuoteTickEvent> streamMarkedTicks(String ccyPairs, String clientTier, String tenor) {
        metrics.quotesStreamSubscriptions().increment();
        String tier = clientTier == null ? "TIER_2" : clientTier;
        String regime = resolveActiveRegime();
        List<String> pairs = parsePairs(ccyPairs);
        BigDecimal markup = resolveEffectiveMarkup(tier, pairs.getFirst(), regime);
        maybeEmitRegimeShift(pairs.getFirst(), regime, markup);
        publishLastLookPenaltyHint(regime);
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

    public RegimeContextResponse regimeContext(String clientTier, String ccyPair) {
        String tier = clientTier == null ? "TIER_2" : clientTier;
        String pair = normalizePair(ccyPair);
        String regime = resolveActiveRegime();
        return new RegimeContextResponse(
                regime,
                macroRegimeFacade.isMacroActive(),
                lastLookPenaltyWeight(regime),
                resolveEffectiveMarkup(tier, pair, regime));
    }

    public String resolveActiveRegime() {
        return macroRegimeFacade.current()
                .map(MacroRegimeFacade.MacroFlag::regime)
                .orElseGet(QuotePricingFacade::clockSessionRegime);
    }

    public BigDecimal resolveEffectiveMarkup(String tier, String ccyPair, String regime) {
        ClientTierSpreadSchedule row = scheduleCache.find(tier, ccyPair, regime)
                .filter(r -> "ACTIVE".equalsIgnoreCase(nullToActive(r.getStatus())))
                .orElseGet(() -> scheduleCache.find(tier, ccyPair, DEFAULT_REGIME)
                        .filter(r -> "ACTIVE".equalsIgnoreCase(nullToActive(r.getStatus())))
                        .orElse(null));
        if (row == null) {
            return new BigDecimal("1.5000");
        }
        BigDecimal markup = row.getMarkupBps() == null ? new BigDecimal("1.5000") : row.getMarkupBps();
        BigDecimal news = row.getNewsMultiplier() == null ? BigDecimal.ONE : row.getNewsMultiplier();
        // Macro flag can further scale when active and schedule is MACRO_NEWS_HIGH
        if (macroRegimeFacade.isMacroActive() && MacroRegimeFacade.MACRO_NEWS_HIGH.equals(regime)) {
            BigDecimal sev = macroRegimeFacade.current().map(MacroRegimeFacade.MacroFlag::severity).orElse(BigDecimal.ONE);
            news = news.multiply(sev.max(BigDecimal.ONE));
        }
        return markup.multiply(news).setScale(4, RoundingMode.HALF_UP);
    }

    public BigDecimal lastLookPenaltyWeight(String regime) {
        String r = regime == null ? DEFAULT_REGIME : regime;
        if (MacroRegimeFacade.MACRO_NEWS_HIGH.equals(r)) {
            return new BigDecimal("1.75");
        }
        if ("NY_ROLLOVER".equals(r)) {
            return new BigDecimal("1.50");
        }
        if ("TOKYO".equals(r)) {
            return new BigDecimal("1.20");
        }
        return BigDecimal.ONE;
    }

    public void publishLastLookPenaltyHint(String regime) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("eventId", UUID.randomUUID().toString());
        payload.put("sessionRegime", regime);
        payload.put("lastLookPenaltyWeight", lastLookPenaltyWeight(regime));
        payload.put("macroActive", macroRegimeFacade.isMacroActive());
        payload.put("timestamp", Instant.now().toString());
        domainEvents.publish(TOPIC_LAST_LOOK_HINT, payload);
    }

    private void maybeEmitRegimeShift(String ccyPair, String activeRegime, BigDecimal spreadMultiplier) {
        String previous = lastObservedRegime.getAndSet(activeRegime);
        if (previous == null || previous.equals(activeRegime)) {
            return;
        }
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("eventId", UUID.randomUUID().toString());
        payload.put("ccyPair", ccyPair);
        payload.put("previousRegime", previous);
        payload.put("activeRegime", activeRegime);
        payload.put("spreadMultiplier", spreadMultiplier);
        payload.put("lastLookPenaltyWeight", lastLookPenaltyWeight(activeRegime));
        payload.put("timestamp", Instant.now().toString());
        domainEvents.publish(TOPIC_REGIME_SHIFTED, payload);
    }

    private void emitStaleFeed(LpQuote quote, long nowMs) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("eventId", UUID.randomUUID().toString());
        payload.put("lpId", quote.getLpId());
        payload.put("ccyPair", quote.getCcyPair());
        payload.put("lastQuoteTs", Instant.ofEpochMilli(quote.getQuoteTs()).toString());
        payload.put("ageMs", nowMs - quote.getQuoteTs());
        payload.put("thresholdMs", quote.getTtlMs());
        payload.put("timestamp", Instant.now().toString());
        domainEvents.publish(TOPIC_STALE_FEED, payload);
    }

    private List<LpQuote> freshQuotes(String ccyPair, long now) {
        List<LpQuote> quotes = quoteBookCache.getByCcyPair(ccyPair);
        List<LpQuote> fresh = quotes.stream()
                .filter(q -> !q.isStale(now))
                .collect(Collectors.toCollection(ArrayList::new));
        long stale = quotes.size() - fresh.size();
        if (stale > 0) {
            metrics.quotesStaleFiltered().increment(stale);
            for (LpQuote q : quotes) {
                if (q.isStale(now)) {
                    emitStaleFeed(q, now);
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

    static String clockSessionRegime() {
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
                .map(QuotePricingFacade::normalizePair)
                .filter(s -> !s.isBlank())
                .toList();
    }

    private static String nullToActive(String status) {
        return status == null || status.isBlank() ? "ACTIVE" : status;
    }

    private record TopOfBook(BigDecimal bid, BigDecimal ask, int activeLpCount) {
    }
}
