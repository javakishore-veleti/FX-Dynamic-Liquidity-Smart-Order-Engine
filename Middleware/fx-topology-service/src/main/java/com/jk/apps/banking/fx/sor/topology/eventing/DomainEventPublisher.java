package com.jk.apps.banking.fx.sor.topology.eventing;

import java.util.Map;

public interface DomainEventPublisher {

    void publish(String topic, Map<String, Object> payload);
}
