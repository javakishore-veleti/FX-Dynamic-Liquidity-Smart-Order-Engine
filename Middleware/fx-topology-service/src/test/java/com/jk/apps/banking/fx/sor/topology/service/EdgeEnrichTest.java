package com.jk.apps.banking.fx.sor.topology.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.jk.apps.banking.fx.sor.topology.service.EdgeEnrich.ServiceMeta;
import java.util.Map;
import org.junit.jupiter.api.Test;

class EdgeEnrichTest {

    @Test
    void enrichesWithTiersAndDomainAndMarksUnknown() {
        EdgeEnrich e = new EdgeEnrich(Map.of(
                "fx-quote-service", new ServiceMeta("TIER_0", "PRICING", "pricing-team")));
        EdgeEnrich.Enriched r = e.enrich("fx-quote-service", "unknown-svc");
        assertEquals("TIER_0", r.srcTier());
        assertEquals(EdgeEnrich.UNKNOWN, r.dstTier());
        assertEquals("PRICING", r.domain());
    }
}
