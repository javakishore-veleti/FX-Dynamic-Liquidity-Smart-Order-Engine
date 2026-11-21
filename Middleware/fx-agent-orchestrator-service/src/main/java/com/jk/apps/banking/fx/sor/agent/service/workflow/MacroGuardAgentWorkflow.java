package com.jk.apps.banking.fx.sor.agent.service.workflow;

import com.jk.apps.banking.fx.sor.agent.client.TopologyMcpClient;
import com.jk.apps.banking.fx.sor.agent.dto.MacroGuardAssessRequest;
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
public class MacroGuardAgentWorkflow {

    private final TopologyMcpClient mcpClient;
    private final ChatModelGateway chatModelGateway;

    public MacroGuardAgentWorkflow(TopologyMcpClient mcpClient, ChatModelGateway chatModelGateway) {
        this.mcpClient = mcpClient;
        this.chatModelGateway = chatModelGateway;
    }

    public AgentWorkflowResult execute(MacroGuardAssessRequest request) {
        List<Map<String, Object>> invocations = new ArrayList<>();

        invocations.add(mcpClient.invokeTool("assess_macro_headline_regime", Map.of(
                "headline", nullToEmpty(request.getHeadline()),
                "ccyPair", nullToEmpty(request.getCcyPair())
        )));

        LlmCompletionResponse llm = chatModelGateway.complete(new LlmCompletionRequest(
                "Assess macro/central-bank volatility for headline=\"" + nullToEmpty(request.getHeadline())
                        + "\" ccyPair=" + nullToEmpty(request.getCcyPair()),
                "You are a macro news and central bank volatility guard. Return regime severity and spread multipliers.",
                ModelTier.VOLUME,
                0.2
        ));

        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("agent", "MACRO_GUARD");
        summary.put("dryRun", request.isDryRun());
        summary.put("headline", request.getHeadline());
        summary.put("ccyPair", request.getCcyPair());
        summary.put("toolsInvoked", List.of("assess_macro_headline_regime"));
        summary.put("llmProvider", llm.provider());
        summary.put("modelId", llm.modelId());
        summary.put("llmContent", llm.content());
        return new AgentWorkflowResult(invocations, summary);
    }

    private static String nullToEmpty(String value) {
        return value == null ? "" : value;
    }
}
