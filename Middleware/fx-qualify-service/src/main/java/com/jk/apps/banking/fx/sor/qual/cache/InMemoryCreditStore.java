package com.jk.apps.banking.fx.sor.qual.cache;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

public class InMemoryCreditStore implements CreditStore {

    private final ConcurrentHashMap<String, CreditLine> lines = new ConcurrentHashMap<>();

    private static String key(String clientId, String lpId) {
        return clientId + ":" + lpId;
    }

    @Override
    public Optional<CreditLine> get(String clientId, String lpId) {
        return Optional.ofNullable(lines.get(key(clientId, lpId)));
    }

    @Override
    public void put(CreditLine line) {
        lines.put(key(line.getClientId(), line.getLpId()), line);
    }

    @Override
    public boolean tryReserve(String clientId, String lpId, BigDecimal notionalUsd) {
        AtomicBoolean reserved = new AtomicBoolean(false);
        lines.computeIfPresent(key(clientId, lpId), (ignored, existing) -> {
            if (existing.headroom().compareTo(notionalUsd) >= 0) {
                existing.setReservedUsd(existing.getReservedUsd().add(notionalUsd));
                reserved.set(true);
            }
            return existing;
        });
        return reserved.get();
    }

    @Override
    public List<CreditLine> listAll() {
        return List.copyOf(lines.values());
    }

    @Override
    public void delete(String clientId, String lpId) {
        lines.remove(key(clientId, lpId));
    }
}
