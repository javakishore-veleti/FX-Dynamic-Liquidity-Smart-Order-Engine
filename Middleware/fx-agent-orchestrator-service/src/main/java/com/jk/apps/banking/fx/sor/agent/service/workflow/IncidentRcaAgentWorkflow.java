package com.jk.apps.banking.fx.sor.agent.service.workflow;

import com.jk.apps.banking.fx.sor.agent.client.TopologyMcpClient;
import com.jk.apps.banking.fx.sor.agent.dto.IncidentRcaInvestigateRequest;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class IncidentRcaAgentWorkflow {

    private final TopologyMcpClient mcpClient;

    public IncidentRcaAgentWorkflow(TopologyMcpClient mcpClient) {
        this.mcpClient = mcpClient;
    }

    public AgentWorkflowResult execute(IncidentRcaInvestigateRequest request) {
        List<Map<String, Object>> invocations = new ArrayList<>();

        invocations.add(mcpClient.invokeTool("query_iceberg_outlier_traces", Map.of(
                "serviceName", nullToEmpty(request.getServiceName()),
                "minLatencyMs", request.getMinLatencyMs() == null ? 10L : request.getMinLatencyMs(),
                "limit", request.getLimit() == null ? 50 : request.getLimit()
        )));

        invocations.add(mcpClient.invokeTool("get_ipc_edge_health", Map.of(
                "serviceName", nullToEmpty(request.getServiceName()),
                "ccyPair", nullToEmpty(request.getCcyPair())
        )));

        Map<String, Object> haltPayload = new LinkedHashMap<>();
        haltPayload.put("lpId", nullToEmpty(request.getLpId()));
        haltPayload.put("reason", request.getReason() == null
                ? "Incident RCA proposed halt"
                : request.getReason());
        haltPayload.put("dryRun", request.isDryRun());
        if (request.getApprovalToken() != null) {
            haltPayload.put("approvalToken", request.getApprovalToken());
        }
        invocations.add(mcpClient.invokeTool("propose_or_execute_lp_halt", haltPayload));

        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("agent", "INCIDENT_RCA");
        summary.put("dryRun", request.isDryRun());
        summary.put("serviceName", request.getServiceName());
        summary.put("lpId", request.getLpId());
        summary.put("toolsInvoked", List.of(
                "query_iceberg_outlier_traces",
                "get_ipc_edge_health",
                "propose_or_execute_lp_halt"
        ));
        summary.put("governance", "ADR-032: live LP halt requires dryRun=false + Maker-Checker approvalToken");
        return new AgentWorkflowResult(invocations, summary);
    }

    private static String nullToEmpty(String value) {
        return value == null ? "" : value;
    }
}
