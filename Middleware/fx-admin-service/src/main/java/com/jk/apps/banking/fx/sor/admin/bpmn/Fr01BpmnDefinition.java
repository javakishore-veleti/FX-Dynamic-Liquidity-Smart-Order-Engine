package com.jk.apps.banking.fx.sor.admin.bpmn;

/**
 * FR-01 BPMN constants (mirrors {@code bpmn/fr01-session-regime-guardrails.bpmn20.xml}).
 * Every process start is recorded in {@code wf_exec_log}.
 */
public final class Fr01BpmnDefinition {

    public static final String PROCESS_KEY = "FR01_SESSION_REGIME_GUARDRAILS";
    public static final String WORKFLOW_TYPE = "FR01_BPMN";
    public static final String BPMN_RESOURCE = "bpmn/fr01-session-regime-guardrails.bpmn20.xml";

    private Fr01BpmnDefinition() {
    }
}
