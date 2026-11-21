package com.jk.apps.banking.fx.sor.agent.service.workflow;

import com.jk.apps.banking.fx.sor.agent.client.TopologyMcpClient;
import com.jk.apps.banking.fx.sor.agent.dto.ChaosSimExecuteRequest;
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
public class ChaosSimAgentWorkflow {

    private final TopologyMcpClient mcpClient;
    private final ChatModelGateway chatModelGateway;

    public ChaosSimAgentWorkflow(TopologyMcpClient mcpClient, ChatModelGateway chatModelGateway) {
        this.mcpClient = mcpClient;
        this.chatModelGateway = chatModelGateway;
    }

    public AgentWorkflowResult execute(ChaosSimExecuteRequest request) {
        List<Map<String, Object>> invocations = new ArrayList<>();

        Map<String, Object> seedPayload = new LinkedHashMap<>();
        seedPayload.put("numBatches", request.getNumBatches() == null ? 10 : request.getNumBatches());
        seedPayload.put("batchSize", request.getBatchSize() == null ? 1000 : request.getBatchSize());
        seedPayload.put("dryRun", request.isDryRun());
        if (request.getDatasets() != null) {
            seedPayload.put("datasets", request.getDatasets());
        }
        invocations.add(mcpClient.invokeTool("trigger_synthetic_seed", seedPayload));

        Map<String, Object> loadPayload = new LinkedHashMap<>();
        loadPayload.put("targetRps", request.getTargetRps() == null ? 50_000 : request.getTargetRps());
        loadPayload.put("durationSec", request.getDurationSec() == null ? 60 : request.getDurationSec());
        loadPayload.put("ccyPair", request.getCcyPair() == null ? "EURUSD" : request.getCcyPair());
        loadPayload.put("dryRun", request.isDryRun());
        Map<String, Object> loadResult = mcpClient.invokeTool("trigger_skewed_load_sim", loadPayload);
        invocations.add(loadResult);

        Object nestedWf = null;
        if (loadResult.get("response") instanceof Map<?, ?> response) {
            nestedWf = response.get("wfExecId");
        }
        invocations.add(mcpClient.invokeTool("poll_workflow_status", Map.of(
                "wfExecId", nestedWf == null ? "" : nestedWf.toString(),
                "dryRun", request.isDryRun()
        )));

        LlmCompletionResponse llm = chatModelGateway.complete(new LlmCompletionRequest(
                "Plan synthetic chaos / skewed load for ccyPair="
                        + (request.getCcyPair() == null ? "EURUSD" : request.getCcyPair())
                        + " targetRps=" + (request.getTargetRps() == null ? 50_000 : request.getTargetRps()),
                "You are a synthetic chaos and skewed load-generator agent.",
                ModelTier.FAST,
                0.2
        ));

        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("agent", "CHAOS_SIM");
        summary.put("dryRun", request.isDryRun());
        summary.put("ccyPair", request.getCcyPair());
        summary.put("targetRps", request.getTargetRps());
        summary.put("toolsInvoked", List.of(
                "trigger_synthetic_seed",
                "trigger_skewed_load_sim",
                "poll_workflow_status"
        ));
        summary.put("llmProvider", llm.provider());
        summary.put("modelId", llm.modelId());
        summary.put("llmContent", llm.content());
        summary.put("note", "Staging-only chaos; dryRun=true by default (ADR-032)");
        return new AgentWorkflowResult(invocations, summary);
    }
}
