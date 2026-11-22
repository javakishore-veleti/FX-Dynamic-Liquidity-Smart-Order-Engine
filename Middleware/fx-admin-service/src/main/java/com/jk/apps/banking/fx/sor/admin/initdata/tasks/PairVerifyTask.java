package com.jk.apps.banking.fx.sor.admin.initdata.tasks;

import com.jk.apps.banking.fx.sor.admin.service.QuoteScheduleClient;
import com.jk.apps.banking.fx.sor.admin.wf.WfTask;
import java.util.Map;
import org.springframework.stereotype.Component;

/** Currency Pairs, task 2: the pair count in quote must equal the number just loaded. */
@Component
public class PairVerifyTask implements WfTask {

    private final QuoteScheduleClient quoteSvc;

    public PairVerifyTask(QuoteScheduleClient quoteSvc) {
        this.quoteSvc = quoteSvc;
    }

    @Override
    public String id() {
        return "task_init_verify_pairs";
    }

    @Override
    public String run(Map<String, Object> vars) {
        int expected = ((Number) vars.getOrDefault("pairsLoaded", 0)).intValue();
        int found = quoteSvc.listCurrencyPairs().size();
        if (found != expected) {
            throw new IllegalStateException(
                    "pair verify failed: loaded " + expected + ", quote holds " + found);
        }
        vars.put("pairsVerified", found);
        return "{\"expected\":" + expected + ",\"found\":" + found + "}";
    }
}
