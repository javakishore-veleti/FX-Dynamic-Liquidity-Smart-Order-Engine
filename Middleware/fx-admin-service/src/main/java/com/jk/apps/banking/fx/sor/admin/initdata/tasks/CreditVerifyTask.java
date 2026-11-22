package com.jk.apps.banking.fx.sor.admin.initdata.tasks;

import com.jk.apps.banking.fx.sor.admin.service.QualifyReferenceClient;
import com.jk.apps.banking.fx.sor.admin.wf.WfTask;
import java.util.Map;
import org.springframework.stereotype.Component;

/**
 * Credit Lines, task 2: qualify must hold at least the lines just loaded.
 * "At least" because runtime overrides may add lines after the load.
 */
@Component
public class CreditVerifyTask implements WfTask {

    private final QualifyReferenceClient qualifySvc;

    public CreditVerifyTask(QualifyReferenceClient qualifySvc) {
        this.qualifySvc = qualifySvc;
    }

    @Override
    public String id() {
        return "task_init_verify_credit";
    }

    @Override
    public String run(Map<String, Object> vars) {
        int expected = ((Number) vars.getOrDefault("creditLoaded", 0)).intValue();
        int found = qualifySvc.listCreditLines().size();
        if (found < expected) {
            throw new IllegalStateException("credit verify failed: loaded " + expected + ", qualify holds " + found);
        }
        vars.put("creditVerified", found);
        return "{\"expectedAtLeast\":" + expected + ",\"found\":" + found + "}";
    }
}
