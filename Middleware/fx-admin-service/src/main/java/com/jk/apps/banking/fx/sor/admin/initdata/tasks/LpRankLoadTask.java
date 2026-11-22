package com.jk.apps.banking.fx.sor.admin.initdata.tasks;

import com.jk.apps.banking.fx.sor.admin.service.QuoteScheduleClient;
import com.jk.apps.banking.fx.sor.admin.wf.WfTask;
import java.util.Map;
import org.springframework.stereotype.Component;

/** LP Rankings, task 1: ask route-exec to load its committed ranking starter. */
@Component
public class LpRankLoadTask implements WfTask {

    private final QuoteScheduleClient routeSvc;

    public LpRankLoadTask(QuoteScheduleClient routeSvc) {
        this.routeSvc = routeSvc;
    }

    @Override
    public String id() {
        return "task_init_load_lprank";
    }

    @Override
    public String run(Map<String, Object> vars) {
        Object loaded = routeSvc.loadLpRankings().get("loaded");
        vars.put("lpRankLoaded", loaded);
        return "{\"loaded\":" + loaded + "}";
    }
}
