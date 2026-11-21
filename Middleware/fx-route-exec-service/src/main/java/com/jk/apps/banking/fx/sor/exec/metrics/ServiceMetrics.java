package com.jk.apps.banking.fx.sor.exec.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

@Component
public class ServiceMetrics {

    private final Counter ordersRouteExecute;
    private final Counter ordersFailover;
    private final Counter ordersInternalized;
    private final Counter auditEventsPublished;

    public ServiceMetrics(MeterRegistry registry) {
        this.ordersRouteExecute = Counter.builder("fx.route.execute.requests")
                .description("Route-execute requests")
                .tag("service", "fx-route-exec-service")
                .register(registry);
        this.ordersFailover = Counter.builder("fx.route.failover.requests")
                .description("Waterfall failover requests")
                .tag("service", "fx-route-exec-service")
                .register(registry);
        this.ordersInternalized = Counter.builder("fx.route.internalized")
                .description("Orders internalized vs principal book")
                .tag("service", "fx-route-exec-service")
                .register(registry);
        this.auditEventsPublished = Counter.builder("fx.route.audit.published")
                .description("Async provenance audit events")
                .tag("service", "fx-route-exec-service")
                .register(registry);
    }

    public Counter ordersRouteExecute() {
        return ordersRouteExecute;
    }
    public Counter ordersFailover() {
        return ordersFailover;
    }
    public Counter ordersInternalized() {
        return ordersInternalized;
    }
    public Counter auditEventsPublished() {
        return auditEventsPublished;
    }
}
