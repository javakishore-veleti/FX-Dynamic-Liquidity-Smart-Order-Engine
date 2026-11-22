package com.jk.apps.banking.fx.sor.admin.wf;

import java.util.Map;

/**
 * One SERVICE task inside a BPMN workflow run. Implementations are Spring beans under
 * {@code <facade>/tasks}; the engine dispatches to them by {@link #id()}.
 */
public interface WfTask {

    /** Matches the taskId in the step catalog, e.g. {@code task_init_load_calendar}. */
    String id();

    /** Runs the task. May read and write run variables; returns the output detail (text or JSON). */
    String run(Map<String, Object> vars);
}
