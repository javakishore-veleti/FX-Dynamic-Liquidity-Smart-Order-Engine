package com.jk.apps.banking.fx.sor.topology.api.dto;

import java.util.List;

public record NetworkGraphResponse(
        String service,
        int depth,
        List<NetworkEdgeDto> edges
) {
}
