package com.jk.apps.banking.fx.sor.topology.mcp.config;

import java.time.Duration;

import org.springframework.boot.web.client.ClientHttpRequestFactories;
import org.springframework.boot.web.client.ClientHttpRequestFactorySettings;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {

    @Bean
    RestClient topologyRestClient(McpClientProperties props) {
        return RestClient.builder()
                .baseUrl(props.getTopologyBaseUrl())
                .requestFactory(requestFactory(props))
                .build();
    }

    @Bean
    RestClient adminRestClient(McpClientProperties props) {
        return RestClient.builder()
                .baseUrl(props.getAdminBaseUrl())
                .requestFactory(requestFactory(props))
                .build();
    }

    private static ClientHttpRequestFactory requestFactory(McpClientProperties props) {
        ClientHttpRequestFactorySettings settings = ClientHttpRequestFactorySettings.DEFAULTS
                .withConnectTimeout(Duration.ofMillis(props.getConnectTimeoutMs()))
                .withReadTimeout(Duration.ofMillis(props.getReadTimeoutMs()));
        return ClientHttpRequestFactories.get(settings);
    }
}
