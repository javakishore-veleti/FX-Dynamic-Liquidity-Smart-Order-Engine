package com.jk.apps.banking.fx.sor.qual.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

@Component
public class ServiceMetrics {

    private final Counter ordersQualifyRequests;
    private final Counter venuesKnockedOut;
    private final Counter creditReservations;

    public ServiceMetrics(MeterRegistry registry) {
        this.ordersQualifyRequests = Counter.builder("fx.qualify.orders.requests")
                .description("Order qualification requests")
                .tag("service", "fx-qualify-service")
                .register(registry);
        this.venuesKnockedOut = Counter.builder("fx.qualify.venues.knocked_out")
                .description("Venues knocked out by policy")
                .tag("service", "fx-qualify-service")
                .register(registry);
        this.creditReservations = Counter.builder("fx.qualify.credit.reservations")
                .description("Bilateral credit reservations")
                .tag("service", "fx-qualify-service")
                .register(registry);
    }

    public Counter ordersQualifyRequests() {
        return ordersQualifyRequests;
    }
    public Counter venuesKnockedOut() {
        return venuesKnockedOut;
    }
    public Counter creditReservations() {
        return creditReservations;
    }
}
