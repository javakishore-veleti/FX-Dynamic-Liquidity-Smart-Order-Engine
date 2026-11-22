package com.jk.apps.banking.fx.sor.admin.service;

import com.jk.apps.banking.fx.sor.admin.bpmn.InitialDatasetCatalog;
import com.jk.apps.banking.fx.sor.admin.bpmn.InitialDatasetCatalog.Dataset;
import com.jk.apps.banking.fx.sor.admin.dto.InitialDatasetView;
import com.jk.apps.banking.fx.sor.admin.dto.InitialDatasetView.TaskRowView;
import com.jk.apps.banking.fx.sor.admin.entity.WfExecLog;
import com.jk.apps.banking.fx.sor.admin.entity.WfTaskExecLog;
import com.jk.apps.banking.fx.sor.admin.repository.WfExecLogRepository;
import com.jk.apps.banking.fx.sor.admin.repository.WfTaskExecLogRepository;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

/**
 * Initial Data facade: lists datasets with their setup status and runs each setup through the
 * BPMN engine. A dataset is loaded once; after a COMPLETED run the setup is refused.
 */
@Service
public class InitialDataFacade {

    private static final String COMPLETED = "COMPLETED";
    private static final String RUNNING = "RUNNING";
    private static final String NOT_SETUP = "NOT_SETUP";

    private final BpmnWorkflowService bpmnWorkflowService;
    private final WfExecLogRepository execLogRepository;
    private final WfTaskExecLogRepository taskExecLogRepository;

    public InitialDataFacade(
            BpmnWorkflowService bpmnWorkflowService,
            WfExecLogRepository execLogRepository,
            WfTaskExecLogRepository taskExecLogRepository) {
        this.bpmnWorkflowService = bpmnWorkflowService;
        this.execLogRepository = execLogRepository;
        this.taskExecLogRepository = taskExecLogRepository;
    }

    @Transactional(readOnly = true)
    public List<InitialDatasetView> listDatasets() {
        return InitialDatasetCatalog.all().stream().map(this::toView).toList();
    }

    @Transactional
    public InitialDatasetView setup(String datasetKey, String reason, String triggeredBy) {
        Dataset dataset = InitialDatasetCatalog.byKey(datasetKey)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "unknown dataset " + datasetKey));
        if (execLogRepository.existsByWorkflowKeyAndStatus(dataset.workflowKey(), COMPLETED)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, dataset.title() + " is already set up");
        }
        if (execLogRepository.existsByWorkflowKeyAndStatus(dataset.workflowKey(), RUNNING)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, dataset.title() + " setup is already running");
        }
        Map<String, Object> variables = new LinkedHashMap<>();
        variables.put("reason", reason == null ? "" : reason);
        variables.put("triggeredBy", triggeredBy);
        bpmnWorkflowService.start(dataset.workflowKey(), variables, triggeredBy);
        return toView(dataset);
    }

    private InitialDatasetView toView(Dataset dataset) {
        Optional<WfExecLog> latest = execLogRepository.findFirstByWorkflowKeyOrderByStartedAtDesc(dataset.workflowKey());
        if (latest.isEmpty()) {
            return new InitialDatasetView(
                    dataset.key(), dataset.title(), dataset.description(), dataset.ownerService(),
                    dataset.workflowKey(), NOT_SETUP, true, null, null, null, List.of());
        }
        WfExecLog run = latest.get();
        boolean done = COMPLETED.equals(run.getStatus());
        List<TaskRowView> tasks = taskExecLogRepository.findByWfExecIdOrderByTaskSeqAsc(run.getWfExecId())
                .stream()
                .map(InitialDataFacade::toTaskRow)
                .toList();
        return new InitialDatasetView(
                dataset.key(), dataset.title(), dataset.description(), dataset.ownerService(),
                dataset.workflowKey(), run.getStatus(), !done && !RUNNING.equals(run.getStatus()),
                run.getWfExecId(), run.getStartedAt(), run.getCompletedAt(), tasks);
    }

    private static TaskRowView toTaskRow(WfTaskExecLog t) {
        return new TaskRowView(
                t.getTaskSeq(), t.getTaskId(), t.getTaskName(), t.getTaskType(), t.getStatus(),
                t.getInputJson(), t.getOutputJson(), t.getErrorDetails(),
                t.getStartedAt(), t.getCompletedAt(), t.getDurationMs());
    }
}
