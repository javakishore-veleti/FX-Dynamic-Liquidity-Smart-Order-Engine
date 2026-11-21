package com.jk.apps.banking.fx.sor.topology.mcp.service;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import org.springframework.stereotype.Service;

import com.jk.apps.banking.fx.sor.topology.mcp.client.AdminServiceClient;
import com.jk.apps.banking.fx.sor.topology.mcp.client.TopologyServiceClient;
import com.jk.apps.banking.fx.sor.topology.mcp.config.FxFeatureProperties;
import com.jk.apps.banking.fx.sor.topology.mcp.dto.McpToolDtos.BlastRadiusRequestDto;
import com.jk.apps.banking.fx.sor.topology.mcp.dto.McpToolDtos.ConsumerLagQueryDto;
import com.jk.apps.banking.fx.sor.topology.mcp.dto.McpToolDtos.LpVenueHaltRequestDto;
import com.jk.apps.banking.fx.sor.topology.mcp.dto.McpToolDtos.McpToolResultDto;
import com.jk.apps.banking.fx.sor.topology.mcp.dto.McpToolDtos.ServiceDependencyQueryDto;
import com.jk.apps.banking.fx.sor.topology.mcp.dto.McpToolDtos.SkewedLoadSimRequestDto;
import com.jk.apps.banking.fx.sor.topology.mcp.dto.McpToolDtos.SyntheticSeedRequestDto;
import com.jk.apps.banking.fx.sor.topology.mcp.dto.McpToolDtos.TopologyDiffRequestDto;
import com.jk.apps.banking.fx.sor.topology.mcp.dto.McpToolDtos.TraceOutlierQueryDto;
import com.jk.apps.banking.fx.sor.topology.mcp.metrics.ServiceMetrics;

import io.micrometer.core.instrument.Timer;

@Service
public class McpToolService {

    private final TopologyServiceClient topologyClient;
    private final AdminServiceClient adminClient;
    private final McpInvocationAuditService auditService;
    private final ServiceMetrics metrics;
    private final FxFeatureProperties features;

    public McpToolService(
            TopologyServiceClient topologyClient,
            AdminServiceClient adminClient,
            McpInvocationAuditService auditService,
            ServiceMetrics metrics,
            FxFeatureProperties features) {
        this.topologyClient = topologyClient;
        this.adminClient = adminClient;
        this.auditService = auditService;
        this.metrics = metrics;
        this.features = features;
    }

    public McpToolResultDto getServiceDependencies(ServiceDependencyQueryDto request) {
        return invoke("get_service_dependencies", request, McpToolCatalog.SAFETY_READ_ONLY, true,
                () -> mapTopology(topologyClient.getServiceDependencies(request)));
    }

    public McpToolResultDto calculateBlastRadius(BlastRadiusRequestDto request) {
        return invoke("calculate_blast_radius", request, McpToolCatalog.SAFETY_READ_ONLY, true,
                () -> mapTopology(topologyClient.calculateBlastRadius(request)));
    }

    public McpToolResultDto diffTopologySnapshots(TopologyDiffRequestDto request) {
        return invoke("diff_topology_snapshots", request, McpToolCatalog.SAFETY_READ_ONLY, true,
                () -> mapTopology(topologyClient.diffSnapshots(request)));
    }

    public McpToolResultDto queryIcebergOutlierTraces(TraceOutlierQueryDto request) {
        return invoke("query_iceberg_outlier_traces", request, McpToolCatalog.SAFETY_READ_ONLY, true,
                () -> mapTopology(topologyClient.queryOutlierTraces(request)));
    }

    public McpToolResultDto getKafkaConsumerLag(ConsumerLagQueryDto request) {
        return invoke("get_kafka_consumer_lag", request, McpToolCatalog.SAFETY_READ_ONLY, true, () -> {
            Map<String, Object> data = new LinkedHashMap<>();
            data.put("consumerGroup", request.consumerGroup());
            data.put("kafkaEnabled", features.isKafkaEnabled());
            if (!features.isKafkaEnabled()) {
                data.put("lagTotal", 0);
                data.put("partitions", java.util.List.of());
                data.put("note", "kafka feature toggle OFF — returning empty lag snapshot");
                return Mapped.ok(data, "200");
            }
            // Graceful stub when Kafka is enabled but Admin client wiring is optional here.
            data.put("lagTotal", 0);
            data.put("partitions", java.util.List.of());
            data.put("note", "Kafka Admin lag query not configured; empty snapshot returned");
            return Mapped.ok(data, "200");
        });
    }

