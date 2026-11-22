package com.jk.apps.banking.fx.sor.admin.initdata.tasks;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jk.apps.banking.fx.sor.admin.initdata.InitDataCatalog;
import com.jk.apps.banking.fx.sor.admin.service.QuoteScheduleClient;
import com.jk.apps.banking.fx.sor.admin.wf.WfTask;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

/** Initial Data · Economic Calendar, task 1: upsert every seed row into fx-quote-service. */
@Component
public class CalLoadTask implements WfTask {

    private final QuoteScheduleClient quoteSvc;
    private final ObjectMapper mapper;

    public CalLoadTask(QuoteScheduleClient quoteSvc, ObjectMapper mapper) {
        this.quoteSvc = quoteSvc;
        this.mapper = mapper;
    }

    @Override
    public String id() {
        return "task_init_load_calendar";
    }

    @Override
    public String run(Map<String, Object> vars) {
        List<Map<String, Object>> events = readSeed(InitDataCatalog.CALENDAR_RESOURCE);
        int upserted = 0;
        for (Map<String, Object> event : events) {
            quoteSvc.upsertEconomicCalendar(event);
            upserted++;
        }
        vars.put("calendarRowsLoaded", upserted);
        return toJson(Map.of("source", InitDataCatalog.CALENDAR_RESOURCE, "rowsUpserted", upserted));
    }

    private List<Map<String, Object>> readSeed(String resource) {
        try (InputStream in = new ClassPathResource(resource).getInputStream()) {
            return mapper.readValue(in, new TypeReference<>() {
            });
        } catch (IOException ex) {
            throw new IllegalStateException("cannot read seed resource " + resource, ex);
        }
    }

    private String toJson(Object value) {
        try {
            return mapper.writeValueAsString(value);
        } catch (Exception ex) {
            return String.valueOf(value);
        }
    }
}
