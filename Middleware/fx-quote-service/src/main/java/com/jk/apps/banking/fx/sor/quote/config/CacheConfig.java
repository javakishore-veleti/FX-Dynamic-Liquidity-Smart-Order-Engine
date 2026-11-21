package com.jk.apps.banking.fx.sor.quote.config;

import com.jk.apps.banking.fx.sor.quote.cache.InMemoryQuoteBookCache;
import com.jk.apps.banking.fx.sor.quote.cache.QuoteBookCache;
import com.jk.apps.banking.fx.sor.quote.cache.RedisQuoteBookCache;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;

@Configuration
public class CacheConfig {

    @Bean
    @ConditionalOnProperty(prefix = "fx.features", name = "redis-enabled", havingValue = "false", matchIfMissing = true)
    public QuoteBookCache inMemoryQuoteBookCache() {
        return new InMemoryQuoteBookCache();
    }

    @Bean
    @ConditionalOnProperty(prefix = "fx.features", name = "redis-enabled", havingValue = "true")
    public StringRedisTemplate stringRedisTemplate(RedisConnectionFactory connectionFactory) {
        return new StringRedisTemplate(connectionFactory);
    }

    @Bean
    @ConditionalOnProperty(prefix = "fx.features", name = "redis-enabled", havingValue = "true")
    public QuoteBookCache redisQuoteBookCache(StringRedisTemplate stringRedisTemplate) {
        return new RedisQuoteBookCache(stringRedisTemplate);
    }
}
