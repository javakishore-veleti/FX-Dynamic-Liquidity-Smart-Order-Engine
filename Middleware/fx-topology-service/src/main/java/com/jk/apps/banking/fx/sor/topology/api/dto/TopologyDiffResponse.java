package com.jk.apps.banking.fx.sor.topology.api.dto;

import java.time.Instant;
import java.util.List;

public record TopologyDiffResponse(
        Instant from,
        Instant to,
        Integer fromEdgeCount,
        Integer toEdgeCount,
        Integer edgeDelta,
        List<String> nodesAdded,
        List<String> nodesRemoved,
        List<SnapshotSummaryDto> snapshotsInRange
) {
}
