package com.jk.apps.banking.fx.sor.admin.service;

import java.util.List;
import java.util.Map;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/**
 * OPEN integration adapter — admin talks to quote via HTTP only (zero-sharing).
 */
@Component
public class QuoteScheduleClient {

    private final RestClient quoteRestClient;

    public QuoteScheduleClient(RestClient quoteRestClient) {
        this.quoteRestClient = quoteRestClient;
    }

    public List<Map<String, Object>> listSchedules() {
        return quoteRestClient.get()
                .uri("/api/v1/fx/spread-schedules")
                .retrieve()
                .body(new ParameterizedTypeReference<>() {
                });
    }

    public Map<String, Object> draft(Map<String, Object> body) {
        return quoteRestClient.post()
                .uri("/api/v1/fx/spread-schedules/draft")
                .body(body)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {
                });
    }

    public Map<String, Object> submit(Map<String, Object> body) {
        return quoteRestClient.post()
                .uri("/api/v1/fx/spread-schedules/submit")
                .body(body)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {
                });
    }

    public Map<String, Object> approve(Map<String, Object> body) {
        return quoteRestClient.post()
                .uri("/api/v1/fx/spread-schedules/approve")
                .body(body)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {
                });
    }

    public void invalidateScheduleCache() {
        quoteRestClient.post()
                .uri("/api/v1/fx/spread-schedules/cache/invalidate")
                .retrieve()
                .toBodilessEntity();
    }

    public Map<String, Object> putMacroFlag(Map<String, Object> body) {
        return quoteRestClient.put()
                .uri("/api/v1/fx/macro-flag")
                .body(body == null ? Map.of() : body)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {
                });
    }

    public Map<String, Object> clearMacroFlag() {
        return quoteRestClient.method(org.springframework.http.HttpMethod.DELETE)
                .uri("/api/v1/fx/macro-flag")
                .retrieve()
                .body(new ParameterizedTypeReference<>() {
                });
    }
}
