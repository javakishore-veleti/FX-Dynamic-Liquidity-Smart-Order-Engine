package com.jk.apps.banking.fx.sor.quote.cache;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.data.redis.core.StringRedisTemplate;

public class RedisQuoteBookCache implements QuoteBookCache {

    private static final String KEY_PREFIX = "quote:lp:";

    private final StringRedisTemplate redis;

    public RedisQuoteBookCache(StringRedisTemplate redis) {
        this.redis = redis;
    }

    @Override
    public void put(LpQuote quote) {
        String key = KEY_PREFIX + quote.getCcyPair() + ":" + quote.getLpId();
        redis.opsForHash()
                .putAll(
                        key,
                        Map.of(
                                "bid", quote.getBid().toPlainString(),
                                "ask", quote.getAsk().toPlainString(),
                                "available_size", quote.getAvailableSize().toPlainString(),
                                "quote_ts", Long.toString(quote.getQuoteTs()),
                                "ttl_ms", Long.toString(quote.getTtlMs())));
        redis.expire(key, Duration.ofMillis(Math.max(quote.getTtlMs(), 1_000L)));
    }

    @Override
    public List<LpQuote> getByCcyPair(String ccyPair) {
        Set<String> keys = redis.keys(KEY_PREFIX + ccyPair + ":*");
        if (keys == null || keys.isEmpty()) {
            return List.of();
        }
        List<LpQuote> quotes = new ArrayList<>();
        for (String key : keys) {
            Map<Object, Object> hash = redis.opsForHash().entries(key);
            if (hash.isEmpty()) {
                continue;
            }
            String lpId = key.substring(key.lastIndexOf(':') + 1);
            quotes.add(
                    new LpQuote(
                            ccyPair,
                            lpId,
                            new BigDecimal(String.valueOf(hash.get("bid"))),
                            new BigDecimal(String.valueOf(hash.get("ask"))),
                            new BigDecimal(String.valueOf(hash.get("available_size"))),
                            Long.parseLong(String.valueOf(hash.get("quote_ts"))),
                            Long.parseLong(String.valueOf(hash.get("ttl_ms")))));
        }
        return quotes;
    }
}
