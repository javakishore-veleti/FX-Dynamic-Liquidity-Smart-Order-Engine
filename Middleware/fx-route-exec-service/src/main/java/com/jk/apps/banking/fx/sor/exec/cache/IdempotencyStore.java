package com.jk.apps.banking.fx.sor.exec.cache;

import java.util.Optional;

public interface IdempotencyStore {

    /**
     * @return empty if lock acquired; existing record if already locked/filled
     */
    Optional<IdempotencyRecord> tryAcquire(String clientOrderId, IdempotencyRecord provisional);

    void complete(String clientOrderId, IdempotencyRecord filled);

    Optional<IdempotencyRecord> get(String clientOrderId);
}
