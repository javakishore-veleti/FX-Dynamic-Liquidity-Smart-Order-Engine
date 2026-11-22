package com.jk.apps.banking.fx.sor.topology.service;

import java.util.List;

/**
 * Decides whether this instance owns a message key, using the consistent hash ring (P6).
 * With a single instance in the membership list, every key is owned locally.
 */
public final class IngestPartition {

    private final String self;
    private final HashRing ring;

    public IngestPartition(String self, List<String> members) {
        this.self = self;
        this.ring = new HashRing(members.isEmpty() ? List.of(self) : members);
    }

    public boolean owns(String key) {
        return self.equals(ring.owner(key));
    }
}
