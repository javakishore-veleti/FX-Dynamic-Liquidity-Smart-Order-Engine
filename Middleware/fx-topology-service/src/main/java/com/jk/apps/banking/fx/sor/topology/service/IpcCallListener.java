package com.jk.apps.banking.fx.sor.topology.service;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/** Kafka consumer for IPC call messages. Only active when fx.features.kafka-enabled is true. */
@Component
@ConditionalOnProperty(name = "fx.features.kafka-enabled", havingValue = "true")
public class IpcCallListener {

    private final IpcCallIngest ingest;

    public IpcCallListener(IpcCallIngest ingest) {
        this.ingest = ingest;
    }

    @KafkaListener(topics = "${fx.topology.ipc-topic:fx.topology.ipc-call.v1}")
    public void onMessage(String json) {
        ingest.accept(json);
    }
}
