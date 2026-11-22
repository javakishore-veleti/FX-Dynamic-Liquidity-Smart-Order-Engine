package com.jk.apps.banking.fx.sor.admin.initdata.tasks;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jk.apps.banking.fx.sor.admin.service.QuoteScheduleClient;
import com.jk.apps.banking.fx.sor.admin.wf.WfTask;
import java.util.Map;
import org.springframework.stereotype.Component;

/** Initial Data · Economic Calendar, task 2: confirm fx-quote-service holds at least the loaded rows. */
@Component
public class CalVerifyTask implements WfTask {

    private final QuoteScheduleClient quoteSvc;
    private final ObjectMapper mapper;

    public CalVerifyTask(QuoteScheduleClient quoteSvc, ObjectMapper mapper) {
        this.quoteSvc = quoteSvc;
        this.mapper = mapper;
    }

    @Override
    public String id() {
        return "task_init_verify_calendar";
    }

    @Override
    public String run(Map<String, Object> vars) {
        int expected = ((Number) vars.getOrDefault("calendarRowsLoaded", 0)).intValue();
        int found = quoteSvc.listEconomicCalendar().size();
        if (found < expected) {
            throw new IllegalStateException(
                    "calendar verify failed: expected at least " + expected + " rows, quote has " + found);
        }
        vars.put("calendarRowsVerified", found);
        try {
            return mapper.writeValueAsString(Map.of("expectedAtLeast", expected, "foundInQuote", found));
        } catch (Exception ex) {
            return "verified";
        }
    }
}
