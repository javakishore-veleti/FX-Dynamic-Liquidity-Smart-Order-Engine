package com.jk.apps.banking.fx.sor.admin.initdata;

import java.util.List;
import java.util.Optional;

/** Initial Data datasets. Each dataset is loaded once, by its setup workflow, from a button click. */
public final class InitDataCatalog {

    /** workflows.purpose value that marks a seed workflow. */
    public static final String PURPOSE = "INITIAL_DATA_SETUP";
    public static final String PAIRS_WORKFLOW = "INIT_CURRENCY_PAIRS";
    public static final String SETTLEMENT_WORKFLOW = "INIT_SETTLEMENT_POLICIES";
    public static final String CALENDAR_WORKFLOW = "INIT_ECONOMIC_CALENDAR";
    public static final String CALENDAR_RESOURCE = "initial-data/economic-calendar.json";

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
                    "CURRENCY_PAIRS",
                    "Currency Pairs",
                    "28-pair master: G10 majors, G10 crosses, EM and restricted pairs. Owner: quote.",
                    "fx-quote-service",
                    PAIRS_WORKFLOW,
                    "fx-quote-service master/currency-pairs.json"),
            new Dataset(
                    "SETTLEMENT_POLICIES",
                    "Settlement Policies",
                    "SPOT and TOM cutoffs, clip limits, CLS and restricted flags per pair. SYNTHETIC seed until business values arrive. Owner: qualify.",
                    "fx-qualify-service",
                    SETTLEMENT_WORKFLOW,
                    "fx-qualify-service master/settlement-policies.synthetic.json"),
            new Dataset(
                    "ECONOMIC_CALENDAR",
                    "Economic Calendar",
                    "FOMC, NFP and ECB announcement windows used by FR-01 macro guardrails",
                    "fx-quote-service",
                    CALENDAR_WORKFLOW,
                    CALENDAR_RESOURCE));

    private InitDataCatalog() {
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
