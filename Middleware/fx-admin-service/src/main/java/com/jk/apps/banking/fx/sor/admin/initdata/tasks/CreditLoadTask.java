package com.jk.apps.banking.fx.sor.admin.initdata.tasks;

import com.jk.apps.banking.fx.sor.admin.service.QualifyReferenceClient;
import com.jk.apps.banking.fx.sor.admin.wf.WfTask;
import java.util.Map;
import org.springframework.stereotype.Component;

/** Credit Lines, task 1: ask qualify to load its committed credit-line seed. */
@Component
public class CreditLoadTask implements WfTask {

    private final QualifyReferenceClient qualifySvc;

    public CreditLoadTask(QualifyReferenceClient qualifySvc) {
        this.qualifySvc = qualifySvc;
    }

    @Override
    public String id() {
        return "task_init_load_credit";
    }

    @Override
    public String run(Map<String, Object> vars) {
        Object loaded = qualifySvc.loadCreditLines().get("loaded");
        vars.put("creditLoaded", loaded);
        return "{\"loaded\":" + loaded + "}";
    }
}
