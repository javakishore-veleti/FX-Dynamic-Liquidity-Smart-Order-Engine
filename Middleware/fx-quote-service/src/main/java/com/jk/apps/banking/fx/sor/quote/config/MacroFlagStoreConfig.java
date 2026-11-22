package com.jk.apps.banking.fx.sor.quote.config;

import com.jk.apps.banking.fx.sor.quote.macro.InMemoryMacroFlagStore;
import com.jk.apps.banking.fx.sor.quote.macro.MacroFlagStore;
import com.jk.apps.banking.fx.sor.quote.macro.RedisMacroFlagStore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;

@Configuration
public class MacroFlagStoreConfig {

    @Bean
    @ConditionalOnProperty(prefix = "fx.features", name = "redis-enabled", havingValue = "false", matchIfMissing = true)
    public MacroFlagStore inMemoryMacroFlagStore() {
        return new InMemoryMacroFlagStore();
    }

    @Bean
    @ConditionalOnProperty(prefix = "fx.features", name = "redis-enabled", havingValue = "true")
    public MacroFlagStore redisMacroFlagStore(StringRedisTemplate stringRedisTemplate) {
        return new RedisMacroFlagStore(stringRedisTemplate);
    }
}
