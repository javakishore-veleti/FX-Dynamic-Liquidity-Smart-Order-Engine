package com.jk.apps.banking.fx.sor.admin.initdata.tasks;

import com.jk.apps.banking.fx.sor.admin.service.QuoteScheduleClient;
import com.jk.apps.banking.fx.sor.admin.wf.WfTask;
import java.util.Map;
import org.springframework.stereotype.Component;

/** LP Top-of-Book, task 2: quote must hold at least the rows just loaded. */
@Component
public class LpBookVerifyTask implements WfTask {

    private final QuoteScheduleClient quoteSvc;

    public LpBookVerifyTask(QuoteScheduleClient quoteSvc) {
        this.quoteSvc = quoteSvc;
    }

    @Override
    public String id() {
        return "task_init_verify_lpbook";
    }

    @Override
    public String run(Map<String, Object> vars) {
        int expected = ((Number) vars.getOrDefault("lpBookLoaded", 0)).intValue();
        int found = quoteSvc.listLpBook().size();
        if (found < expected) {
            throw new IllegalStateException("lp book verify failed: loaded " + expected + ", quote holds " + found);
        }
        vars.put("lpBookVerified", found);
        return "{\"expectedAtLeast\":" + expected + ",\"found\":" + found + "}";
    }
}
