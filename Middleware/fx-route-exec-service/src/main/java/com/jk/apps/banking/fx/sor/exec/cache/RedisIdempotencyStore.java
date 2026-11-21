package com.jk.apps.banking.fx.sor.exec.cache;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.redis.core.StringRedisTemplate;

public class RedisIdempotencyStore implements IdempotencyStore {

    private static final String KEY_PREFIX = "idem:order:";
    private static final Duration TTL = Duration.ofHours(24);

    private final StringRedisTemplate redis;

    public RedisIdempotencyStore(StringRedisTemplate redis) {
        this.redis = redis;
    }

    private static String key(String clientOrderId) {
        return KEY_PREFIX + clientOrderId;
    }

    @Override
    public Optional<IdempotencyRecord> tryAcquire(String clientOrderId, IdempotencyRecord provisional) {
        Boolean acquired = redis.opsForValue()
                .setIfAbsent(key(clientOrderId), serialize(provisional), TTL);
        if (Boolean.TRUE.equals(acquired)) {
            return Optional.empty();
        }
        return get(clientOrderId);
    }

    @Override
    public void complete(String clientOrderId, IdempotencyRecord filled) {
        redis.opsForValue().set(key(clientOrderId), serialize(filled), TTL);
    }

    @Override
    public Optional<IdempotencyRecord> get(String clientOrderId) {
        String raw = redis.opsForValue().get(key(clientOrderId));
        if (raw == null || raw.isBlank()) {
            return Optional.empty();
        }
        return Optional.of(deserialize(raw));
    }

    private static String serialize(IdempotencyRecord record) {
        return record.getState()
                + "|"
                + record.getOrderId()
                + "|"
                + (record.getLockedRate() == null ? "" : record.getLockedRate().toPlainString());
    }

    private static IdempotencyRecord deserialize(String raw) {
        String[] parts = raw.split("\\|", -1);
        return new IdempotencyRecord(
                parts[0],
                UUID.fromString(parts[1]),
                parts.length > 2 && !parts[2].isBlank() ? new BigDecimal(parts[2]) : null);
    }
}
