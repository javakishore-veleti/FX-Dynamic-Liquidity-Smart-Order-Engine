package com.jk.apps.banking.fx.sor.topology.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class IngestQueueTest {

    @Test
    void refusesWhenFullAndCountsEachRefusal() {
        IngestQueue<String> q = new IngestQueue<>(2);
        assertTrue(q.offer("a"));
        assertTrue(q.offer("b"));
        assertFalse(q.offer("c"));
        assertFalse(q.offer("d"));
        assertEquals(2, q.refusedCount());
        assertEquals(2, q.size());
    }

    @Test
    void acceptsAgainAfterDrain() {
        IngestQueue<String> q = new IngestQueue<>(1);
        assertTrue(q.offer("a"));
        assertEquals("a", q.poll());
        assertTrue(q.offer("b"));
    }
}
