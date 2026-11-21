package com.jk.apps.banking.fx.sor.agent.dto;

import java.time.Instant;
import java.util.UUID;

public record AgentAcceptedResponse(
        UUID wfExecId,
        String agentType,
        String status,
        boolean dryRun,
        String triggeredBy,
        Instant acceptedAt
) {
}
