package com.jk.apps.banking.fx.sor.quote.business.facade;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import org.springframework.stereotype.Component;

/**
 * CLOSED business layer — active macro / news regime override (FOMC, NFP, ECB).
 */
@Component
public class MacroRegimeFacade {

    public static final String MACRO_NEWS_HIGH = "MACRO_NEWS_HIGH";

    public record MacroFlag(
            String regime,
            String source,
            BigDecimal severity,
            Instant expiresAt) {
    }

    private final AtomicReference<MacroFlag> active = new AtomicReference<>();

    public void ingest(String source, BigDecimal severity, long ttlSeconds) {
        String src = source == null || source.isBlank() ? "MACRO_FEED" : source.toUpperCase();
        BigDecimal sev = severity == null ? new BigDecimal("1.25") : severity;
        long ttl = ttlSeconds <= 0 ? 3600 : ttlSeconds;
        active.set(new MacroFlag(MACRO_NEWS_HIGH, src, sev, Instant.now().plusSeconds(ttl)));
    }

    public void clear() {
        active.set(null);
    }

    public Optional<MacroFlag> current() {
        MacroFlag flag = active.get();
        if (flag == null) {
            return Optional.empty();
        }
        if (flag.expiresAt().isBefore(Instant.now())) {
            active.compareAndSet(flag, null);
            return Optional.empty();
        }
        return Optional.of(flag);
    }

    public boolean isMacroActive() {
        return current().isPresent();
    }
}
