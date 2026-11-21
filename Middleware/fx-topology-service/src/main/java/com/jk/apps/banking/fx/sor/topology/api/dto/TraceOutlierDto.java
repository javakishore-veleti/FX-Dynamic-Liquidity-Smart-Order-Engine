package com.jk.apps.banking.fx.sor.topology.api.dto;

import java.time.Instant;
import java.util.UUID;

public record TraceOutlierDto(
        UUID id,
        String serviceName,
        String clientOrderId,
        String droolsRuleVersion,
        String knockoutReason,
        String winningLpId,
        double latencyMs,
        Instant observedAt
) {
}
