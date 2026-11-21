package com.jk.apps.banking.fx.sor.topology.api.dto;

import java.util.List;

public record BlastRadiusResponse(
        String serviceName,
        String ccyPair,
        int depth,
        List<String> impactedServices,
        List<NetworkEdgeDto> networkEdges,
        List<IpcEdgeDto> ipcEdges,
        long queryDurationMs
) {
}
