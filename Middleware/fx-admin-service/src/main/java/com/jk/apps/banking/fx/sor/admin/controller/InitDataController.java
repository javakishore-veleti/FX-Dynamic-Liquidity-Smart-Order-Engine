package com.jk.apps.banking.fx.sor.admin.controller;

import com.jk.apps.banking.fx.sor.admin.initdata.InitDataFacade;
import com.jk.apps.banking.fx.sor.admin.initdata.InitDataView;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/initial-data")
public class InitDataController {

    private final InitDataFacade initDataFacade;

    public InitDataController(InitDataFacade initDataFacade) {
        this.initDataFacade = initDataFacade;
    }

    @GetMapping
    public List<InitDataView> listDatasets() {
        return initDataFacade.listDatasets();
    }

    /** Runs the dataset's setup workflow once. Seed loads take no typed input; body may carry triggeredBy only. */
    @PostMapping("/{datasetKey}/setup")
    public InitDataView setup(
            @PathVariable("datasetKey") String datasetKey,
            @RequestBody(required = false) Map<String, Object> body) {
        Map<String, Object> vars = body == null ? Map.of() : body;
        String triggeredBy = vars.get("triggeredBy") == null ? "ADMIN_PORTAL" : String.valueOf(vars.get("triggeredBy"));
        return initDataFacade.setup(datasetKey, triggeredBy);
    }
}
