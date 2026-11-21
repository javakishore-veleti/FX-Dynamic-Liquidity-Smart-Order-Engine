package com.jk.apps.banking.fx.sor.topology.api.dto;

public record BlastRadiusRequest(
        String serviceName,
        String ccyPair,
        Integer depth
) {
}
