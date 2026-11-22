package com.jk.apps.banking.fx.sor.admin.service;

import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/** OPEN — admin → qualify HTTP only (settlement policies + credit lines). */
@Component
public class QualifyReferenceClient {

    private final RestClient qualifyRestClient;

    public QualifyReferenceClient(@Qualifier("qualifyRestClient") RestClient qualifyRestClient) {
        this.qualifyRestClient = qualifyRestClient;
    }

    public List<Map<String, Object>> listSettlementPolicies() {
        return qualifyRestClient.get()
                .uri("/api/v1/fx/reference/settlement-policies")
                .retrieve()
                .body(new ParameterizedTypeReference<>() {
                });
    }

    public Map<String, Object> loadSettlementPolicies() {
        return qualifyRestClient.post()
                .uri("/api/v1/fx/reference/settlement-policies/load")
                .retrieve()
                .body(new ParameterizedTypeReference<>() {
                });
    }

    public Map<String, Object> upsertSettlementPolicy(Map<String, Object> body) {
        return qualifyRestClient.put()
                .uri("/api/v1/fx/reference/settlement-policies")
                .body(body)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {
                });
    }

    public void deleteSettlementPolicy(String ccyPair, String tenor) {
        qualifyRestClient.method(HttpMethod.DELETE)
                .uri("/api/v1/fx/reference/settlement-policies/{ccyPair}/{tenor}", ccyPair, tenor)
                .retrieve()
                .toBodilessEntity();
    }

    public List<Map<String, Object>> listCreditLines() {
        return qualifyRestClient.get()
                .uri("/api/v1/fx/reference/credit-lines")
                .retrieve()
                .body(new ParameterizedTypeReference<>() {
                });
    }

    public Map<String, Object> upsertCreditLine(Map<String, Object> body) {
        return qualifyRestClient.put()
                .uri("/api/v1/fx/reference/credit-lines")
                .body(body)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {
                });
    }

    public void deleteCreditLine(String clientId, String lpId) {
        qualifyRestClient.method(HttpMethod.DELETE)
                .uri("/api/v1/fx/reference/credit-lines/{clientId}/{lpId}", clientId, lpId)
                .retrieve()
                .toBodilessEntity();
    }
}
