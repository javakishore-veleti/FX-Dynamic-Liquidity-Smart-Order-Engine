package com.jk.apps.banking.fx.sor.quote.macro;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import org.springframework.data.redis.core.StringRedisTemplate;

public class RedisMacroFlagStore implements MacroFlagStore {

    static final String KEY = "fx:quote:macro-flag";

    private final StringRedisTemplate redis;

    public RedisMacroFlagStore(StringRedisTemplate redis) {
        this.redis = redis;
    }

    @Override
    public void save(MacroFlag flag) {
        long ttlSeconds = Math.max(1L, Duration.between(Instant.now(), flag.expiresAt()).getSeconds());
        redis.opsForHash()
                .putAll(
                        KEY,
                        Map.of(
                                "regime", flag.regime(),
                                "source", nullToEmpty(flag.source()),
                                "severity", flag.severity().toPlainString(),
                                "expiresAt", flag.expiresAt().toString(),
                                "armedBy", nullToEmpty(flag.armedBy())));
        redis.expire(KEY, Duration.ofSeconds(ttlSeconds));
    }

    @Override
    public void clear() {
        redis.delete(KEY);
    }

    @Override
    public Optional<MacroFlag> load() {
        Map<Object, Object> hash = redis.opsForHash().entries(KEY);
        if (hash == null || hash.isEmpty()) {
            return Optional.empty();
        }
        try {
            return Optional.of(new MacroFlag(
                    String.valueOf(hash.get("regime")),
                    String.valueOf(hash.getOrDefault("source", "")),
                    new BigDecimal(String.valueOf(hash.get("severity"))),
                    Instant.parse(String.valueOf(hash.get("expiresAt"))),
                    String.valueOf(hash.getOrDefault("armedBy", ""))));
        } catch (RuntimeException ex) {
            redis.delete(KEY);
            return Optional.empty();
        }
    }

    private static String nullToEmpty(String value) {
        return value == null ? "" : value;
    }
}
