package com.jk.apps.banking.fx.sor.topology.mcp.config;

import java.net.http.HttpClient;
import java.time.Duration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.JdkClientHttpRequestFactory;
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
        HttpClient http = HttpClient.newBuilder()
                .connectTimeout(Duration.ofMillis(props.getConnectTimeoutMs()))
                .build();
        JdkClientHttpRequestFactory factory = new JdkClientHttpRequestFactory(http);
        factory.setReadTimeout(Duration.ofMillis(props.getReadTimeoutMs()));
        return factory;
    }
}
