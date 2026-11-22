package com.jk.apps.banking.fx.sor.quote.business.facade;

import com.jk.apps.banking.fx.sor.quote.macro.MacroFlagStore;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import org.springframework.stereotype.Component;

/**
 * CLOSED business layer — active macro / news regime override (FOMC, NFP, ECB).
 * Backed by {@link MacroFlagStore} (in-memory or Redis). Calendar auto-arm uses armedBy=CALENDAR.
 */
@Component
public class MacroRegimeFacade {

    public static final String MACRO_NEWS_HIGH = "MACRO_NEWS_HIGH";
    public static final String ARMED_BY_MANUAL = "MANUAL";
    public static final String ARMED_BY_CALENDAR = "CALENDAR";
    public static final String ARMED_BY_AGENT = "AGENT";

    public record MacroFlag(
            String regime,
            String source,
            BigDecimal severity,
            Instant expiresAt,
            String armedBy) {
    }

    private final MacroFlagStore store;

    public MacroRegimeFacade(MacroFlagStore store) {
        this.store = store;
    }

    public void ingest(String source, BigDecimal severity, long ttlSeconds) {
        ingest(source, severity, ttlSeconds, ARMED_BY_MANUAL);
    }

    public void ingest(String source, BigDecimal severity, long ttlSeconds, String armedBy) {
        String src = source == null || source.isBlank() ? "MACRO_FEED" : source.toUpperCase();
        BigDecimal sev = severity == null ? new BigDecimal("1.25") : severity;
        long ttl = ttlSeconds <= 0 ? 3600 : ttlSeconds;
        String by = armedBy == null || armedBy.isBlank() ? ARMED_BY_MANUAL : armedBy;
        store.save(new MacroFlagStore.MacroFlag(
                MACRO_NEWS_HIGH, src, sev, Instant.now().plusSeconds(ttl), by));
    }

    public void clear() {
        store.clear();
    }

    /**
     * Clear only if currently armed by calendar (manual/agent overrides are sticky until explicit clear).
     */
    public void clearIfCalendar() {
        current().ifPresent(flag -> {
            if (ARMED_BY_CALENDAR.equals(flag.armedBy())) {
                store.clear();
            }
        });
    }

    public Optional<MacroFlag> current() {
        Optional<MacroFlagStore.MacroFlag> loaded = store.load();
        if (loaded.isEmpty()) {
            return Optional.empty();
        }
        MacroFlagStore.MacroFlag flag = loaded.get();
        if (flag.expiresAt().isBefore(Instant.now())) {
            store.clear();
            return Optional.empty();
        }
        return Optional.of(new MacroFlag(
                flag.regime(),
                flag.source(),
                flag.severity(),
                flag.expiresAt(),
                flag.armedBy() == null || flag.armedBy().isBlank() ? ARMED_BY_MANUAL : flag.armedBy()));
    }

    public boolean isMacroActive() {
        return current().isPresent();
    }
}
