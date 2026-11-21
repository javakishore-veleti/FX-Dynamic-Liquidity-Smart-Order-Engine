package com.jk.apps.banking.fx.sor.topology.mcp.client;

import java.util.LinkedHashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import com.jk.apps.banking.fx.sor.topology.mcp.dto.McpToolDtos.LpVenueHaltRequestDto;
import com.jk.apps.banking.fx.sor.topology.mcp.dto.McpToolDtos.SkewedLoadSimRequestDto;
import com.jk.apps.banking.fx.sor.topology.mcp.dto.McpToolDtos.SyntheticSeedRequestDto;

@Component
public class AdminServiceClient {

    private static final Logger log = LoggerFactory.getLogger(AdminServiceClient.class);

    private final RestClient adminRestClient;

    public AdminServiceClient(@Qualifier("adminRestClient") RestClient adminRestClient) {
        this.adminRestClient = adminRestClient;
    }

    public ClientResult triggerSyntheticSeed(SyntheticSeedRequestDto request) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("numBatches", request.numBatches());
        payload.put("batchSize", request.batchSize());
        payload.put("datasets", request.datasets());
        return post("/api/v1/admin/synthetic/seed", payload);
    }

    public ClientResult triggerSkewedLoadSim(SkewedLoadSimRequestDto request) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("targetRps", request.targetRps());
        payload.put("durationSec", request.durationSec());
        payload.put("ccyPair", request.ccyPair());
        return post("/api/v1/admin/load-sim/skewed", payload);
    }

    public ClientResult proposeOrExecuteLpHalt(LpVenueHaltRequestDto request) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("lpId", request.lpId());
        payload.put("reason", request.reason());
        payload.put("dryRun", request.dryRun());
        if (request.approvalToken() != null && !request.approvalToken().isBlank()) {
            payload.put("approvalToken", request.approvalToken());
        }
        return post("/api/v1/admin/lp/halt", payload);
    }

    private ClientResult post(String path, Map<String, Object> payload) {
        try {
            Object body = adminRestClient.post()
                    .uri(path)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(payload)
                    .retrieve()
                    .body(Object.class);
            return ClientResult.ok(body);
        } catch (RestClientResponseException ex) {
            return ClientResult.fail(ex.getStatusCode().value(), safeMessage(ex));
        } catch (RestClientException ex) {
            log.warn("Admin call {} failed: {}", path, ex.getMessage());
            return ClientResult.fail(503, "admin-service unavailable: " + ex.getMessage());
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
