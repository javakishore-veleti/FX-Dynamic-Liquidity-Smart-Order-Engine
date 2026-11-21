package com.jk.apps.banking.fx.sor.quote.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

@Component
public class ServiceMetrics {

    private final Counter quotesIndicativeRequests;
    private final Counter quotesStreamSubscriptions;
    private final Counter quotesStaleFiltered;

    public ServiceMetrics(MeterRegistry registry) {
        this.quotesIndicativeRequests = Counter.builder("fx.quote.indicative.requests")
                .description("Indicative quote requests")
                .tag("service", "fx-quote-service")
                .register(registry);
        this.quotesStreamSubscriptions = Counter.builder("fx.quote.stream.subscriptions")
                .description("Quote stream subscriptions")
                .tag("service", "fx-quote-service")
                .register(registry);
        this.quotesStaleFiltered = Counter.builder("fx.quote.stale.filtered")
                .description("Stale quote ticks filtered")
                .tag("service", "fx-quote-service")
                .register(registry);
    }

    public Counter quotesIndicativeRequests() {
        return quotesIndicativeRequests;
    }
    public Counter quotesStreamSubscriptions() {
        return quotesStreamSubscriptions;
    }
    public Counter quotesStaleFiltered() {
        return quotesStaleFiltered;
    }
}
