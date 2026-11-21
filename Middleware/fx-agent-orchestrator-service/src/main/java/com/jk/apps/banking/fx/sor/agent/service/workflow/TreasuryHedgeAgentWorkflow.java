package com.jk.apps.banking.fx.sor.agent.service.workflow;

import com.jk.apps.banking.fx.sor.agent.client.TopologyMcpClient;
import com.jk.apps.banking.fx.sor.agent.dto.TreasuryHedgeCopilotRequest;
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
public class TreasuryHedgeAgentWorkflow {

    private final TopologyMcpClient mcpClient;
    private final ChatModelGateway chatModelGateway;

    public TreasuryHedgeAgentWorkflow(TopologyMcpClient mcpClient, ChatModelGateway chatModelGateway) {
        this.mcpClient = mcpClient;
        this.chatModelGateway = chatModelGateway;
    }

    public AgentWorkflowResult execute(TreasuryHedgeCopilotRequest request) {
        List<Map<String, Object>> invocations = new ArrayList<>();

        invocations.add(mcpClient.invokeTool("qualify_corporate_order", Map.of(
                "clientId", nullToEmpty(request.getClientId()),
                "ccyPair", nullToEmpty(request.getCcyPair()),
                "notionalUsd", request.getNotionalUsd() == null ? 0 : request.getNotionalUsd()
        )));
        invocations.add(mcpClient.invokeTool("get_credit_utilization", Map.of(
                "clientId", nullToEmpty(request.getClientId())
        )));

        String userPrompt = request.getPrompt() == null || request.getPrompt().isBlank()
                ? "Propose hedge block-split for client=" + nullToEmpty(request.getClientId())
                + " ccyPair=" + nullToEmpty(request.getCcyPair())
                + " notionalUsd=" + request.getNotionalUsd()
                : request.getPrompt();

        LlmCompletionResponse llm = chatModelGateway.complete(new LlmCompletionRequest(
                userPrompt,
                "You are a corporate treasury hedge and block-splitting copilot.",
                ModelTier.REASONING,
                0.2
        ));

        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("agent", "TREASURY_HEDGE");
        summary.put("dryRun", request.isDryRun());
        summary.put("clientId", request.getClientId());
        summary.put("ccyPair", request.getCcyPair());
        summary.put("notionalUsd", request.getNotionalUsd());
        summary.put("toolsInvoked", List.of("qualify_corporate_order", "get_credit_utilization"));
        summary.put("llmProvider", llm.provider());
        summary.put("modelId", llm.modelId());
        summary.put("llmContent", llm.content());
        return new AgentWorkflowResult(invocations, summary);
    }

    private static String nullToEmpty(String value) {
        return value == null ? "" : value;
    }
}
