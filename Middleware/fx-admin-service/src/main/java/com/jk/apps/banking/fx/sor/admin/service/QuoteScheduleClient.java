package com.jk.apps.banking.fx.sor.admin.service;

import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/**
 * OPEN integration adapter — admin talks to quote / route-exec via HTTP only (zero-sharing).
 */
@Component
public class QuoteScheduleClient {

    private final RestClient quoteRestClient;
    private final RestClient routeExecRestClient;

    public QuoteScheduleClient(
            @Qualifier("quoteRestClient") RestClient quoteRestClient,
            @Qualifier("routeExecRestClient") RestClient routeExecRestClient) {
        this.quoteRestClient = quoteRestClient;
        this.routeExecRestClient = routeExecRestClient;
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

    public Map<String, Object> getMacroFlag() {
        return quoteRestClient.get()
                .uri("/api/v1/fx/macro-flag")
                .retrieve()
                .body(new ParameterizedTypeReference<>() {
                });
    }

    public Map<String, Object> regimeContext(String clientTier, String ccyPair) {
        return quoteRestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/v1/fx/regime/context")
                        .queryParam("clientTier", clientTier)
                        .queryParam("ccyPair", ccyPair)
                        .build())
                .retrieve()
                .body(new ParameterizedTypeReference<>() {
                });
    }

    public List<Map<String, Object>> listEconomicCalendar() {
        return quoteRestClient.get()
                .uri("/api/v1/fx/economic-calendar")
                .retrieve()
                .body(new ParameterizedTypeReference<>() {
                });
    }

    public Map<String, Object> upsertEconomicCalendar(Map<String, Object> body) {
        return quoteRestClient.put()
                .uri("/api/v1/fx/economic-calendar")
                .body(body)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {
                });
    }

    public void deleteEconomicCalendar(String source, String start) {
        quoteRestClient.method(org.springframework.http.HttpMethod.DELETE)
                .uri(uriBuilder -> uriBuilder
                        .path("/api/v1/fx/economic-calendar/{source}")
                        .queryParam("start", start)
                        .build(source))
                .retrieve()
                .toBodilessEntity();
    }

    public void deleteSchedule(String tierId, String ccyPair, String sessionRegime) {
        quoteRestClient.method(org.springframework.http.HttpMethod.DELETE)
                .uri("/api/v1/fx/spread-schedules/{tierId}/{ccyPair}/{sessionRegime}", tierId, ccyPair, sessionRegime)
                .retrieve()
                .toBodilessEntity();
    }

    public List<Map<String, Object>> listLpBook() {
        return quoteRestClient.get()
                .uri("/api/v1/fx/lp-book")
                .retrieve()
                .body(new ParameterizedTypeReference<>() {
                });
    }

    public Map<String, Object> upsertLpBook(Map<String, Object> body) {
        return quoteRestClient.put()
                .uri("/api/v1/fx/lp-book")
                .body(body)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {
                });
    }

    public void deleteLpBook(String ccyPair, String lpId) {
        quoteRestClient.method(org.springframework.http.HttpMethod.DELETE)
                .uri("/api/v1/fx/lp-book/{ccyPair}/{lpId}", ccyPair, lpId)
                .retrieve()
                .toBodilessEntity();
    }

    public List<Map<String, Object>> listLpRankings() {
        return routeExecRestClient.get()
                .uri("/api/v1/fx/reference/lp-rankings")
                .retrieve()
                .body(new ParameterizedTypeReference<>() {
                });
    }

    public Map<String, Object> upsertLpRanking(Map<String, Object> body) {
        return routeExecRestClient.put()
                .uri("/api/v1/fx/reference/lp-rankings")
                .body(body)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {
                });
    }

    public void deleteLpRanking(String lpId) {
        routeExecRestClient.method(org.springframework.http.HttpMethod.DELETE)
                .uri("/api/v1/fx/reference/lp-rankings/{lpId}", lpId)
                .retrieve()
                .toBodilessEntity();
    }

    public Map<String, Object> indicative(Map<String, Object> body) {
        return quoteRestClient.post()
                .uri("/api/v1/fx/quotes/indicative")
                .body(body)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {
                });
    }

    public Map<String, Object> routeExecute(Map<String, Object> body) {
        return routeExecRestClient.post()
                .uri("/api/v1/fx/orders/route-execute")
                .body(body)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {
                });
    }
}
