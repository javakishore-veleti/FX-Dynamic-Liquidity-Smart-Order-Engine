package com.jk.apps.banking.fx.sor.agent.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "fx.features")
public class FxFeatureProperties {

    private boolean redisEnabled;
    private boolean kafkaEnabled;
    private boolean liquibaseOnStartup = true;

    public boolean isRedisEnabled() {
        return redisEnabled;
    }

    public void setRedisEnabled(boolean redisEnabled) {
        this.redisEnabled = redisEnabled;
    }

    public boolean isKafkaEnabled() {
        return kafkaEnabled;
    }

    public void setKafkaEnabled(boolean kafkaEnabled) {
        this.kafkaEnabled = kafkaEnabled;
    }

    public boolean isLiquibaseOnStartup() {
        return liquibaseOnStartup;
    }

    public void setLiquibaseOnStartup(boolean liquibaseOnStartup) {
        this.liquibaseOnStartup = liquibaseOnStartup;
    }
}
