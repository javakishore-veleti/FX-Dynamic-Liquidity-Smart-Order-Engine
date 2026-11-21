package com.jk.apps.banking.fx.sor.agent.client;

import com.jk.apps.banking.fx.sor.agent.metrics.ServiceMetrics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Thin RestClient adapter over fx-topology-mcp-server (:8091) /mcp/tools/* surface.
 */
@Component
public class TopologyMcpClient {

    private static final Logger log = LoggerFactory.getLogger(TopologyMcpClient.class);

    private final RestClient mcpRestClient;
    private final ServiceMetrics metrics;

    public TopologyMcpClient(RestClient mcpRestClient, ServiceMetrics metrics) {
        this.mcpRestClient = mcpRestClient;
        this.metrics = metrics;
    }

    public Map<String, Object> invokeTool(String toolName, Map<String, Object> payload) {
        metrics.mcpToolCalls().increment();
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> body = mcpRestClient.post()
                    .uri("/mcp/tools/{tool}", toolName)
                    .body(payload == null ? Map.of() : payload)
                    .retrieve()
                    .body(Map.class);
            Map<String, Object> result = new LinkedHashMap<>();
            result.put("tool", toolName);
            result.put("ok", true);
            result.put("response", body == null ? Map.of() : body);
            return result;
        } catch (RestClientException ex) {
            metrics.mcpToolCallFailures().increment();
            log.warn("MCP tool {} failed: {}", toolName, ex.getMessage());
            Map<String, Object> result = new LinkedHashMap<>();
            result.put("tool", toolName);
            result.put("ok", false);
            result.put("error", ex.getMessage());
            return result;
        }
    }
}
