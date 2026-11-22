package com.jk.apps.banking.fx.sor.quote.business.facade;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jk.apps.banking.fx.sor.quote.dto.EconomicCalendarEventDto;
import jakarta.annotation.PostConstruct;
import java.io.InputStream;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

/**
 * CLOSED — published FOMC/NFP/ECB windows from classpath JSON (visible via API / Admin portal).
 */
@Component
public class EconomicCalendarFacade {

    private static final Logger log = LoggerFactory.getLogger(EconomicCalendarFacade.class);

    private final ObjectMapper objectMapper;
    private final Clock clock;
    private volatile List<CalendarWindow> windows = List.of();

    @Autowired
    public EconomicCalendarFacade(ObjectMapper objectMapper) {
        this(objectMapper, Clock.systemUTC());
    }

    EconomicCalendarFacade(ObjectMapper objectMapper, Clock clock) {
        this.objectMapper = objectMapper;
        this.clock = clock;
    }

    @PostConstruct
    void loadCalendar() {
        try (InputStream in = new ClassPathResource("fr01/economic-calendar.json").getInputStream()) {
            List<RawEvent> raw = objectMapper.readValue(in, new TypeReference<>() {
            });
            List<CalendarWindow> loaded = new ArrayList<>();
            for (RawEvent e : raw) {
                loaded.add(new CalendarWindow(
                        e.source().toUpperCase(),
                        e.severity() == null ? new BigDecimal("1.25") : e.severity(),
                        Instant.parse(e.start()),
                        Instant.parse(e.end()),
                        e.label() == null ? e.source() : e.label()));
            }
            loaded.sort(Comparator.comparing(CalendarWindow::start));
            windows = List.copyOf(loaded);
            log.info("FR-01 economic calendar loaded: {} windows", windows.size());
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to load fr01/economic-calendar.json", ex);
        }
    }

    public List<EconomicCalendarEventDto> listEvents() {
        Instant now = clock.instant();
        List<EconomicCalendarEventDto> out = new ArrayList<>();
        for (CalendarWindow w : windows) {
            out.add(new EconomicCalendarEventDto(
                    w.source(),
                    w.severity(),
                    w.start(),
                    w.end(),
                    w.label(),
                    !now.isBefore(w.start()) && now.isBefore(w.end())));
        }
        return out;
    }

    public Optional<CalendarWindow> activeWindow() {
        Instant now = clock.instant();
        return windows.stream()
                .filter(w -> !now.isBefore(w.start()) && now.isBefore(w.end()))
                .max(Comparator.comparing(CalendarWindow::severity));
    }

    public record CalendarWindow(
            String source, BigDecimal severity, Instant start, Instant end, String label) {
    }

    private record RawEvent(String source, BigDecimal severity, String start, String end, String label) {
    }
}
