package com.jk.apps.banking.fx.sor.exec.config;

import com.jk.apps.banking.fx.sor.exec.cache.IdempotencyStore;
import com.jk.apps.banking.fx.sor.exec.cache.InMemoryIdempotencyStore;
import com.jk.apps.banking.fx.sor.exec.cache.RedisIdempotencyStore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;

@Configuration
public class CacheConfig {

    @Bean
    @ConditionalOnProperty(prefix = "fx.features", name = "redis-enabled", havingValue = "false", matchIfMissing = true)
    public IdempotencyStore inMemoryIdempotencyStore() {
        return new InMemoryIdempotencyStore();
    }

    @Bean
    @ConditionalOnProperty(prefix = "fx.features", name = "redis-enabled", havingValue = "true")
    public StringRedisTemplate stringRedisTemplate(RedisConnectionFactory connectionFactory) {
        return new StringRedisTemplate(connectionFactory);
    }

    @Bean
    @ConditionalOnProperty(prefix = "fx.features", name = "redis-enabled", havingValue = "true")
    public IdempotencyStore redisIdempotencyStore(StringRedisTemplate stringRedisTemplate) {
        return new RedisIdempotencyStore(stringRedisTemplate);
    }
}
