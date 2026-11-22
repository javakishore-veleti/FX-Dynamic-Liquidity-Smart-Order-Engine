package com.jk.apps.banking.fx.sor.topology.service;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Premise P7 (Netflix TechBlog): backpressure, not silent loss. The queue between ingest and write
 * has a fixed capacity. When it is full, the producer is told to pause (offer returns false) and the
 * refusal is counted. Nothing is dropped without being counted.
 */
public final class IngestQueue<T> {

    private final BlockingQueue<T> queue;
    private final AtomicLong refused = new AtomicLong();

    public IngestQueue(int capacity) {
        if (capacity <= 0) {
            throw new IllegalArgumentException("capacity must be positive");
        }
        this.queue = new ArrayBlockingQueue<>(capacity);
    }

    /** Returns false when full. The caller must slow down or retry, and the refusal is counted. */
    public boolean offer(T item) {
        boolean accepted = queue.offer(item);
        if (!accepted) {
            refused.incrementAndGet();
        }
        return accepted;
    }

    public T poll() {
        return queue.poll();
    }

    public int size() {
        return queue.size();
    }

    public long refusedCount() {
        return refused.get();
    }
}
