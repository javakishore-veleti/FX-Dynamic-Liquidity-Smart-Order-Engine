package com.jk.apps.banking.fx.sor.exec.eventing;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jk.apps.banking.fx.sor.exec.regime.LastLookPenaltyHintCache;
import java.math.BigDecimal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix = "fx.features", name = "kafka-enabled", havingValue = "true")
public class LastLookPenaltyHintListener {

    private static final Logger log = LoggerFactory.getLogger(LastLookPenaltyHintListener.class);

    private final LastLookPenaltyHintCache cache;
    private final ObjectMapper objectMapper;

    public LastLookPenaltyHintListener(LastLookPenaltyHintCache cache, ObjectMapper objectMapper) {
        this.cache = cache;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = "fx.quote.last-look-penalty-hint.v1", groupId = "fx-route-exec-fr01")
    public void onHint(String payload) {
        try {
            JsonNode node = objectMapper.readTree(payload);
            String regime = node.path("sessionRegime").asText("LONDON_NY_OVERLAP");
            BigDecimal weight = node.path("lastLookPenaltyWeight").decimalValue();
            cache.update(regime, weight);
            log.info("FR-01 last-look hint cached regime={} weight={}", regime, weight);
        } catch (Exception ex) {
            log.warn("Failed to parse last-look hint: {}", ex.getMessage());
        }
    }
}
