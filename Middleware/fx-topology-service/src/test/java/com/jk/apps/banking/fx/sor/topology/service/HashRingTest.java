package com.jk.apps.banking.fx.sor.topology.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class HashRingTest {

    private static List<String> keys(int n) {
        List<String> k = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            k.add("svc-" + i);
        }
        return k;
    }

    @Test
    void sharesAreRoughlyEven() {
        HashRing ring = new HashRing(List.of("i1", "i2", "i3"));
        int[] count = new int[3];
        List<String> names = List.of("i1", "i2", "i3");
        for (String key : keys(3000)) {
            count[names.indexOf(ring.owner(key))]++;
        }
        for (int c : count) {
            assertTrue(c > 700 && c < 1300, "share out of range: " + c);
        }
    }

    @Test
    void addingAnInstanceMovesOnlyAMinority() {
        HashRing before = new HashRing(List.of("i1", "i2", "i3"));
        HashRing after = new HashRing(List.of("i1", "i2", "i3", "i4"));
        int moved = 0;
        List<String> all = keys(3000);
        for (String key : all) {
            if (!before.owner(key).equals(after.owner(key))) {
                moved++;
            }
        }
        assertTrue(moved < all.size() / 2, "too many keys moved: " + moved);
    }

    @Test
    void sameMembershipGivesSameOwner() {
        assertEquals(new HashRing(List.of("a", "b")).owner("x"), new HashRing(List.of("b", "a")).owner("x"));
    }

    @Test
    void singleInstanceOwnsEverything() {
        IngestPartition p = new IngestPartition("i1", List.of());
        assertTrue(p.owns("anything"));
    }
}
