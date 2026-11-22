package com.jk.apps.banking.fx.sor.topology.service;

import java.time.Duration;
import java.time.Instant;

/**
 * Premise P13 (Netflix TechBlog): a living map. An edge that has not been seen within the freshness
 * window fades from the view. It is hidden on read, not deleted, so history stays available.
 */
public final class Freshness {

    private final Duration window;

    public Freshness(Duration window) {
        if (window == null || window.isNegative() || window.isZero()) {
            throw new IllegalArgumentException("freshness window must be positive");
        }
        this.window = window;
    }

    public boolean isFresh(Instant lastSeen, Instant now) {
        if (lastSeen == null || now == null) {
            return false;
        }
        return !lastSeen.isAfter(now) && Duration.between(lastSeen, now).compareTo(window) <= 0;
    }
}
