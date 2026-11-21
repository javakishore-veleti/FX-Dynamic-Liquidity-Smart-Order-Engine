package com.jk.apps.banking.fx.sor.topology.eventing;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;

/**
 * Cluster-mode publisher: JSON payload to Kafka. Avro is stubbed via schema-name header.
 */
public class KafkaJsonDomainEventPublisher implements DomainEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(KafkaJsonDomainEventPublisher.class);

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public KafkaJsonDomainEventPublisher(KafkaTemplate<String, String> kafkaTemplate, ObjectMapper objectMapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public void publish(String topic, Map<String, Object> payload) {
        try {
            String json = objectMapper.writeValueAsString(payload);
            ProducerRecord<String, String> record = new ProducerRecord<>(topic, json);
            // Stub Avro schema identity for downstream consumers / schema registry wiring
            record.headers().add(new RecordHeader(
                    "avro.schema.name",
                    topic.getBytes(StandardCharsets.UTF_8)));
            record.headers().add(new RecordHeader(
                    "content-type",
                    "application/json".getBytes(StandardCharsets.UTF_8)));
            Object eventId = payload.get("eventId");
            kafkaTemplate.send(record);
            log.debug("domain_event_published topic={} eventId={}", topic, eventId);
        } catch (JsonProcessingException e) {
            log.warn("Failed to serialize domain event topic={}: {}", topic, e.getMessage());
        } catch (Exception e) {
            log.warn("Failed to publish domain event topic={}: {}", topic, e.getMessage());
        }
    }
}
