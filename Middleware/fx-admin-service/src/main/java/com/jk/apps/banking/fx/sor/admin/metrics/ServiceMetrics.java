package com.jk.apps.banking.fx.sor.admin.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

@Component
public class ServiceMetrics {

    private final Counter syntheticJobsQueued;
    private final Counter loadSimJobsQueued;
    private final Counter workflowsCancelled;

    public ServiceMetrics(MeterRegistry registry) {
        this.syntheticJobsQueued = Counter.builder("fx.admin.synthetic.jobs.queued")
                .description("Synthetic dataset jobs queued")
                .tag("service", "fx-admin-service")
                .register(registry);
        this.loadSimJobsQueued = Counter.builder("fx.admin.loadsim.jobs.queued")
                .description("Load simulation jobs queued")
                .tag("service", "fx-admin-service")
                .register(registry);
        this.workflowsCancelled = Counter.builder("fx.admin.workflows.cancelled")
                .description("Workflow cancellations")
                .tag("service", "fx-admin-service")
                .register(registry);
    }

    public Counter syntheticJobsQueued() {
        return syntheticJobsQueued;
    }
    public Counter loadSimJobsQueued() {
        return loadSimJobsQueued;
    }
    public Counter workflowsCancelled() {
        return workflowsCancelled;
    }
}
