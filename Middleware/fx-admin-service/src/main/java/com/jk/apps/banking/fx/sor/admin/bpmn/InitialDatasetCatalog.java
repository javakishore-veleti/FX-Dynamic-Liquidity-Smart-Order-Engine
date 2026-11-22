package com.jk.apps.banking.fx.sor.admin.bpmn;

import java.util.List;
import java.util.Optional;

/** Initial Data datasets. Each dataset is loaded once, by its setup workflow, from a button click. */
public final class InitialDatasetCatalog {

    public static final String ECONOMIC_CALENDAR_WORKFLOW = "INIT_ECONOMIC_CALENDAR";
    public static final String ECONOMIC_CALENDAR_RESOURCE = "initial-data/economic-calendar.json";

    public record Dataset(
            String key,
            String title,
            String description,
            String ownerService,
            String workflowKey,
            String seedResource) {
    }

    private static final List<Dataset> DATASETS = List.of(
            new Dataset(
                    "ECONOMIC_CALENDAR",
                    "Economic Calendar",
                    "FOMC, NFP and ECB announcement windows used by FR-01 macro guardrails",
                    "fx-quote-service",
                    ECONOMIC_CALENDAR_WORKFLOW,
                    ECONOMIC_CALENDAR_RESOURCE));

    private InitialDatasetCatalog() {
    }

    public static List<Dataset> all() {
        return DATASETS;
    }

    public static Optional<Dataset> byKey(String datasetKey) {
        return DATASETS.stream().filter(d -> d.key().equals(datasetKey)).findFirst();
    }

    public static boolean isInitialDataWorkflow(String workflowKey) {
        return workflowKey != null && workflowKey.startsWith("INIT_");
    }
}
