package com.jk.apps.banking.fx.sor.agent.service.workflow;

import com.jk.apps.banking.fx.sor.agent.client.TopologyMcpClient;
import com.jk.apps.banking.fx.sor.agent.dto.BackpressureAssessRequest;
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
public class BackpressureAgentWorkflow {

    private final TopologyMcpClient mcpClient;
    private final ChatModelGateway chatModelGateway;

    public BackpressureAgentWorkflow(TopologyMcpClient mcpClient, ChatModelGateway chatModelGateway) {
        this.mcpClient = mcpClient;
        this.chatModelGateway = chatModelGateway;
    }

    public AgentWorkflowResult execute(BackpressureAssessRequest request) {
        List<Map<String, Object>> invocations = new ArrayList<>();

        invocations.add(mcpClient.invokeTool("get_kafka_consumer_lag", Map.of(
                "consumerGroup", nullToEmpty(request.getConsumerGroup())
        )));
        invocations.add(mcpClient.invokeTool("get_pod_zgc_and_cpu_metrics", Map.of(
                "serviceName", nullToEmpty(request.getServiceName())
        )));
        invocations.add(mcpClient.invokeTool("recommend_or_apply_scaling", Map.of(
                "serviceName", nullToEmpty(request.getServiceName()),
                "applyScaling", request.isApplyScaling(),
                "dryRun", request.isDryRun(),
                "approvalToken", nullToEmpty(request.getApprovalToken())
        )));

        LlmCompletionResponse llm = chatModelGateway.complete(new LlmCompletionRequest(
                "Assess Kafka lag / backpressure for consumerGroup=" + nullToEmpty(request.getConsumerGroup())
                        + " service=" + nullToEmpty(request.getServiceName()),
                "You are an FX SOR backpressure remediation agent.",
                ModelTier.VOLUME,
                0.2
        ));

        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("agent", "BACKPRESSURE");
        summary.put("dryRun", request.isDryRun());
        summary.put("consumerGroup", request.getConsumerGroup());
        summary.put("serviceName", request.getServiceName());
        summary.put("applyScaling", request.isApplyScaling() && !request.isDryRun());
        summary.put("toolsInvoked", List.of(
                "get_kafka_consumer_lag",
                "get_pod_zgc_and_cpu_metrics",
                "recommend_or_apply_scaling"
        ));
        summary.put("llmProvider", llm.provider());
        summary.put("modelId", llm.modelId());
        summary.put("llmContent", llm.content());
        summary.put("recommendation", request.isDryRun()
                ? "DRY_RUN: scaling recommendation only; no cluster mutation"
                : "LIVE: applyScaling gated by approvalToken");

        return new AgentWorkflowResult(invocations, summary);
    }

    private static String nullToEmpty(String value) {
        return value == null ? "" : value;
    }
}
