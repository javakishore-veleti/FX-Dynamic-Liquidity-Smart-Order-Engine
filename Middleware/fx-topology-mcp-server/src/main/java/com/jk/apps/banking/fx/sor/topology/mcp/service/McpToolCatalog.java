package com.jk.apps.banking.fx.sor.topology.mcp.service;

import java.util.List;

import com.jk.apps.banking.fx.sor.topology.mcp.dto.McpToolDtos.McpCatalogDto;
import com.jk.apps.banking.fx.sor.topology.mcp.dto.McpToolDtos.McpToolDescriptorDto;

public final class McpToolCatalog {

    public static final String SAFETY_READ_ONLY = "READ_ONLY";
    public static final String SAFETY_CONTROLLED_WRITE = "CONTROLLED_WRITE";
    public static final String SAFETY_HIGH_RISK = "HIGH_RISK_MUTATION";

    private McpToolCatalog() {
    }

    public static McpCatalogDto catalog() {
        return new McpCatalogDto(
                "fx-topology-mcp-server",
                "streamable-http",
                List.of(
                        tool("get_service_dependencies",
                                "Resolve service dependencies across topology layers",
                                SAFETY_READ_ONLY, "fx-topology-service",
                                "/mcp/tools/get_service_dependencies"),
                        tool("calculate_blast_radius",
                                "Compute downstream blast radius for a service/ccyPair",
                                SAFETY_READ_ONLY, "fx-topology-service",
                                "/mcp/tools/calculate_blast_radius"),
                        tool("diff_topology_snapshots",
                                "Time-travel diff between topology snapshots",
                                SAFETY_READ_ONLY, "fx-topology-service",
                                "/mcp/tools/diff_topology_snapshots"),
                        tool("query_iceberg_outlier_traces",
                                "Query Iceberg outlier traces above latency threshold",
                                SAFETY_READ_ONLY, "fx-topology-service",
                                "/mcp/tools/query_iceberg_outlier_traces"),
                        tool("get_kafka_consumer_lag",
                                "Read Kafka consumer-group lag (Prometheus/Kafka Admin)",
                                SAFETY_READ_ONLY, "prometheus-kafka",
                                "/mcp/tools/get_kafka_consumer_lag"),
                        tool("trigger_synthetic_seed",
                                "Queue synthetic dataset seed via fx-admin-service (non-prod)",
                                SAFETY_CONTROLLED_WRITE, "fx-admin-service",
                                "/mcp/tools/trigger_synthetic_seed"),
                        tool("trigger_skewed_load_sim",
                                "Queue skewed hot-partition load simulation (non-prod)",
                                SAFETY_CONTROLLED_WRITE, "fx-admin-service",
                                "/mcp/tools/trigger_skewed_load_sim"),
                        tool("propose_or_execute_lp_halt",
                                "Propose (dryRun=true default) or execute LP venue halt with approvalToken",
                                SAFETY_HIGH_RISK, "fx-admin-service",
                                "/mcp/tools/propose_or_execute_lp_halt")
                )
        );
    }

    private static McpToolDescriptorDto tool(
            String name, String description, String safetyTier, String backend, String path) {
        return new McpToolDescriptorDto(name, description, safetyTier, backend, path);
    }
}
