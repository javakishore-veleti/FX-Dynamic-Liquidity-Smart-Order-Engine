package com.jk.apps.banking.fx.sor.topology.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/** Turns on the periodic drain of the flow-hop ingest queue (see FlowHopIngest). */
@Configuration
@EnableScheduling
public class IngestScheduling {
}
