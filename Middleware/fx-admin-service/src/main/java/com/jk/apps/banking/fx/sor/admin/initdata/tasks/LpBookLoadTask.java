package com.jk.apps.banking.fx.sor.admin.initdata.tasks;

import com.jk.apps.banking.fx.sor.admin.service.QuoteScheduleClient;
import com.jk.apps.banking.fx.sor.admin.wf.WfTask;
import java.util.Map;
import org.springframework.stereotype.Component;

/** LP Top-of-Book, task 1: ask quote to load its committed starter book. */
@Component
public class LpBookLoadTask implements WfTask {

    private final QuoteScheduleClient quoteSvc;

    public LpBookLoadTask(QuoteScheduleClient quoteSvc) {
        this.quoteSvc = quoteSvc;
    }

    @Override
    public String id() {
        return "task_init_load_lpbook";
    }

    @Override
    public String run(Map<String, Object> vars) {
        Object loaded = quoteSvc.loadLpBook().get("loaded");
        vars.put("lpBookLoaded", loaded);
        return "{\"loaded\":" + loaded + "}";
    }
}
