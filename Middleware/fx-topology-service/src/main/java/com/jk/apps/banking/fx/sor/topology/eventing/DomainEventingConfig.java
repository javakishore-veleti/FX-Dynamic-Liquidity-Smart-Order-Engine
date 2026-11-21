package com.jk.apps.banking.fx.sor.topology.eventing;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jk.apps.banking.fx.sor.topology.config.AppEventsProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;

@Configuration
@EnableConfigurationProperties(AppEventsProperties.class)
public class DomainEventingConfig {

    @Bean
    @ConditionalOnProperty(prefix = "app.events", name = "broker-enabled", havingValue = "false", matchIfMissing = true)
    DomainEventPublisher noOpLoggingDomainEventPublisher() {
        return new NoOpLoggingDomainEventPublisher();
    }

    @Bean
    @ConditionalOnProperty(prefix = "app.events", name = "broker-enabled", havingValue = "true")
    @ConditionalOnBean(KafkaTemplate.class)
    DomainEventPublisher kafkaJsonDomainEventPublisher(
            KafkaTemplate<String, String> kafkaTemplate, ObjectMapper objectMapper) {
        return new KafkaJsonDomainEventPublisher(kafkaTemplate, objectMapper);
    }

    /**
     * Safety net when broker-enabled=true but KafkaTemplate is not on the context
     * (e.g. KafkaAutoConfiguration still excluded). Prefer fixing the profile exclude.
     */
    @Bean
    @ConditionalOnProperty(prefix = "app.events", name = "broker-enabled", havingValue = "true")
    @ConditionalOnMissingBean(DomainEventPublisher.class)
    DomainEventPublisher brokerEnabledFallbackPublisher() {
        return new NoOpLoggingDomainEventPublisher();
    }
}
