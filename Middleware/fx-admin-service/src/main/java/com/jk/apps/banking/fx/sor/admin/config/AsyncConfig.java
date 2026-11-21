package com.jk.apps.banking.fx.sor.admin.config;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AsyncConfig {

    @Bean(destroyMethod = "close")
    public ExecutorService workflowExecutor() {
        return Executors.newVirtualThreadPerTaskExecutor();
    }
}
