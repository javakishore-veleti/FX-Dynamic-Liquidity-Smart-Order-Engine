package com.jk.apps.banking.fx.sor.topology.service;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;

/** Kafka consumer for flow-hop events. Only active when fx.features.kafka-enabled is true. */
@Component
@ConditionalOnProperty(name = "fx.features.kafka-enabled", havingValue = "true")
public class FlowHopListener {

    private final FlowHopIngest ingest;

    public FlowHopListener(FlowHopIngest ingest) {
        this.ingest = ingest;
    }

    @KafkaListener(topics = "${fx.topology.flow-topic:fx.topology.flow-hop.v1}")
    public void onMessage(String json) throws InterruptedException {
        ingest.accept(json);
    }

    @Scheduled(fixedDelayString = "${fx.topology.drain-ms:5000}")
    public void drain() {
        ingest.drainOnce(Instant.now());
    }
}
