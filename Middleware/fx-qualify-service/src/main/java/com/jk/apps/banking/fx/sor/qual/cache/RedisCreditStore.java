package com.jk.apps.banking.fx.sor.qual.cache;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;

public class RedisCreditStore implements CreditStore {

    private static final String KEY_PREFIX = "credit:bilateral:";
    private static final DefaultRedisScript<Long> RESERVE_SCRIPT = new DefaultRedisScript<>(
            """
            local maxc = tonumber(redis.call('HGET', KEYS[1], 'max_credit_usd') or '0')
            local util = tonumber(redis.call('HGET', KEYS[1], 'utilized_usd') or '0')
            local reserved = tonumber(redis.call('HGET', KEYS[1], 'reserved_usd') or '0')
            local need = tonumber(ARGV[1])
            if (maxc - util - reserved) < need then
              return 0
            end
            redis.call('HINCRBYFLOAT', KEYS[1], 'reserved_usd', need)
            return 1
            """,
            Long.class);

    private final StringRedisTemplate redis;

    public RedisCreditStore(StringRedisTemplate redis) {
        this.redis = redis;
    }

    private static String key(String clientId, String lpId) {
        return KEY_PREFIX + clientId + ":" + lpId;
    }

    @Override
    public Optional<CreditLine> get(String clientId, String lpId) {
        Map<Object, Object> hash = redis.opsForHash().entries(key(clientId, lpId));
        if (hash.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(new CreditLine(
                clientId,
                lpId,
                new BigDecimal(String.valueOf(hash.get("max_credit_usd"))),
                new BigDecimal(String.valueOf(hash.get("utilized_usd"))),
                new BigDecimal(String.valueOf(hash.get("reserved_usd"))),
                Long.parseLong(String.valueOf(hash.get("reset_epoch")))));
    }

    @Override
    public void put(CreditLine line) {
        redis.opsForHash()
                .putAll(
                        key(line.getClientId(), line.getLpId()),
                        Map.of(
                                "max_credit_usd", line.getMaxCreditUsd().toPlainString(),
                                "utilized_usd", line.getUtilizedUsd().toPlainString(),
                                "reserved_usd", line.getReservedUsd().toPlainString(),
                                "reset_epoch", Long.toString(line.getResetEpoch())));
    }

    @Override
    public boolean tryReserve(String clientId, String lpId, BigDecimal notionalUsd) {
        Long result = redis.execute(
                RESERVE_SCRIPT,
                java.util.List.of(key(clientId, lpId)),
                notionalUsd.toPlainString());
        return result != null && result == 1L;
    }

    @Override
    public java.util.List<CreditLine> listAll() {
        java.util.Set<String> keys = redis.keys(KEY_PREFIX + "*");
        if (keys == null || keys.isEmpty()) {
            return java.util.List.of();
        }
        java.util.List<CreditLine> out = new java.util.ArrayList<>();
        for (String redisKey : keys) {
            String suffix = redisKey.substring(KEY_PREFIX.length());
            int sep = suffix.indexOf(':');
            if (sep <= 0) {
                continue;
            }
            get(suffix.substring(0, sep), suffix.substring(sep + 1)).ifPresent(out::add);
        }
        return out;
    }

    @Override
    public void delete(String clientId, String lpId) {
        redis.delete(key(clientId, lpId));
    }
}
