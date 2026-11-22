package com.jk.apps.banking.fx.sor.admin.initdata.tasks;

import com.jk.apps.banking.fx.sor.admin.service.QuoteScheduleClient;
import com.jk.apps.banking.fx.sor.admin.wf.WfTask;
import java.util.Map;
import org.springframework.stereotype.Component;

/** Currency Pairs, task 1: ask quote to load its committed master file. */
@Component
public class PairLoadTask implements WfTask {

    private final QuoteScheduleClient quoteSvc;

    public PairLoadTask(QuoteScheduleClient quoteSvc) {
        this.quoteSvc = quoteSvc;
    }

    @Override
    public String id() {
        return "task_init_load_pairs";
    }

    @Override
    public String run(Map<String, Object> vars) {
        String actor = String.valueOf(vars.getOrDefault("triggeredBy", "ADMIN_WORKFLOW"));
        Map<String, Object> result = quoteSvc.loadCurrencyPairs(actor);
        Object loaded = result.get("loaded");
        vars.put("pairsLoaded", loaded);
        return "{\"loaded\":" + loaded + "}";
    }
}
