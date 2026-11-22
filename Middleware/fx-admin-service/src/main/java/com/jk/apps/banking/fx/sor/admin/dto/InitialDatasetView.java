package com.jk.apps.banking.fx.sor.admin.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/** One Initial Data accordion: dataset metadata, setup status, and the latest run's task rows. */
public record InitialDatasetView(
        String datasetKey,
        String title,
        String description,
        String ownerService,
        String workflowKey,
        String setupStatus,
        boolean setupAllowed,
        UUID wfExecId,
        Instant startedAt,
        Instant completedAt,
        List<TaskRowView> tasks) {

    /** One wf_task_exec_log row, as shown in the flow view. */
    public record TaskRowView(
            int seq,
            String taskId,
            String name,
            String type,
            String status,
            String inputJson,
            String outputJson,
            String errorDetails,
            Instant startedAt,
            Instant completedAt,
            Long durationMs) {
    }
}
