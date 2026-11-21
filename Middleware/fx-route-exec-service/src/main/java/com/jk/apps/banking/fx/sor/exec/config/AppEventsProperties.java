package com.jk.apps.banking.fx.sor.exec.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.events")
public class AppEventsProperties {

    /**
     * When false (default), domain events are DEBUG-logged only (no Kafka).
     * Enable via local-kafka / load-kafka profiles for cluster mode.
     */
    private boolean brokerEnabled = false;

    public boolean isBrokerEnabled() {
        return brokerEnabled;
    }

    public void setBrokerEnabled(boolean brokerEnabled) {
        this.brokerEnabled = brokerEnabled;
    }
}
