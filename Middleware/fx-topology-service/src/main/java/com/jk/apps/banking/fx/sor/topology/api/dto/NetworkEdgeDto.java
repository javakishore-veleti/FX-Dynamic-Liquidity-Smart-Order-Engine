package com.jk.apps.banking.fx.sor.topology.api.dto;

import java.time.Instant;
import java.util.UUID;

public record NetworkEdgeDto(
        UUID id,
        String srcService,
        String dstService,
        String timeBucket,
        String podNamespace,
        String azZone,
        String egressLpGateway,
        int socketRetransmits,
        long flowCount,
        Instant observedAt
) {
}
