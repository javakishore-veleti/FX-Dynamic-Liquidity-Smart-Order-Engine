package com.jk.apps.banking.fx.sor.qual.repository;

import com.jk.apps.banking.fx.sor.qual.entity.SettlementCutoffPolicy;
import com.jk.apps.banking.fx.sor.qual.entity.SettlementCutoffPolicyId;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SettlementCutoffPolicyRepository
        extends JpaRepository<SettlementCutoffPolicy, SettlementCutoffPolicyId> {

    Optional<SettlementCutoffPolicy> findByCcyPairAndTenor(String ccyPair, String tenor);
}
