package com.jk.apps.banking.fx.sor.agent.service.workflow;

import com.jk.apps.banking.fx.sor.agent.client.TopologyMcpClient;
import com.jk.apps.banking.fx.sor.agent.dto.BlastRadiusAnalyzeRequest;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class BlastRadiusAgentWorkflow {

    private final TopologyMcpClient mcpClient;

    public BlastRadiusAgentWorkflow(TopologyMcpClient mcpClient) {
        this.mcpClient = mcpClient;
    }

    public AgentWorkflowResult execute(BlastRadiusAnalyzeRequest request) {
        List<Map<String, Object>> invocations = new ArrayList<>();

        invocations.add(mcpClient.invokeTool("get_service_dependencies", Map.of(
                "serviceName", nullToEmpty(request.getServiceName()),
                "layer", "UNIFIED",
                "depth", request.getDepth() == null ? 3 : request.getDepth()
        )));

        invocations.add(mcpClient.invokeTool("calculate_blast_radius", Map.of(
                "serviceName", nullToEmpty(request.getServiceName()),
                "ccyPair", nullToEmpty(request.getCcyPair())
        )));

        Map<String, Object> diffPayload = new LinkedHashMap<>();
        if (request.getFromTs() != null) {
            diffPayload.put("fromTs", request.getFromTs());
        }
        if (request.getToTs() != null) {
            diffPayload.put("toTs", request.getToTs());
        }
        invocations.add(mcpClient.invokeTool("diff_topology_snapshots", diffPayload));

        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("agent", "BLAST_RADIUS");
        summary.put("dryRun", request.isDryRun());
        summary.put("serviceName", request.getServiceName());
        summary.put("ccyPair", request.getCcyPair());
        summary.put("toolsInvoked", List.of(
                "get_service_dependencies",
                "calculate_blast_radius",
                "diff_topology_snapshots"
        ));
        summary.put("note", "Manual pre-change validation; live mutations require dryRun=false + approvalToken");
        return new AgentWorkflowResult(invocations, summary);
    }

    private static String nullToEmpty(String value) {
        return value == null ? "" : value;
    }
}
