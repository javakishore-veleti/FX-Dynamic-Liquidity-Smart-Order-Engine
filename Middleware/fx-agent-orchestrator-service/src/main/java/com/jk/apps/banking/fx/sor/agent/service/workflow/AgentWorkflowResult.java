package com.jk.apps.banking.fx.sor.agent.service.workflow;

import java.util.List;
import java.util.Map;

public record AgentWorkflowResult(
        List<Map<String, Object>> mcpInvocations,
        Map<String, Object> summary
) {
}
