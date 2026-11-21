package com.jk.apps.banking.fx.sor.topology.mcp.dto;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Standardized 8-tool MCP DTO contracts (ADR-033 catalog parity with Python stack).
 */
public final class McpToolDtos {

    private McpToolDtos() {
    }

    public record ServiceDependencyQueryDto(
            @NotBlank String serviceName,
            String layer,
            Integer depth
    ) {
        public ServiceDependencyQueryDto {
            if (layer == null || layer.isBlank()) {
                layer = "network";
            }
            if (depth == null || depth < 1) {
                depth = 3;
            }
        }
    }

    public record BlastRadiusRequestDto(
            @NotBlank String serviceName,
            String ccyPair
    ) {
    }

    public record TopologyDiffRequestDto(
            @NotBlank String fromTs,
            @NotBlank String toTs
    ) {
    }

    public record TraceOutlierQueryDto(
            @NotBlank String serviceName,
            Integer minLatencyMs,
            Integer limit
    ) {
        public TraceOutlierQueryDto {
            if (minLatencyMs == null || minLatencyMs < 0) {
                minLatencyMs = 10;
            }
            if (limit == null || limit < 1) {
                limit = 50;
            }
        }
    }

    public record ConsumerLagQueryDto(
            @NotBlank String consumerGroup
    ) {
    }

    public record SyntheticSeedRequestDto(
            Integer numBatches,
            Integer batchSize,
            List<String> datasets
    ) {
        public SyntheticSeedRequestDto {
            if (numBatches == null || numBatches < 1) {
                numBatches = 10;
            }
            if (batchSize == null || batchSize < 1) {
                batchSize = 1000;
            }
            if (datasets == null) {
                datasets = List.of();
            }
        }
    }

    public record SkewedLoadSimRequestDto(
            Integer targetRps,
            Integer durationSec,
            String ccyPair
    ) {
        public SkewedLoadSimRequestDto {
            if (targetRps == null || targetRps < 1) {
                targetRps = 1000;
            }
            if (durationSec == null || durationSec < 1) {
                durationSec = 60;
            }
            if (ccyPair == null || ccyPair.isBlank()) {
                ccyPair = "EURUSD";
            }
        }
    }

    public record LpVenueHaltRequestDto(
            @NotBlank String lpId,
            @NotBlank String reason,
            Boolean dryRun,
            String approvalToken
    ) {
        public LpVenueHaltRequestDto {
            if (dryRun == null) {
                dryRun = Boolean.TRUE;
            }
        }
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record McpToolResultDto(
            boolean success,
            String toolName,
            Object data,
            String error,
            String upstreamStatus,
            Long durationMs
    ) {
        public static McpToolResultDto ok(String toolName, Object data, long durationMs) {
            return new McpToolResultDto(true, toolName, data, null, null, durationMs);
        }

        public static McpToolResultDto fail(String toolName, String error, String upstreamStatus, long durationMs) {
            return new McpToolResultDto(false, toolName, null, error, upstreamStatus, durationMs);
        }
    }

    public record McpToolDescriptorDto(
            String name,
            String description,
            String safetyTier,
            String backend,
            String path
    ) {
    }

    public record McpCatalogDto(
            String server,
            String transport,
            List<McpToolDescriptorDto> tools
    ) {
    }

    public record BackendEnvelopeDto(
            @NotNull Map<String, Object> body
    ) {
    }
}