    public McpToolResultDto triggerSyntheticSeed(SyntheticSeedRequestDto request) {
        return invoke("trigger_synthetic_seed", request, McpToolCatalog.SAFETY_CONTROLLED_WRITE, false,
                () -> mapAdmin(adminClient.triggerSyntheticSeed(request)));
    }

    public McpToolResultDto triggerSkewedLoadSim(SkewedLoadSimRequestDto request) {
        return invoke("trigger_skewed_load_sim", request, McpToolCatalog.SAFETY_CONTROLLED_WRITE, false,
                () -> mapAdmin(adminClient.triggerSkewedLoadSim(request)));
    }

    public McpToolResultDto proposeOrExecuteLpHalt(LpVenueHaltRequestDto request) {
        boolean dryRun = request.dryRun() == null || request.dryRun();
        if (!dryRun && (request.approvalToken() == null || request.approvalToken().isBlank())) {
            long start = System.nanoTime();
            metrics.toolInvocations().increment();
            metrics.toolFailures().increment();
            long durationMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start);
            UUID id = auditService.begin(
                    "propose_or_execute_lp_halt", request, McpToolCatalog.SAFETY_HIGH_RISK, false);
            String error = "approvalToken required when dryRun=false (ADR-032 maker-checker)";
            auditService.fail(id, error, durationMs);
            return McpToolResultDto.fail("propose_or_execute_lp_halt", error, "400", durationMs);
        }
        return invoke("propose_or_execute_lp_halt", request, McpToolCatalog.SAFETY_HIGH_RISK, dryRun,
                () -> mapAdmin(adminClient.proposeOrExecuteLpHalt(request)));
    }

    private McpToolResultDto invoke(
            String toolName,
            Object request,
            String safetyTier,
            boolean dryRun,
            Supplier<Mapped> action) {
        metrics.toolInvocations().increment();
        Timer.Sample sample = Timer.start();
        long startNs = System.nanoTime();
        UUID invocationId = auditService.begin(toolName, request, safetyTier, dryRun);
        try {
            Mapped mapped = action.get();
            long durationMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNs);
            if (mapped.success()) {
                auditService.complete(invocationId, mapped.data(), durationMs);
                return McpToolResultDto.ok(toolName, mapped.data(), durationMs);
            }
            metrics.toolFailures().increment();
            auditService.fail(invocationId, mapped.error(), durationMs);
            return McpToolResultDto.fail(toolName, mapped.error(), mapped.upstreamStatus(), durationMs);
        } catch (Exception ex) {
            metrics.toolFailures().increment();
            long durationMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNs);
            auditService.fail(invocationId, ex.getMessage(), durationMs);
            return McpToolResultDto.fail(toolName, ex.getMessage(), "500", durationMs);
        } finally {
            sample.stop(metrics.toolLatency());
        }
    }

    private static Mapped mapTopology(TopologyServiceClient.ClientResult result) {
        if (result.success()) {
            return Mapped.ok(result.data(), String.valueOf(result.status()));
        }
        return Mapped.fail(result.error(), String.valueOf(result.status()));
    }

    private static Mapped mapAdmin(AdminServiceClient.ClientResult result) {
        if (result.success()) {
            return Mapped.ok(result.data(), String.valueOf(result.status()));
        }
        return Mapped.fail(result.error(), String.valueOf(result.status()));
    }

    private record Mapped(boolean success, Object data, String error, String upstreamStatus) {
        static Mapped ok(Object data, String status) {
            return new Mapped(true, data, null, status);
        }

        static Mapped fail(String error, String status) {
            return new Mapped(false, null, error, status);
        }
    }
}
