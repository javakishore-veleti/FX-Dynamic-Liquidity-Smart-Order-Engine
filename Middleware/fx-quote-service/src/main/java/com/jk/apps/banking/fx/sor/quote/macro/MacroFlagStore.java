package com.jk.apps.banking.fx.sor.quote.macro;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;

/**
 * Durable macro-flag storage. In-memory by default; Redis when {@code fx.features.redis-enabled=true}.
 */
public interface MacroFlagStore {

    record MacroFlag(
            String regime,
            String source,
            BigDecimal severity,
            Instant expiresAt,
            String armedBy) {
    }

    void save(MacroFlag flag);

    void clear();

    Optional<MacroFlag> load();
}
