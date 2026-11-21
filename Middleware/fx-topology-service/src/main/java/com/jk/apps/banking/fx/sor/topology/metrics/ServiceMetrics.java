package com.jk.apps.banking.fx.sor.topology.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Component;

@Component
public class ServiceMetrics {

    private final Counter networkGraphQueries;
    private final Counter ipcGraphQueries;
    private final Counter outlierTraceQueries;
    private final Counter blastRadiusQueries;
    private final Counter topologyDiffQueries;
    private final Timer queryLatency;

    public ServiceMetrics(MeterRegistry registry) {
        this.networkGraphQueries = Counter.builder("fx.topology.graph.network.queries")
                .description("Network graph queries served")
                .tag("service", "fx-topology-service")
                .register(registry);
        this.ipcGraphQueries = Counter.builder("fx.topology.graph.ipc.queries")
                .description("IPC graph queries served")
                .tag("service", "fx-topology-service")
                .register(registry);
        this.outlierTraceQueries = Counter.builder("fx.topology.traces.outlier.queries")
                .description("Trace outlier queries served")
                .tag("service", "fx-topology-service")
                .register(registry);
        this.blastRadiusQueries = Counter.builder("fx.topology.blast.radius.queries")
                .description("Blast-radius calculations served")
                .tag("service", "fx-topology-service")
                .register(registry);
        this.topologyDiffQueries = Counter.builder("fx.topology.diff.queries")
                .description("Topology snapshot diffs served")
                .tag("service", "fx-topology-service")
                .register(registry);
        this.queryLatency = Timer.builder("fx.topology.query.latency")
                .description("Topology query latency")
                .tag("service", "fx-topology-service")
                .register(registry);
    }

    public Counter networkGraphQueries() {
        return networkGraphQueries;
    }

    public Counter ipcGraphQueries() {
        return ipcGraphQueries;
    }

    public Counter outlierTraceQueries() {
        return outlierTraceQueries;
    }

    public Counter blastRadiusQueries() {
        return blastRadiusQueries;
    }

    public Counter topologyDiffQueries() {
        return topologyDiffQueries;
    }

    public Timer queryLatency() {
        return queryLatency;
    }
}
