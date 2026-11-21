package com.jk.apps.banking.fx.sor.topology.mcp.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "fx.clients")
public class McpClientProperties {

    private String topologyBaseUrl = "http://localhost:8085";
    private String adminBaseUrl = "http://localhost:8084";
    private int connectTimeoutMs = 2000;
    private int readTimeoutMs = 5000;

    public String getTopologyBaseUrl() {
        return topologyBaseUrl;
    }

    public void setTopologyBaseUrl(String topologyBaseUrl) {
        this.topologyBaseUrl = topologyBaseUrl;
    }

    public String getAdminBaseUrl() {
        return adminBaseUrl;
    }

    public void setAdminBaseUrl(String adminBaseUrl) {
        this.adminBaseUrl = adminBaseUrl;
    }

    public int getConnectTimeoutMs() {
        return connectTimeoutMs;
    }

    public void setConnectTimeoutMs(int connectTimeoutMs) {
        this.connectTimeoutMs = connectTimeoutMs;
    }

    public int getReadTimeoutMs() {
        return readTimeoutMs;
    }

    public void setReadTimeoutMs(int readTimeoutMs) {
        this.readTimeoutMs = readTimeoutMs;
    }
}
