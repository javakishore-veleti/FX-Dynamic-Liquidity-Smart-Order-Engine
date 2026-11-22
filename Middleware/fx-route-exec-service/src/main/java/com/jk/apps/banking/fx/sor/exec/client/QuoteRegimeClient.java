package com.jk.apps.banking.fx.sor.exec.client;

import java.math.BigDecimal;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/** Zero-sharing HTTP client — route-exec resolves FR-01 regime from quote when request omits it. */
@Component
public class QuoteRegimeClient {

    private static final Logger log = LoggerFactory.getLogger(QuoteRegimeClient.class);

    private final RestClient quoteRestClient;

    public QuoteRegimeClient(@Value("${fx.quote.base-url:http://127.0.0.1:8081}") String quoteBaseUrl) {
        this.quoteRestClient = RestClient.builder().baseUrl(quoteBaseUrl).build();
    }

    public RegimeHint fetch(String clientTier, String ccyPair) {
        try {
            Map<String, Object> body = quoteRestClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/api/v1/fx/regime/context")
                            .queryParam("clientTier", clientTier == null ? "TIER_2" : clientTier)
                            .queryParam("ccyPair", ccyPair == null ? "EURUSD" : ccyPair)
                            .build())
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() {
                    });
            if (body == null) {
                return RegimeHint.fallback();
            }
            String regime = String.valueOf(body.getOrDefault("sessionRegime", "LONDON_NY_OVERLAP"));
            BigDecimal penalty = toDecimal(body.get("lastLookPenaltyWeight"), BigDecimal.ONE);
            return new RegimeHint(regime, penalty, true);
        } catch (Exception ex) {
            log.warn("FR-01 regime context unavailable from quote: {}", ex.getMessage());
            return RegimeHint.fallback();
        }
    }

    private static BigDecimal toDecimal(Object value, BigDecimal defaultValue) {
        if (value == null) {
            return defaultValue;
        }
        try {
            return new BigDecimal(String.valueOf(value));
        } catch (Exception e) {
            return defaultValue;
        }
    }

    public record RegimeHint(String sessionRegime, BigDecimal lastLookPenaltyWeight, boolean fromQuote) {
        static RegimeHint fallback() {
            return new RegimeHint("LONDON_NY_OVERLAP", BigDecimal.ONE, false);
        }
    }
}
