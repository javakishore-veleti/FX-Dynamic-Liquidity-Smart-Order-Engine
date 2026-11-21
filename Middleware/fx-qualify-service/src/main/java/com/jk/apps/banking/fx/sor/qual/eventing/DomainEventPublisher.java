package com.jk.apps.banking.fx.sor.qual.eventing;

import java.util.Map;

public interface DomainEventPublisher {

    void publish(String topic, Map<String, Object> payload);
}
