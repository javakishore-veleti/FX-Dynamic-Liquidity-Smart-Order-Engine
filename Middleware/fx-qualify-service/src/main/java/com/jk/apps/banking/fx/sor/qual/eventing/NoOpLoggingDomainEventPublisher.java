package com.jk.apps.banking.fx.sor.qual.eventing;

import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Local-dev publisher: structured DEBUG log only; no Kafka sockets.
 */
public class NoOpLoggingDomainEventPublisher implements DomainEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(NoOpLoggingDomainEventPublisher.class);

    @Override
    public void publish(String topic, Map<String, Object> payload) {
        log.debug("domain_event topic={} payload={}", topic, payload);
    }
}
