package com.jk.apps.banking.fx.sor.topology.service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class FreshnessTest {

    private final Freshness f = new Freshness(Duration.ofMinutes(10));
    private final Instant now = Instant.parse("2026-11-22T12:00:00Z");

    @Test
    void recentEdgeIsFresh() {
        assertTrue(f.isFresh(now.minus(Duration.ofMinutes(5)), now));
    }

    @Test
    void staleEdgeFades() {
        assertFalse(f.isFresh(now.minus(Duration.ofMinutes(11)), now));
    }

    @Test
    void missingTimestampIsNotFresh() {
        assertFalse(f.isFresh(null, now));
    }
}
