package com.jk.apps.banking.fx.sor.agent.service.workflow;

import com.jk.apps.banking.fx.sor.agent.client.TopologyMcpClient;
import com.jk.apps.banking.fx.sor.agent.dto.PolicyAuthoringRequest;
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
public class PolicyAuthoringAgentWorkflow {

    private final TopologyMcpClient mcpClient;
    private final ChatModelGateway chatModelGateway;

    public PolicyAuthoringAgentWorkflow(TopologyMcpClient mcpClient, ChatModelGateway chatModelGateway) {
        this.mcpClient = mcpClient;
        this.chatModelGateway = chatModelGateway;
    }

    public AgentWorkflowResult execute(PolicyAuthoringRequest request) {
        List<Map<String, Object>> invocations = new ArrayList<>();

        invocations.add(mcpClient.invokeTool("get_existing_dmn_table", Map.of(
                "existingDmnRef", nullToEmpty(request.getExistingDmnRef())
        )));
        invocations.add(mcpClient.invokeTool("shadow_replay_policy_candidate", Map.of(
                "intentText", nullToEmpty(request.getIntentText()),
                "dryRun", request.isDryRun(),
                "approvalToken", nullToEmpty(request.getApprovalToken())
        )));

        LlmCompletionResponse llm = chatModelGateway.complete(new LlmCompletionRequest(
                "Author Drools/DMN policy from intent: " + nullToEmpty(request.getIntentText())
                        + " existingDmnRef=" + nullToEmpty(request.getExistingDmnRef()),
                "You are an FX natural-language Drools/DMN policy authoring agent. Emit structured rule JSON.",
                ModelTier.REASONING,
                0.2
        ));

        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("agent", "POLICY_AUTHORING");
        summary.put("dryRun", request.isDryRun());
        summary.put("existingDmnRef", request.getExistingDmnRef());
        summary.put("toolsInvoked", List.of("get_existing_dmn_table", "shadow_replay_policy_candidate"));
        summary.put("llmProvider", llm.provider());
        summary.put("modelId", llm.modelId());
        summary.put("llmContent", llm.content());
        summary.put("note", "Shadow replay only unless dryRun=false + approvalToken");
        return new AgentWorkflowResult(invocations, summary);
    }

    private static String nullToEmpty(String value) {
        return value == null ? "" : value;
    }
}
