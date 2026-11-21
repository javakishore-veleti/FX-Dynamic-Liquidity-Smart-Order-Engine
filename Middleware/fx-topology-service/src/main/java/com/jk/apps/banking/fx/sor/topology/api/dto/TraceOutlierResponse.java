package com.jk.apps.banking.fx.sor.topology.api.dto;

import java.util.List;

public record TraceOutlierResponse(
        double minLatencyMs,
        String serviceName,
        List<TraceOutlierDto> outliers
) {
}
