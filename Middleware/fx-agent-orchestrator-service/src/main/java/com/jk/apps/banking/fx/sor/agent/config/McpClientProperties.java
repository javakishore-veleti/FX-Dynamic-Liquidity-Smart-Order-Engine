package com.jk.apps.banking.fx.sor.agent.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "fx.mcp")
public class McpClientProperties {

    /**
     * Base URL for fx-topology-mcp-server (default localhost:8091).
     */
    private String baseUrl = "http://localhost:8091";

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }
}
