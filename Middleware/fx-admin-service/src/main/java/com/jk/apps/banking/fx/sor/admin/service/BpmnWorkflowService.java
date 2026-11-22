package com.jk.apps.banking.fx.sor.admin.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jk.apps.banking.fx.sor.admin.bpmn.BpmnStepCatalog;
import com.jk.apps.banking.fx.sor.admin.bpmn.Fr01BpmnDefinition;
import com.jk.apps.banking.fx.sor.admin.initdata.InitDataCatalog;
import com.jk.apps.banking.fx.sor.admin.dto.BpmnProcessView;
import com.jk.apps.banking.fx.sor.admin.entity.WfExecLog;
import com.jk.apps.banking.fx.sor.admin.entity.WfTaskExecLog;
import com.jk.apps.banking.fx.sor.admin.entity.WorkflowDefinition;
import com.jk.apps.banking.fx.sor.admin.repository.WfExecLogRepository;
import com.jk.apps.banking.fx.sor.admin.repository.WfTaskExecLogRepository;
import com.jk.apps.banking.fx.sor.admin.wf.WfTask;
import com.jk.apps.banking.fx.sor.admin.repository.WorkflowDefinitionRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

/**
 * BPMN process engine backed by master {@code workflows} + every run in {@code wf_exec_log}.
 * Nothing is hidden: portals list definitions and task execution views from these tables.
 */
@Service
public class BpmnWorkflowService {

    private final WorkflowDefinitionRepository workflowRepository;
    private final WfExecLogRepository execLogRepository;
    private final WfTaskExecLogRepository taskExecLogRepository;
    private final QuoteScheduleClient quoteScheduleClient;
    private final ObjectMapper objectMapper;
    private final Map<String, WfTask> taskRegistry;

    public BpmnWorkflowService(
            WorkflowDefinitionRepository workflowRepository,
            WfExecLogRepository execLogRepository,
            WfTaskExecLogRepository taskExecLogRepository,
            QuoteScheduleClient quoteScheduleClient,
            ObjectMapper objectMapper,
            List<WfTask> wfTasks) {
        this.workflowRepository = workflowRepository;
        this.execLogRepository = execLogRepository;
        this.taskExecLogRepository = taskExecLogRepository;
        this.quoteScheduleClient = quoteScheduleClient;
        this.objectMapper = objectMapper;
        this.taskRegistry = wfTasks.stream().collect(Collectors.toMap(WfTask::id, Function.identity()));
    }

    public List<WorkflowDefinition> listMaster(String portalScope) {
        if (portalScope == null || portalScope.isBlank()) {
            return workflowRepository.findByStatusOrderByWorkflowKeyAsc("ACTIVE");
        }
        return workflowRepository.findByPortalScopeAndStatusOrderByWorkflowKeyAsc(
                portalScope.toUpperCase(), "ACTIVE");
    }

