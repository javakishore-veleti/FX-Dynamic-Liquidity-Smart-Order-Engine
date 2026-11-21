package com.jk.apps.banking.fx.sor.topology.mcp.client;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import com.jk.apps.banking.fx.sor.topology.mcp.dto.McpToolDtos.BlastRadiusRequestDto;
import com.jk.apps.banking.fx.sor.topology.mcp.dto.McpToolDtos.ServiceDependencyQueryDto;
import com.jk.apps.banking.fx.sor.topology.mcp.dto.McpToolDtos.TopologyDiffRequestDto;
import com.jk.apps.banking.fx.sor.topology.mcp.dto.McpToolDtos.TraceOutlierQueryDto;

@Component
public class TopologyServiceClient {

    private static final Logger log = LoggerFactory.getLogger(TopologyServiceClient.class);

    private final RestClient topologyRestClient;

    public TopologyServiceClient(@Qualifier("topologyRestClient") RestClient topologyRestClient) {
        this.topologyRestClient = topologyRestClient;
    }

    public ClientResult getServiceDependencies(ServiceDependencyQueryDto query) {
        String layer = query.layer() == null ? "network" : query.layer().toLowerCase();
        String path = switch (layer) {
            case "ipc" -> "/api/v1/topology/graph/ipc";
            case "trace", "traces" -> "/api/v1/topology/traces/outliers";
            default -> "/api/v1/topology/graph/network";
        };
        try {
            Object body = topologyRestClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path(path)
                            .queryParam("service", query.serviceName())
                            .queryParam("depth", query.depth())
                            .build())
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .body(Object.class);
            return ClientResult.ok(body);
        } catch (RestClientResponseException ex) {
            return ClientResult.fail(ex.getStatusCode().value(), safeMessage(ex));
        } catch (RestClientException ex) {
            log.warn("Topology dependency query failed: {}", ex.getMessage());
            return ClientResult.fail(503, "topology-service unavailable: " + ex.getMessage());
        }
    }

    public ClientResult calculateBlastRadius(BlastRadiusRequestDto request) {
        try {
            Object body = topologyRestClient.post()
                    .uri("/api/v1/topology/blast-radius")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of(
                            "serviceName", request.serviceName(),
                            "ccyPair", request.ccyPair() == null ? "" : request.ccyPair()))
                    .retrieve()
                    .body(Object.class);
            return ClientResult.ok(body);
        } catch (RestClientResponseException ex) {
            return ClientResult.fail(ex.getStatusCode().value(), safeMessage(ex));
        } catch (RestClientException ex) {
            log.warn("Blast-radius call failed: {}", ex.getMessage());
            return ClientResult.fail(503, "topology-service unavailable: " + ex.getMessage());
        }
    }

    public ClientResult diffSnapshots(TopologyDiffRequestDto request) {
        try {
            Object body = topologyRestClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/api/v1/topology/diff")
                            .queryParam("from", request.fromTs())
                            .queryParam("to", request.toTs())
                            .build())
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .body(Object.class);
            return ClientResult.ok(body);
        } catch (RestClientResponseException ex) {
            return ClientResult.fail(ex.getStatusCode().value(), safeMessage(ex));
        } catch (RestClientException ex) {
            log.warn("Topology diff call failed: {}", ex.getMessage());
            return ClientResult.fail(503, "topology-service unavailable: " + ex.getMessage());
        }
    }

    public ClientResult queryOutlierTraces(TraceOutlierQueryDto query) {
        try {
            Object body = topologyRestClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/api/v1/topology/traces/outliers")
                            .queryParam("service", query.serviceName())
                            .queryParam("minLatencyMs", query.minLatencyMs())
                            .queryParam("limit", query.limit())
                            .build())
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .body(Object.class);
            return ClientResult.ok(body);
        } catch (RestClientResponseException ex) {
            return ClientResult.fail(ex.getStatusCode().value(), safeMessage(ex));
        } catch (RestClientException ex) {
            log.warn("Outlier traces call failed: {}", ex.getMessage());
            return ClientResult.fail(503, "topology-service unavailable: " + ex.getMessage());
        }
    }

    private static String safeMessage(RestClientResponseException ex) {
        String body = ex.getResponseBodyAsString();
        if (body == null || body.isBlank()) {
            return "upstream HTTP " + ex.getStatusCode().value();
        }
        return body.length() > 500 ? body.substring(0, 500) : body;
    }

    public record ClientResult(boolean success, Object data, Integer status, String error) {
        public static ClientResult ok(Object data) {
            return new ClientResult(true, data, 200, null);
        }

        public static ClientResult fail(int status, String error) {
            return new ClientResult(false, null, status, error);
        }
    }
}
