package com.jk.apps.banking.fx.sor.quote.repository;

import com.jk.apps.banking.fx.sor.quote.entity.ClientTierSpreadSchedule;
import com.jk.apps.banking.fx.sor.quote.entity.ClientTierSpreadScheduleId;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClientTierSpreadScheduleRepository
        extends JpaRepository<ClientTierSpreadSchedule, ClientTierSpreadScheduleId> {

    List<ClientTierSpreadSchedule> findByCcyPair(String ccyPair);

    Optional<ClientTierSpreadSchedule> findByTierIdAndCcyPairAndSessionRegime(
            String tierId, String ccyPair, String sessionRegime);
}
