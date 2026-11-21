package com.jk.apps.banking.fx.sor.topology.mcp.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Component;

@Component
public class ServiceMetrics {

    private final Counter toolInvocations;
    private final Counter toolFailures;
    private final Timer toolLatency;

    public ServiceMetrics(MeterRegistry registry) {
        this.toolInvocations = Counter.builder("fx.topology.mcp.tool.invocations")
                .description("MCP tool invocations")
                .tag("service", "fx-topology-mcp-server")
                .register(registry);
        this.toolFailures = Counter.builder("fx.topology.mcp.tool.failures")
                .description("MCP tool invocation failures")
                .tag("service", "fx-topology-mcp-server")
                .register(registry);
        this.toolLatency = Timer.builder("fx.topology.mcp.tool.latency")
                .description("MCP tool invocation latency")
                .tag("service", "fx-topology-mcp-server")
                .register(registry);
    }

    public Counter toolInvocations() {
        return toolInvocations;
    }

    public Counter toolFailures() {
        return toolFailures;
    }

    public Timer toolLatency() {
        return toolLatency;
    }
}
