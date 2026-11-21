package com.jk.apps.banking.fx.sor.topology.api.dto;

import java.time.Instant;
import java.util.UUID;

public record SnapshotSummaryDto(
        UUID id,
        Instant snapshotTs,
        String layer,
        int edgeCount,
        String nodeSummary
) {
}