    public WorkflowDefinition getMaster(String workflowKey) {
        return workflowRepository
                .findById(workflowKey)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "workflow definition not found"));
    }

    public Page<WfExecLog> listExecutions(
            String status, String workflowType, String workflowKey, String triggeredBy, Pageable pageable) {
        if (workflowKey != null && !workflowKey.isBlank()) {
            return execLogRepository.findByWorkflowKey(workflowKey, pageable);
        }
        return execLogRepository.findFiltered(status, workflowType, triggeredBy, pageable);
    }

    @Transactional
    public BpmnProcessView start(String workflowKey, Map<String, Object> variables, String triggeredBy) {
        WorkflowDefinition def = getMaster(workflowKey);
        if (!"ACTIVE".equalsIgnoreCase(def.getStatus())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "workflow not ACTIVE");
        }
        List<BpmnStepCatalog.Step> steps = BpmnStepCatalog.stepsFor(workflowKey);
        if (steps.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "no process steps registered for " + workflowKey);
        }

        Instant now = Instant.now();
        Map<String, Object> vars = variables == null ? new LinkedHashMap<>() : new LinkedHashMap<>(variables);
        vars.putIfAbsent("clientTier", "TIER_2");
        vars.putIfAbsent("ccyPair", "EURUSD");
        vars.putIfAbsent("confirmMacro", false);
        if (InitDataCatalog.isInitialDataWorkflow(workflowKey)) {
            validateInitialDataStart(vars, triggeredBy);
        } else {
            validateStartCriteria(workflowKey, vars, triggeredBy);
        }

        List<Map<String, Object>> taskRows = new ArrayList<>();
        for (BpmnStepCatalog.Step step : steps) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("taskId", step.taskId());
            row.put("name", step.name());
            row.put("type", step.type().name());
            row.put("status", "PENDING");
            taskRows.add(row);
        }

        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("processKey", def.getWorkflowKey());
        summary.put("bpmnResource", def.getBpmnResource());
        summary.put("currentTaskId", steps.getFirst().taskId());
        summary.put("currentTaskName", steps.getFirst().name());
        summary.put("variables", vars);
        summary.put("tasks", taskRows);

        WfExecLog log = new WfExecLog();
        log.setWfExecId(UUID.randomUUID());
        log.setWorkflowType(Fr01BpmnDefinition.WORKFLOW_TYPE);
        log.setWorkflowKey(def.getWorkflowKey());
        log.setNumBatches(steps.size());
        log.setBatchSize(1);
        log.setBatchesCompleted(0);
        log.setRecordsInserted(0);
        log.setRequestParams(toJson(vars));
        log.setExecutionSummary(toJson(summary));
        log.setStatus("RUNNING");
        log.setStartedAt(now);
        log.setUpdatedAt(now);
        log.setTriggeredBy(triggeredBy == null || triggeredBy.isBlank() ? "ADMIN_PORTAL" : triggeredBy);
        execLogRepository.save(log);

        // Auto-run until first USER task or completion
        return advanceInternal(log.getWfExecId(), Map.of(), false);
    }

    @Transactional
    public BpmnProcessView completeCurrentTask(UUID wfExecId, Map<String, Object> payload) {
        return advanceInternal(wfExecId, payload == null ? Map.of() : payload, true);
    }

    @Transactional(readOnly = true)
    public BpmnProcessView getProcess(UUID wfExecId) {
        return toView(require(wfExecId));
    }

    private void validateInitialDataStart(Map<String, Object> vars, String triggeredBy) {
        if (triggeredBy == null || triggeredBy.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "triggeredBy required");
        }
    }

    private void validateStartCriteria(String workflowKey, Map<String, Object> vars, String triggeredBy) {
        String reason = String.valueOf(vars.getOrDefault("reason", "")).trim();
        if (reason.length() < 8) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "business reason required (min 8 chars)");
        }
        if (triggeredBy == null || triggeredBy.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "triggeredBy required");
        }
        String clientId = String.valueOf(vars.getOrDefault("clientId", "")).trim();
        if (clientId.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "clientId required");
        }
        BigDecimal notional;
        try {
            notional = new BigDecimal(String.valueOf(vars.getOrDefault("notional", "0")));
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "notional must be numeric");
        }
        if (notional.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "notional must be > 0");
        }
        if ("FR01_RETAIL_CONVERT".equals(workflowKey) && notional.compareTo(new BigDecimal("100000")) > 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "retail convert notional must be <= 100000");
        }
        if (("FR01_CORPORATE_QUOTE_ROUTE".equals(workflowKey) || "FR01_SESSION_REGIME_GUARDRAILS".equals(workflowKey))
                && notional.compareTo(new BigDecimal("100000")) < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "institutional notional must be >= 100000");
        }
        String ccyPair = String.valueOf(vars.getOrDefault("ccyPair", "")).trim();
        if (ccyPair.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "ccyPair required");
        }
    }

    private BpmnProcessView advanceInternal(UUID wfExecId, Map<String, Object> payload, boolean userAction) {
        WfExecLog log = require(wfExecId);
        if ("COMPLETED".equals(log.getStatus()) || "FAILED".equals(log.getStatus()) || "CANCELLED".equals(log.getStatus())) {
            return toView(log);
        }

        Map<String, Object> summary = parseSummary(log.getExecutionSummary());
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> tasks = (List<Map<String, Object>>) summary.get("tasks");
        @SuppressWarnings("unchecked")
        Map<String, Object> variables = (Map<String, Object>) summary.getOrDefault("variables", new LinkedHashMap<>());
        variables.putAll(payload);

        int idx = currentIndex(tasks);
        while (idx >= 0 && idx < tasks.size()) {
            Map<String, Object> task = tasks.get(idx);
            String type = String.valueOf(task.get("type"));
            String status = String.valueOf(task.get("status"));

            if ("PENDING".equals(status) || "WAITING".equals(status)) {
                if ("USER".equals(type) && !userAction && !"WAITING".equals(status)) {
                    task.put("status", "WAITING");
                    task.put("startedAt", Instant.now().toString());
                    summary.put("currentTaskId", task.get("taskId"));
                    summary.put("currentTaskName", task.get("name"));
                    summary.put("variables", variables);
                    summary.put("tasks", tasks);
                    persistRunning(log, summary, idx);
                    return toView(log);
                }
                if ("USER".equals(type) && "WAITING".equals(status) && !userAction) {
                    return toView(log);
                }

                Map<String, Object> taskInput = new LinkedHashMap<>(variables);
                try {
                    task.put("status", "RUNNING");
                    task.put("startedAt", Instant.now().toString());
                    String detail = executeStep(log.getWorkflowKey(), String.valueOf(task.get("taskId")), variables);
                    task.put("detail", detail);
                    task.put("status", "COMPLETED");
                    task.put("completedAt", Instant.now().toString());
                    recordTask(log, idx, task, taskInput, null);
                    userAction = false; // subsequent SERVICE steps auto-run
                } catch (Exception ex) {
                    task.put("status", "FAILED");
                    task.put("detail", ex.getMessage());
                    task.put("completedAt", Instant.now().toString());
                    recordTask(log, idx, task, taskInput, ex.getMessage());
                    summary.put("variables", variables);
                    summary.put("tasks", tasks);
                    summary.put("currentTaskId", task.get("taskId"));
                    summary.put("currentTaskName", task.get("name"));
                    log.setStatus("FAILED");
                    log.setErrorDetails(ex.getMessage());
                    log.setExecutionSummary(toJson(summary));
                    log.setUpdatedAt(Instant.now());
                    log.setCompletedAt(Instant.now());
                    execLogRepository.save(log);
                    return toView(log);
                }
            }

            // GATEWAY: skip USER confirm + arm when macro inactive
            if ("GATEWAY".equals(type) && "COMPLETED".equals(String.valueOf(task.get("status")))) {
                boolean macroActive = Boolean.TRUE.equals(variables.get("macroWindowActive"));
                if (!macroActive) {
                    skipTask(tasks, "task_confirm_macro", "skipped — no macro window");
                    skipTask(tasks, "task_arm_macro", "skipped — no macro window");
                }
            }

            idx = nextPendingIndex(tasks);
            if (idx < 0) {
                break;
            }
            Map<String, Object> next = tasks.get(idx);
            if ("USER".equals(String.valueOf(next.get("type")))) {
                next.put("status", "WAITING");
                next.put("startedAt", Instant.now().toString());
                summary.put("currentTaskId", next.get("taskId"));
                summary.put("currentTaskName", next.get("name"));
                summary.put("variables", variables);
                summary.put("tasks", tasks);
                persistRunning(log, summary, completedCount(tasks));
                return toView(log);
            }
        }

        summary.put("variables", variables);
        summary.put("tasks", tasks);
        summary.put("currentTaskId", null);
        summary.put("currentTaskName", null);
        Instant now = Instant.now();
        log.setBatchesCompleted(completedCount(tasks));
        log.setExecutionSummary(toJson(summary));
        log.setStatus("COMPLETED");
        log.setUpdatedAt(now);
        log.setCompletedAt(now);
        log.setDurationMs(Math.max(0, now.toEpochMilli() - log.getStartedAt().toEpochMilli()));
        execLogRepository.save(log);
        return toView(log);
    }

    private String executeStep(String workflowKey, String taskId, Map<String, Object> variables) {
        String tier = String.valueOf(variables.getOrDefault("clientTier", "TIER_2"));
        String pair = String.valueOf(variables.getOrDefault("ccyPair", "EURUSD"));

        return switch (taskId) {
            case "task_detect_session", "task_regime_context" -> {
                Map<String, Object> regime = quoteScheduleClient.regimeContext(tier, pair);
                variables.put("sessionRegime", regime.get("sessionRegime"));
                variables.put("lastLookPenaltyWeight", regime.get("lastLookPenaltyWeight"));
                variables.put("macroActive", regime.get("macroActive"));
                variables.put("sampleMarkupBps", regime.get("sampleMarkupBps"));
                yield toJson(regime);
            }
            case "task_scan_calendar" -> {
                List<Map<String, Object>> calendar = quoteScheduleClient.listEconomicCalendar();
                boolean active = calendar.stream().anyMatch(e -> Boolean.TRUE.equals(e.get("activeNow")));
                variables.put("macroWindowActive", active);
                variables.put("calendarEvents", calendar);
                if (active) {
                    calendar.stream()
                            .filter(e -> Boolean.TRUE.equals(e.get("activeNow")))
                            .findFirst()
                            .ifPresent(e -> {
                                variables.put("macroSource", e.get("source"));
                                variables.put("macroSeverity", e.get("severity"));
                                variables.put("macroLabel", e.get("label"));
                            });
                }
                yield "calendarEvents=" + calendar.size() + " activeNow=" + active;
            }
            case "gateway_macro" -> "macroWindowActive=" + variables.get("macroWindowActive");
            case "task_confirm_macro" -> {
                boolean confirm = Boolean.TRUE.equals(variables.get("confirmMacro"))
                        || "true".equalsIgnoreCase(String.valueOf(variables.getOrDefault("confirmMacro", "false")));
                if (!confirm) {
                    throw new IllegalStateException("Desk must set confirmMacro=true to arm MACRO_NEWS_HIGH");
                }
                yield "desk confirmed macro arm";
            }
            case "task_arm_macro" -> {
                Map<String, Object> body = new LinkedHashMap<>();
                body.put("source", variables.getOrDefault("macroSource", "FOMC"));
                body.put("severity", variables.getOrDefault("macroSeverity", new BigDecimal("1.25")));
                body.put("ttlSeconds", 3600);
                body.put("armedBy", "BPMN");
                Map<String, Object> result = quoteScheduleClient.putMacroFlag(body);
                variables.put("macroFlag", result);
                yield toJson(result);
            }
            case "task_resolve_markup", "task_indicative" -> {
                Map<String, Object> quote = quoteScheduleClient.indicative(Map.of(
                        "clientId",
                        variables.getOrDefault("clientId", "CORP-100"),
                        "clientTier",
                        tier,
                        "ccyPair",
                        pair,
                        "side",
                        "BUY",
                        "notional",
                        variables.getOrDefault("notional", 1_000_000),
                        "tenor",
                        "SPOT"));
                variables.put("indicative", quote);
                variables.put("sessionRegime", quote.get("sessionRegime"));
                yield toJson(quote);
            }
            case "task_publish_hint" -> {
                Map<String, Object> regime = quoteScheduleClient.regimeContext(tier, pair);
                variables.put("publishedHint", regime);
                yield "last-look hint refreshed via regime context " + toJson(regime);
            }
            case "task_apply_route_penalty", "task_route_execute" -> {
                Map<String, Object> routeReq = new LinkedHashMap<>();
                routeReq.put("clientOrderId", "BPMN-" + UUID.randomUUID());
                routeReq.put("clientId", variables.getOrDefault("clientId", "CORP-100"));
                routeReq.put("ccyPair", pair);
                routeReq.put("side", "BUY");
                routeReq.put("notional", variables.getOrDefault("notional", 2_500_000));
                routeReq.put("sessionRegime", variables.get("sessionRegime"));
                routeReq.put("lastLookPenaltyWeight", variables.get("lastLookPenaltyWeight"));
                Map<String, Object> routed = quoteScheduleClient.routeExecute(routeReq);
                variables.put("routeExecute", routed);
                yield toJson(routed);
            }
            default -> taskRegistry.containsKey(taskId)
                    ? taskRegistry.get(taskId).run(variables)
                    : "noop";
        };
    }

    /** Writes one wf_task_exec_log row per executed task: input snapshot, output or error, timings, audit. */
    private void recordTask(
            WfExecLog log, int seq, Map<String, Object> task, Map<String, Object> input, String error) {
        Instant now = Instant.now();
        String actor = log.getTriggeredBy();
        Instant started = parseInstant(task.get("startedAt"));
        Instant completed = parseInstant(task.get("completedAt"));

        WfTaskExecLog row = new WfTaskExecLog();
        row.setTaskExecId(UUID.randomUUID());
        row.setWfExecId(log.getWfExecId());
        row.setTaskSeq(seq);
        row.setTaskId(String.valueOf(task.get("taskId")));
        row.setTaskName(String.valueOf(task.get("name")));
        row.setTaskType(String.valueOf(task.get("type")));
        row.setStatus(String.valueOf(task.get("status")));
        row.setInputJson(toJson(input));
        row.setOutputJson(error == null ? asJsonOutput(task.get("detail")) : null);
        row.setErrorDetails(error);
        row.setStartedAt(started);
        row.setCompletedAt(completed);
        if (started != null && completed != null) {
            row.setDurationMs(Math.max(0, completed.toEpochMilli() - started.toEpochMilli()));
        }
        row.setCreatedAt(now);
        row.setCreatedBy(actor);
        row.setUpdatedAt(now);
        row.setUpdatedBy(actor);
        taskExecLogRepository.save(row);
    }

    private String asJsonOutput(Object detail) {
        String text = String.valueOf(detail);
        if (text.startsWith("{") || text.startsWith("[")) {
            return text;
        }
        return toJson(Map.of("message", text));
    }

    private Instant parseInstant(Object value) {
        if (value == null) {
            return null;
        }
        try {
            return Instant.parse(String.valueOf(value));
        } catch (Exception ex) {
            return null;
        }
    }

    private void persistRunning(WfExecLog log, Map<String, Object> summary, int batchesCompleted) {
        log.setBatchesCompleted(batchesCompleted);
        log.setExecutionSummary(toJson(summary));
        log.setStatus("RUNNING");
        log.setUpdatedAt(Instant.now());
        execLogRepository.save(log);
    }

    private static void skipTask(List<Map<String, Object>> tasks, String taskId, String detail) {
        for (Map<String, Object> t : tasks) {
            if (taskId.equals(t.get("taskId")) && "PENDING".equals(t.get("status"))) {
                t.put("status", "SKIPPED");
                t.put("detail", detail);
                t.put("completedAt", Instant.now().toString());
            }
        }
    }

    private static int currentIndex(List<Map<String, Object>> tasks) {
        for (int i = 0; i < tasks.size(); i++) {
            String s = String.valueOf(tasks.get(i).get("status"));
            if ("PENDING".equals(s) || "WAITING".equals(s) || "RUNNING".equals(s)) {
                return i;
            }
        }
        return -1;
    }

    private static int nextPendingIndex(List<Map<String, Object>> tasks) {
        for (int i = 0; i < tasks.size(); i++) {
            String s = String.valueOf(tasks.get(i).get("status"));
            if ("PENDING".equals(s) || "WAITING".equals(s)) {
                return i;
            }
        }
        return -1;
    }

    private static int completedCount(List<Map<String, Object>> tasks) {
        int n = 0;
        for (Map<String, Object> t : tasks) {
            String s = String.valueOf(t.get("status"));
            if ("COMPLETED".equals(s) || "SKIPPED".equals(s)) {
                n++;
            }
        }
        return n;
    }

    private WfExecLog require(UUID id) {
        return execLogRepository
                .findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "wf exec not found"));
    }

    private Map<String, Object> parseSummary(String json) {
        try {
            return objectMapper.readValue(json, new TypeReference<>() {
            });
        } catch (Exception e) {
            return new LinkedHashMap<>();
        }
    }

    private BpmnProcessView toView(WfExecLog log) {
        Map<String, Object> summary = parseSummary(log.getExecutionSummary());
        BpmnProcessView view = new BpmnProcessView();
        view.setWfExecId(log.getWfExecId());
        view.setProcessKey(log.getWorkflowKey());
        view.setWorkflowType(log.getWorkflowType());
        view.setStatus(log.getStatus());
        view.setTriggeredBy(log.getTriggeredBy());
        view.setStartedAt(log.getStartedAt());
        view.setCompletedAt(log.getCompletedAt());
        view.setBpmnResource(String.valueOf(summary.getOrDefault("bpmnResource", "")));
        view.setCurrentTaskId((String) summary.get("currentTaskId"));
        view.setCurrentTaskName((String) summary.get("currentTaskName"));
        @SuppressWarnings("unchecked")
        Map<String, Object> vars = (Map<String, Object>) summary.get("variables");
        view.setVariables(vars);
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> tasks = (List<Map<String, Object>>) summary.getOrDefault("tasks", List.of());
        List<BpmnProcessView.BpmnTaskView> taskViews = new ArrayList<>();
        for (Map<String, Object> t : tasks) {
            BpmnProcessView.BpmnTaskView tv = new BpmnProcessView.BpmnTaskView();
            tv.setTaskId(String.valueOf(t.get("taskId")));
            tv.setName(String.valueOf(t.get("name")));
            tv.setType(String.valueOf(t.get("type")));
            tv.setStatus(String.valueOf(t.get("status")));
            tv.setDetail(t.get("detail") == null ? null : String.valueOf(t.get("detail")));
            if (t.get("startedAt") != null) {
                tv.setStartedAt(Instant.parse(String.valueOf(t.get("startedAt"))));
            }
            if (t.get("completedAt") != null) {
                tv.setCompletedAt(Instant.parse(String.valueOf(t.get("completedAt"))));
            }
            taskViews.add(tv);
        }
        view.setTasks(taskViews);
        return view;
    }

    private String toJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            return String.valueOf(value);
        }
    }
}
