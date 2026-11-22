package com.jk.apps.banking.fx.sor.exec.regime;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import org.springframework.stereotype.Component;

/** L1 cache of last Kafka / published last-look penalty hint (visible in provenance). */
@Component
public class LastLookPenaltyHintCache {

    public record Hint(String sessionRegime, BigDecimal lastLookPenaltyWeight) {
    }

    private final AtomicReference<Hint> latest = new AtomicReference<>();

    public void update(String sessionRegime, BigDecimal weight) {
        latest.set(new Hint(
                sessionRegime == null ? "LONDON_NY_OVERLAP" : sessionRegime,
                weight == null ? BigDecimal.ONE : weight));
    }

    public Optional<Hint> current() {
        return Optional.ofNullable(latest.get());
    }
}
