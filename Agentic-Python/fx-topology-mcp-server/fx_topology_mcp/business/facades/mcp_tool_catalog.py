from __future__ import annotations

from fx_topology_mcp.business.dtos.tool_contract_dtos import (
    CatalogRequestDto,
    McpCatalogDto,
    McpToolDescriptorDto,
)


class McpToolCatalog:
    SAFETY_READ_ONLY = "READ_ONLY"
    SAFETY_CONTROLLED_WRITE = "CONTROLLED_WRITE"
    SAFETY_HIGH_RISK = "HIGH_RISK_MUTATION"

    def catalog(self, dto: CatalogRequestDto) -> McpCatalogDto:
        _ = dto
        return McpCatalogDto(
            server="fx-topology-mcp-server",
            transport="streamable-http",
            tools=[
                McpToolDescriptorDto(
                    name="get_service_dependencies",
                    description="Resolve service dependencies across topology layers",
                    safety_tier=self.SAFETY_READ_ONLY,
                    backend="fx-topology-service",
                    path="/mcp/tools/get_service_dependencies",
                ),
                McpToolDescriptorDto(
                    name="calculate_blast_radius",
                    description="Compute downstream blast radius for a service/ccyPair",
                    safety_tier=self.SAFETY_READ_ONLY,
                    backend="fx-topology-service",
                    path="/mcp/tools/calculate_blast_radius",
                ),
                McpToolDescriptorDto(
                    name="diff_topology_snapshots",
                    description="Time-travel diff between topology snapshots",
                    safety_tier=self.SAFETY_READ_ONLY,
                    backend="fx-topology-service",
                    path="/mcp/tools/diff_topology_snapshots",
                ),
                McpToolDescriptorDto(
                    name="query_iceberg_outlier_traces",
                    description="Query Iceberg outlier traces above latency threshold",
                    safety_tier=self.SAFETY_READ_ONLY,
                    backend="fx-topology-service",
                    path="/mcp/tools/query_iceberg_outlier_traces",
                ),
                McpToolDescriptorDto(
                    name="get_kafka_consumer_lag",
                    description="Read Kafka consumer-group lag (Prometheus/Kafka Admin)",
                    safety_tier=self.SAFETY_READ_ONLY,
                    backend="prometheus-kafka",
                    path="/mcp/tools/get_kafka_consumer_lag",
                ),
                McpToolDescriptorDto(
                    name="trigger_synthetic_seed",
                    description="Queue synthetic dataset seed via fx-admin-service (non-prod)",
                    safety_tier=self.SAFETY_CONTROLLED_WRITE,
                    backend="fx-admin-service",
                    path="/mcp/tools/trigger_synthetic_seed",
                ),
                McpToolDescriptorDto(
                    name="trigger_skewed_load_sim",
                    description="Queue skewed hot-partition load simulation (non-prod)",
                    safety_tier=self.SAFETY_CONTROLLED_WRITE,
                    backend="fx-admin-service",
                    path="/mcp/tools/trigger_skewed_load_sim",
                ),
                McpToolDescriptorDto(
                    name="propose_or_execute_lp_halt",
                    description="Propose (dryRun=true default) or execute LP venue halt with approvalToken",
                    safety_tier=self.SAFETY_HIGH_RISK,
                    backend="fx-admin-service",
                    path="/mcp/tools/propose_or_execute_lp_halt",
                ),
            ],
        )
