package com.jk.apps.banking.fx.sor.qual.cache;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface CreditStore {

    Optional<CreditLine> get(String clientId, String lpId);

    void put(CreditLine line);

    boolean tryReserve(String clientId, String lpId, BigDecimal notionalUsd);

    List<CreditLine> listAll();

    void delete(String clientId, String lpId);
}
