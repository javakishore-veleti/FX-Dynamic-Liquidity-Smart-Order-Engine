package com.jk.apps.banking.fx.sor.quote.business.facade;

import org.springframework.stereotype.Component;

/**
 * Intentionally does NOT auto-arm macro flags.
 * Calendar windows are visible via {@code GET /api/v1/fx/economic-calendar} and are applied only when
 * the Admin BPMN workflow task {@code task_scan_calendar} / {@code task_arm_macro} runs (recorded in wf_exec_log).
 */
@Component
public class MacroCalendarScheduler {
}
