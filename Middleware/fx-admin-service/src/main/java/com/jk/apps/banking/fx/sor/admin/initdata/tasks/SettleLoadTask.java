package com.jk.apps.banking.fx.sor.admin.initdata.tasks;

import com.jk.apps.banking.fx.sor.admin.service.QualifyReferenceClient;
import com.jk.apps.banking.fx.sor.admin.wf.WfTask;
import java.util.Map;
import org.springframework.stereotype.Component;

/** Settlement Policies, task 1: ask qualify to load its committed settlement seed. */
@Component
public class SettleLoadTask implements WfTask {

    private final QualifyReferenceClient qualifySvc;

    public SettleLoadTask(QualifyReferenceClient qualifySvc) {
        this.qualifySvc = qualifySvc;
    }

    @Override
    public String id() {
        return "task_init_load_settlement";
    }

    @Override
    public String run(Map<String, Object> vars) {
        Object loaded = qualifySvc.loadSettlementPolicies().get("loaded");
        vars.put("settlementLoaded", loaded);
        return "{\"loaded\":" + loaded + "}";
    }
}
