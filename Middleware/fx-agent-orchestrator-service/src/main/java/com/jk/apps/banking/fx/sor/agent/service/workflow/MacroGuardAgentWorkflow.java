package com.jk.apps.banking.fx.sor.agent.service.workflow;

import com.jk.apps.banking.fx.sor.agent.client.QuoteMacroClient;
import com.jk.apps.banking.fx.sor.agent.client.TopologyMcpClient;
import com.jk.apps.banking.fx.sor.agent.dto.MacroGuardAssessRequest;
import com.jk.apps.banking.fx.sor.agent.llm.ChatModelGateway;
import com.jk.apps.banking.fx.sor.agent.llm.LlmCompletionRequest;
import com.jk.apps.banking.fx.sor.agent.llm.LlmCompletionResponse;
import com.jk.apps.banking.fx.sor.agent.llm.ModelTier;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class MacroGuardAgentWorkflow {

    private final TopologyMcpClient mcpClient;
    private final ChatModelGateway chatModelGateway;
    private final QuoteMacroClient quoteMacroClient;

    public MacroGuardAgentWorkflow(
            TopologyMcpClient mcpClient,
            ChatModelGateway chatModelGateway,
            QuoteMacroClient quoteMacroClient) {
        this.mcpClient = mcpClient;
        this.chatModelGateway = chatModelGateway;
        this.quoteMacroClient = quoteMacroClient;
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

        String source = detectSource(request.getHeadline());
        double severity = 1.25;
        Map<String, Object> armResult = Map.of("skipped", true, "reason", "dryRun");
        if (!request.isDryRun()) {
            armResult = quoteMacroClient.armMacro(source, severity, 3600);
            invocations.add(Map.of("tool", "quote_macro_flag", "ok", true, "response", armResult));
        }

        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("agent", "MACRO_GUARD");
        summary.put("dryRun", request.isDryRun());
        summary.put("headline", request.getHeadline());
        summary.put("ccyPair", request.getCcyPair());
        summary.put("macroSource", source);
        summary.put("severity", severity);
        summary.put("quoteMacroArm", armResult);
        summary.put("toolsInvoked", List.of("assess_macro_headline_regime", "quote_macro_flag"));
        summary.put("llmProvider", llm.provider());
        summary.put("modelId", llm.modelId());
        summary.put("llmContent", llm.content());
        return new AgentWorkflowResult(invocations, summary);
    }

    private static String detectSource(String headline) {
        String h = nullToEmpty(headline).toUpperCase(Locale.ROOT);
        if (h.contains("NFP") || h.contains("PAYROLL")) {
            return "NFP";
        }
        if (h.contains("ECB")) {
            return "ECB";
        }
        return "FOMC";
    }

    private static String nullToEmpty(String value) {
        return value == null ? "" : value;
    }
}
