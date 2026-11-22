package com.jk.apps.banking.fx.sor.quote.business.facade;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

class EconomicCalendarFacadeTest {

    @Test
    void loadsPublishedCalendarWindows() {
        EconomicCalendarFacade facade = new EconomicCalendarFacade(new ObjectMapper(), null, java.time.Clock.systemUTC());
        facade.loadCalendar();
        assertFalse(facade.listEvents().isEmpty());
        assertTrue(facade.listEvents().stream()
                .anyMatch(e -> "FOMC".equals(e.getSource()) || "NFP".equals(e.getSource())));
    }
}
