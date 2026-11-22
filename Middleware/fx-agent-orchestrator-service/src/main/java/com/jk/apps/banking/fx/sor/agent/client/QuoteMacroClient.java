package com.jk.apps.banking.fx.sor.agent.client;

import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

/** HTTP-only arm of quote MACRO_NEWS when MacroGuard is not dry-run. */
@Component
public class QuoteMacroClient {

    private static final Logger log = LoggerFactory.getLogger(QuoteMacroClient.class);

    private final RestClient quoteRestClient;

    public QuoteMacroClient(@Value("${fx.quote.base-url:http://127.0.0.1:8081}") String quoteBaseUrl) {
        this.quoteRestClient = RestClient.builder().baseUrl(quoteBaseUrl).build();
    }

    public Map<String, Object> armMacro(String source, double severity, long ttlSeconds) {
        try {
            return quoteRestClient.put()
                    .uri("/api/v1/fx/macro-flag")
                    .body(Map.of(
                            "source", source == null ? "AGENT" : source,
                            "severity", severity,
                            "ttlSeconds", ttlSeconds,
                            "armedBy", "AGENT"))
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() {
                    });
        } catch (RestClientException ex) {
            log.warn("MacroGuard → quote macro-flag failed: {}", ex.getMessage());
            return Map.of("ok", false, "error", ex.getMessage());
        }
    }
}
