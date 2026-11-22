package com.jk.apps.banking.fx.sor.quote.cache;

import com.jk.apps.banking.fx.sor.quote.entity.ClientTierSpreadSchedule;
import com.jk.apps.banking.fx.sor.quote.repository.ClientTierSpreadScheduleRepository;
import jakarta.annotation.PostConstruct;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

/**
 * L1 schedule cache — zero hot-path SQL after warm/reload (FR-01).
 */
@Component
public class SpreadScheduleCache {

    private final ClientTierSpreadScheduleRepository repository;
    private final Map<String, ClientTierSpreadSchedule> byKey = new ConcurrentHashMap<>();

    public SpreadScheduleCache(ClientTierSpreadScheduleRepository repository) {
        this.repository = repository;
    }

    @PostConstruct
    public void warm() {
        reload();
    }

    public void reload() {
        byKey.clear();
        List<ClientTierSpreadSchedule> all = repository.findAll();
        for (ClientTierSpreadSchedule row : all) {
            byKey.put(key(row.getTierId(), row.getCcyPair(), row.getSessionRegime()), row);
        }
    }

    public void invalidate() {
        reload();
    }

    public Optional<ClientTierSpreadSchedule> find(String tierId, String ccyPair, String sessionRegime) {
        return Optional.ofNullable(byKey.get(key(tierId, ccyPair, sessionRegime)));
    }

    public List<ClientTierSpreadSchedule> listAll() {
        return List.copyOf(byKey.values());
    }

    public void put(ClientTierSpreadSchedule row) {
        byKey.put(key(row.getTierId(), row.getCcyPair(), row.getSessionRegime()), row);
    }

    private static String key(String tierId, String ccyPair, String sessionRegime) {
        return tierId + "|" + ccyPair + "|" + sessionRegime;
    }
}
