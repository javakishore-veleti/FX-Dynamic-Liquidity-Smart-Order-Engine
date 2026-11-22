package com.jk.apps.banking.fx.sor.admin.initdata.tasks;

import com.jk.apps.banking.fx.sor.admin.service.QuoteScheduleClient;
import com.jk.apps.banking.fx.sor.admin.wf.WfTask;
import java.util.Map;
import org.springframework.stereotype.Component;

/** LP Rankings, task 2: route-exec must hold exactly the rankings just loaded (the load replaces the set). */
@Component
public class LpRankVerifyTask implements WfTask {

    private final QuoteScheduleClient routeSvc;

    public LpRankVerifyTask(QuoteScheduleClient routeSvc) {
        this.routeSvc = routeSvc;
    }

    @Override
    public String id() {
        return "task_init_verify_lprank";
    }

    @Override
    public String run(Map<String, Object> vars) {
        int expected = ((Number) vars.getOrDefault("lpRankLoaded", 0)).intValue();
        int found = routeSvc.listLpRankings().size();
        if (found != expected) {
            throw new IllegalStateException("lp rank verify failed: loaded " + expected + ", route-exec holds " + found);
        }
        vars.put("lpRankVerified", found);
        return "{\"expected\":" + expected + ",\"found\":" + found + "}";
    }
}
