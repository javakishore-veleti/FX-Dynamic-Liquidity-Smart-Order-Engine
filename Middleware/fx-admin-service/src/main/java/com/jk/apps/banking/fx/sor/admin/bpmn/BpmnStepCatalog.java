package com.jk.apps.banking.fx.sor.admin.bpmn;

import com.jk.apps.banking.fx.sor.admin.initdata.InitDataCatalog;
import java.util.List;
import java.util.Map;

/** Step catalogs keyed by workflows.workflow_key (master list). */
public final class BpmnStepCatalog {

    public enum TaskType {
        SERVICE,
        USER,
        GATEWAY
    }

    public record Step(String taskId, String name, TaskType type) {
    }

    private static final List<Step> FR01_ADMIN = List.of(
            new Step("task_detect_session", "Detect session regime (clock)", TaskType.SERVICE),
            new Step("task_scan_calendar", "Scan economic calendar (FOMC/NFP/ECB)", TaskType.SERVICE),
            new Step("gateway_macro", "Macro window active?", TaskType.GATEWAY),
            new Step("task_confirm_macro", "Desk confirm arm MACRO_NEWS_HIGH", TaskType.USER),
            new Step("task_arm_macro", "Arm macro flag on quote", TaskType.SERVICE),
            new Step("task_resolve_markup", "Resolve markup × news_multiplier", TaskType.SERVICE),
            new Step("task_publish_hint", "Publish regime-shifted + last-look hint", TaskType.SERVICE),
            new Step("task_apply_route_penalty", "Apply last-look penalty (route-exec)", TaskType.SERVICE));

    private static final List<Step> INIT_ECONOMIC_CALENDAR = List.of(
            new Step("task_init_load_calendar", "Load economic calendar rows into quote", TaskType.SERVICE),
            new Step("task_init_verify_calendar", "Verify calendar rows present in quote", TaskType.SERVICE));

    private static final List<Step> INIT_CURRENCY_PAIRS = List.of(
            new Step("task_init_load_pairs", "Load currency pair master into quote", TaskType.SERVICE),
            new Step("task_init_verify_pairs", "Verify currency pair count in quote", TaskType.SERVICE));

    private static final List<Step> INIT_SETTLEMENT_POLICIES = List.of(
            new Step("task_init_load_settlement", "Load settlement policies into qualify", TaskType.SERVICE),
            new Step("task_init_verify_settlement", "Verify settlement policy count in qualify", TaskType.SERVICE));

    private static final List<Step> INIT_CREDIT_LINES = List.of(
            new Step("task_init_load_credit", "Load credit lines into qualify", TaskType.SERVICE),
            new Step("task_init_verify_credit", "Verify loaded credit lines present in qualify", TaskType.SERVICE));

    private static final Map<String, List<Step>> BY_KEY = Map.of(
            Fr01BpmnDefinition.PROCESS_KEY,
            FR01_ADMIN,
            "FR01_CORPORATE_QUOTE_ROUTE",
            List.of(
                    new Step("task_regime_context", "Fetch regime context from quote", TaskType.SERVICE),
                    new Step("task_indicative", "Request indicative quote", TaskType.SERVICE),
                    new Step("task_route_execute", "Route-execute with last-look penalty", TaskType.SERVICE)),
            InitDataCatalog.PAIRS_WORKFLOW,
            INIT_CURRENCY_PAIRS,
            InitDataCatalog.SETTLEMENT_WORKFLOW,
            INIT_SETTLEMENT_POLICIES,
            InitDataCatalog.CREDIT_WORKFLOW,
            INIT_CREDIT_LINES,
            InitDataCatalog.CALENDAR_WORKFLOW,
            INIT_ECONOMIC_CALENDAR,
            "FR01_RETAIL_CONVERT",
            List.of(
                    new Step("task_regime_context", "Fetch retail-tier regime context", TaskType.SERVICE),
                    new Step("task_indicative", "All-in indicative convert quote", TaskType.SERVICE),
                    new Step("task_route_execute", "Optional route-execute (retail notional)", TaskType.SERVICE)));

    private BpmnStepCatalog() {
    }

    public static List<Step> stepsFor(String workflowKey) {
        return BY_KEY.getOrDefault(workflowKey, List.of());
    }
}
