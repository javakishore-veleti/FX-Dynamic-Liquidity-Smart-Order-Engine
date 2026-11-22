package com.jk.apps.banking.fx.sor.admin.controller;

import com.jk.apps.banking.fx.sor.admin.dto.InitialDatasetView;
import com.jk.apps.banking.fx.sor.admin.service.InitialDataFacade;
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
public class InitialDataController {

    private final InitialDataFacade initialDataFacade;

    public InitialDataController(InitialDataFacade initialDataFacade) {
        this.initialDataFacade = initialDataFacade;
    }

    @GetMapping
    public List<InitialDatasetView> listDatasets() {
        return initialDataFacade.listDatasets();
    }

    /** Button click: runs the dataset's setup workflow once. Body: {"reason": "...", "triggeredBy": "..."}. */
    @PostMapping("/{datasetKey}/setup")
    public InitialDatasetView setup(
            @PathVariable("datasetKey") String datasetKey,
            @RequestBody(required = false) Map<String, Object> body) {
        Map<String, Object> vars = body == null ? Map.of() : body;
        String reason = vars.get("reason") == null ? "" : String.valueOf(vars.get("reason"));
        String triggeredBy = vars.get("triggeredBy") == null ? "ADMIN_PORTAL" : String.valueOf(vars.get("triggeredBy"));
        return initialDataFacade.setup(datasetKey, reason, triggeredBy);
    }
}
