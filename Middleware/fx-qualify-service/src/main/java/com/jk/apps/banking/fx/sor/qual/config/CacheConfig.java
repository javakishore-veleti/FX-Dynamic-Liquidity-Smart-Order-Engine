package com.jk.apps.banking.fx.sor.qual.config;

import com.jk.apps.banking.fx.sor.qual.cache.CreditStore;
import com.jk.apps.banking.fx.sor.qual.cache.InMemoryCreditStore;
import com.jk.apps.banking.fx.sor.qual.cache.RedisCreditStore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;

@Configuration
public class CacheConfig {

    @Bean
    @ConditionalOnProperty(prefix = "fx.features", name = "redis-enabled", havingValue = "false", matchIfMissing = true)
    public CreditStore inMemoryCreditStore() {
        return new InMemoryCreditStore();
    }

    @Bean
    @ConditionalOnProperty(prefix = "fx.features", name = "redis-enabled", havingValue = "true")
    public StringRedisTemplate stringRedisTemplate(RedisConnectionFactory connectionFactory) {
        return new StringRedisTemplate(connectionFactory);
    }

    @Bean
    @ConditionalOnProperty(prefix = "fx.features", name = "redis-enabled", havingValue = "true")
    public CreditStore redisCreditStore(StringRedisTemplate stringRedisTemplate) {
        return new RedisCreditStore(stringRedisTemplate);
    }
}
