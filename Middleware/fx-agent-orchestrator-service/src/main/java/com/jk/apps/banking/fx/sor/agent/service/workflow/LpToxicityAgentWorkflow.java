package com.jk.apps.banking.fx.sor.agent.service.workflow;

import com.jk.apps.banking.fx.sor.agent.client.TopologyMcpClient;
import com.jk.apps.banking.fx.sor.agent.dto.LpToxicityDetectRequest;
import com.jk.apps.banking.fx.sor.agent.llm.ChatModelGateway;
import com.jk.apps.banking.fx.sor.agent.llm.LlmCompletionRequest;
import com.jk.apps.banking.fx.sor.agent.llm.LlmCompletionResponse;
import com.jk.apps.banking.fx.sor.agent.llm.ModelTier;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class LpToxicityAgentWorkflow {

    private final TopologyMcpClient mcpClient;
    private final ChatModelGateway chatModelGateway;

    public LpToxicityAgentWorkflow(TopologyMcpClient mcpClient, ChatModelGateway chatModelGateway) {
        this.mcpClient = mcpClient;
        this.chatModelGateway = chatModelGateway;
    }

    public AgentWorkflowResult execute(LpToxicityDetectRequest request) {
        List<Map<String, Object>> invocations = new ArrayList<>();

        invocations.add(mcpClient.invokeTool("lp_telemetry_projections", Map.of(
                "lpId", nullToEmpty(request.getLpId()),
                "ccyPair", nullToEmpty(request.getCcyPair()),
                "windowMinutes", request.getWindowMinutes() == null ? 15 : request.getWindowMinutes()
        )));
        invocations.add(mcpClient.invokeTool("get_last_look_failover_events", Map.of(
                "lpId", nullToEmpty(request.getLpId()),
                "ccyPair", nullToEmpty(request.getCcyPair())
        )));

        LlmCompletionResponse llm = chatModelGateway.complete(new LlmCompletionRequest(
                "Detect LP toxicity / last-look anomalies for lpId=" + nullToEmpty(request.getLpId())
                        + " ccyPair=" + nullToEmpty(request.getCcyPair())
                        + " windowMinutes=" + (request.getWindowMinutes() == null ? 15 : request.getWindowMinutes()),
                "You are an LP toxicity and last-look anomaly detective. Redact raw wholesale spreads.",
                ModelTier.FAST,
                0.2
        ));

        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("agent", "LP_TOXICITY");
        summary.put("dryRun", request.isDryRun());
        summary.put("lpId", request.getLpId());
        summary.put("ccyPair", request.getCcyPair());
        summary.put("windowMinutes", request.getWindowMinutes());
        summary.put("toolsInvoked", List.of("lp_telemetry_projections", "get_last_look_failover_events"));
        summary.put("llmProvider", llm.provider());
        summary.put("modelId", llm.modelId());
        summary.put("llmContent", llm.content());
        return new AgentWorkflowResult(invocations, summary);
    }

    private static String nullToEmpty(String value) {
        return value == null ? "" : value;
    }
}
