package com.jk.apps.banking.fx.sor.topology.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collection;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;

/**
 * Premise P6 (Netflix TechBlog): consistent hashing with dynamic membership. Each instance owns a
 * share of the key space. When an instance joins or leaves, only the keys next to it on the ring move.
 * Virtual nodes spread each instance across the ring so the shares stay even.
 *
 * Pure function over a membership list. Every instance computes the same owner from the same list.
 */
public final class HashRing {

    private static final int VIRTUAL_NODES = 128;
    private final NavigableMap<Long, String> ring = new TreeMap<>();

    public HashRing(Collection<String> instances) {
        for (String instance : instances) {
            for (int i = 0; i < VIRTUAL_NODES; i++) {
                ring.put(hash(instance + "#" + i), instance);
            }
        }
        if (ring.isEmpty()) {
            throw new IllegalArgumentException("ring needs at least one instance");
        }
    }

    /** The instance that owns this key. */
    public String owner(String key) {
        long h = hash(key);
        Map.Entry<Long, String> e = ring.ceilingEntry(h);
        return (e != null ? e : ring.firstEntry()).getValue();
    }

    static long hash(String s) {
        try {
            byte[] d = MessageDigest.getInstance("SHA-256").digest(s.getBytes(StandardCharsets.UTF_8));
            long v = 0;
            for (int i = 0; i < 8; i++) {
                v = (v << 8) | (d[i] & 0xff);
            }
            return v;
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException(ex);
        }
    }
}
