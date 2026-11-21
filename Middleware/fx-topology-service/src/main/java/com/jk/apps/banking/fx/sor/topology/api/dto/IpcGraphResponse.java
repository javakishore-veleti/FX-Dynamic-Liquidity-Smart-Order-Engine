package com.jk.apps.banking.fx.sor.topology.api.dto;

import java.util.List;

public record IpcGraphResponse(
        String ccyPair,
        String window,
        List<IpcEdgeDto> edges
) {
}
