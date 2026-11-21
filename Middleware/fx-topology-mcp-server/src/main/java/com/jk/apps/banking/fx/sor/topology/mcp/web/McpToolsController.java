package com.jk.apps.banking.fx.sor.topology.mcp.web;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.jk.apps.banking.fx.sor.topology.mcp.dto.McpToolDtos.BlastRadiusRequestDto;
import com.jk.apps.banking.fx.sor.topology.mcp.dto.McpToolDtos.ConsumerLagQueryDto;
import com.jk.apps.banking.fx.sor.topology.mcp.dto.McpToolDtos.LpVenueHaltRequestDto;
import com.jk.apps.banking.fx.sor.topology.mcp.dto.McpToolDtos.McpToolResultDto;
import com.jk.apps.banking.fx.sor.topology.mcp.dto.McpToolDtos.ServiceDependencyQueryDto;
import com.jk.apps.banking.fx.sor.topology.mcp.dto.McpToolDtos.SkewedLoadSimRequestDto;
import com.jk.apps.banking.fx.sor.topology.mcp.dto.McpToolDtos.SyntheticSeedRequestDto;
import com.jk.apps.banking.fx.sor.topology.mcp.dto.McpToolDtos.TopologyDiffRequestDto;
import com.jk.apps.banking.fx.sor.topology.mcp.dto.McpToolDtos.TraceOutlierQueryDto;
import com.jk.apps.banking.fx.sor.topology.mcp.service.McpToolService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/mcp/tools")
@Validated
public class McpToolsController {

    private final McpToolService mcpToolService;

    public McpToolsController(McpToolService mcpToolService) {
        this.mcpToolService = mcpToolService;
    }

    @PostMapping("/get_service_dependencies")
    public ResponseEntity<McpToolResultDto> getServiceDependencies(
            @Valid @RequestBody ServiceDependencyQueryDto request) {
        return toResponse(mcpToolService.getServiceDependencies(request));
    }

    @PostMapping("/calculate_blast_radius")
    public ResponseEntity<McpToolResultDto> calculateBlastRadius(
            @Valid @RequestBody BlastRadiusRequestDto request) {
        return toResponse(mcpToolService.calculateBlastRadius(request));
    }

    @PostMapping("/diff_topology_snapshots")
    public ResponseEntity<McpToolResultDto> diffTopologySnapshots(
            @Valid @RequestBody TopologyDiffRequestDto request) {
        return toResponse(mcpToolService.diffTopologySnapshots(request));
    }

    @PostMapping("/query_iceberg_outlier_traces")
    public ResponseEntity<McpToolResultDto> queryIcebergOutlierTraces(
            @Valid @RequestBody TraceOutlierQueryDto request) {
        return toResponse(mcpToolService.queryIcebergOutlierTraces(request));
    }

    @PostMapping("/get_kafka_consumer_lag")
    public ResponseEntity<McpToolResultDto> getKafkaConsumerLag(
            @Valid @RequestBody ConsumerLagQueryDto request) {
        return toResponse(mcpToolService.getKafkaConsumerLag(request));
    }

    @PostMapping("/trigger_synthetic_seed")
    public ResponseEntity<McpToolResultDto> triggerSyntheticSeed(
            @Valid @RequestBody SyntheticSeedRequestDto request) {
        return toResponse(mcpToolService.triggerSyntheticSeed(request));
    }

    @PostMapping("/trigger_skewed_load_sim")
    public ResponseEntity<McpToolResultDto> triggerSkewedLoadSim(
            @Valid @RequestBody SkewedLoadSimRequestDto request) {
        return toResponse(mcpToolService.triggerSkewedLoadSim(request));
    }

    @PostMapping("/propose_or_execute_lp_halt")
    public ResponseEntity<McpToolResultDto> proposeOrExecuteLpHalt(
            @Valid @RequestBody LpVenueHaltRequestDto request) {
        return toResponse(mcpToolService.proposeOrExecuteLpHalt(request));
    }

    private static ResponseEntity<McpToolResultDto> toResponse(McpToolResultDto result) {
        if (result.success()) {
            return ResponseEntity.ok(result);
        }
        if ("400".equals(result.upstreamStatus())) {
            return ResponseEntity.badRequest().body(result);
        }
        if ("503".equals(result.upstreamStatus())) {
            return ResponseEntity.status(503).body(result);
        }
        return ResponseEntity.status(502).body(result);
    }
}
