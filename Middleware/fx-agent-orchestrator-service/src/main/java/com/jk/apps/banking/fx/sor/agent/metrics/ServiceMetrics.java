package com.jk.apps.banking.fx.sor.agent.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

@Component
public class ServiceMetrics {

    private final Counter agentRunsAccepted;
    private final Counter agentRunsCompleted;
    private final Counter agentRunsFailed;
    private final Counter mcpToolCalls;
    private final Counter mcpToolCallFailures;

    public ServiceMetrics(MeterRegistry registry) {
        this.agentRunsAccepted = Counter.builder("fx.agent.runs.accepted")
                .description("Agent runs accepted (HTTP 202)")
                .tag("service", "fx-agent-orchestrator-service")
                .register(registry);
        this.agentRunsCompleted = Counter.builder("fx.agent.runs.completed")
                .description("Agent runs completed successfully")
                .tag("service", "fx-agent-orchestrator-service")
                .register(registry);
        this.agentRunsFailed = Counter.builder("fx.agent.runs.failed")
                .description("Agent runs failed")
                .tag("service", "fx-agent-orchestrator-service")
                .register(registry);
        this.mcpToolCalls = Counter.builder("fx.agent.mcp.tool.calls")
                .description("MCP tool invocations from orchestrator")
                .tag("service", "fx-agent-orchestrator-service")
                .register(registry);
        this.mcpToolCallFailures = Counter.builder("fx.agent.mcp.tool.failures")
                .description("MCP tool invocation failures")
                .tag("service", "fx-agent-orchestrator-service")
                .register(registry);
    }

    public Counter agentRunsAccepted() {
        return agentRunsAccepted;
    }

    public Counter agentRunsCompleted() {
        return agentRunsCompleted;
    }

    public Counter agentRunsFailed() {
        return agentRunsFailed;
    }

    public Counter mcpToolCalls() {
        return mcpToolCalls;
    }

    public Counter mcpToolCallFailures() {
        return mcpToolCallFailures;
    }
}
