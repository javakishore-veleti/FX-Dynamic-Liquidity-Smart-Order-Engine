package com.jk.apps.banking.fx.sor.admin.initdata.tasks;

import com.jk.apps.banking.fx.sor.admin.service.QualifyReferenceClient;
import com.jk.apps.banking.fx.sor.admin.wf.WfTask;
import java.util.Map;
import org.springframework.stereotype.Component;

/** Settlement Policies, task 2: qualify must hold exactly the number of rows just loaded. */
@Component
public class SettleVerifyTask implements WfTask {

    private final QualifyReferenceClient qualifySvc;

    public SettleVerifyTask(QualifyReferenceClient qualifySvc) {
        this.qualifySvc = qualifySvc;
    }

    @Override
    public String id() {
        return "task_init_verify_settlement";
    }

    @Override
    public String run(Map<String, Object> vars) {
        int expected = ((Number) vars.getOrDefault("settlementLoaded", 0)).intValue();
        int found = qualifySvc.listSettlementPolicies().size();
        if (found != expected) {
            throw new IllegalStateException("settlement verify failed: loaded " + expected + ", qualify holds " + found);
        }
        vars.put("settlementVerified", found);
        return "{\"expected\":" + expected + ",\"found\":" + found + "}";
    }
}
