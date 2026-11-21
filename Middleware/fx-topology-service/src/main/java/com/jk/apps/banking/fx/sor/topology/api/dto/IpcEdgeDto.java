package com.jk.apps.banking.fx.sor.topology.api.dto;

import java.time.Instant;
import java.util.UUID;

public record IpcEdgeDto(
        UUID id,
        String srcService,
        String dstService,
        String ccyPair,
        String window,
        String availabilityTier,
        String deskOwner,
        Double p99Ms,
        Double stpFillPct,
        String circuitState,
        Instant observedAt
) {
}
