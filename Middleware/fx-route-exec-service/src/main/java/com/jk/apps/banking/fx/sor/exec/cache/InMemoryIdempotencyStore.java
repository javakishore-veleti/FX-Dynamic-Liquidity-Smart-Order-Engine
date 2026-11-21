package com.jk.apps.banking.fx.sor.exec.cache;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryIdempotencyStore implements IdempotencyStore {

    private final ConcurrentHashMap<String, IdempotencyRecord> locks = new ConcurrentHashMap<>();

    @Override
    public Optional<IdempotencyRecord> tryAcquire(String clientOrderId, IdempotencyRecord provisional) {
        IdempotencyRecord existing = locks.putIfAbsent(clientOrderId, provisional);
        return Optional.ofNullable(existing);
    }

    @Override
    public void complete(String clientOrderId, IdempotencyRecord filled) {
        locks.put(clientOrderId, filled);
    }

    @Override
    public Optional<IdempotencyRecord> get(String clientOrderId) {
        return Optional.ofNullable(locks.get(clientOrderId));
    }
}
