package com.jk.apps.banking.fx.sor.agent.config;

import com.jk.apps.banking.fx.sor.agent.llm.LlmProviderProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
@EnableConfigurationProperties({
        FxFeatureProperties.class,
        McpClientProperties.class,
        LlmProviderProperties.class
})
public class AppConfig {

    @Bean
    RestClient mcpRestClient(McpClientProperties properties) {
        return RestClient.builder()
                .baseUrl(properties.getBaseUrl())
                .build();
    }
}
