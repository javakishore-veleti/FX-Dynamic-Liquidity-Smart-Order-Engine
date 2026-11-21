package com.jk.apps.banking.fx.sor.admin.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jk.apps.banking.fx.sor.admin.dto.CacheInvalidateRequest;
import com.jk.apps.banking.fx.sor.admin.dto.CreditOverrideRequest;
import com.jk.apps.banking.fx.sor.admin.dto.LoadSimRequest;
import com.jk.apps.banking.fx.sor.admin.dto.SyntheticGenerateRequest;
import com.jk.apps.banking.fx.sor.admin.dto.VenueHaltRequest;
import com.jk.apps.banking.fx.sor.admin.dto.WorkflowAcceptedResponse;
import com.jk.apps.banking.fx.sor.admin.dto.WorkflowStatusResponse;
import com.jk.apps.banking.fx.sor.admin.entity.WfExecLog;
import com.jk.apps.banking.fx.sor.admin.eventing.DomainEventPublisher;
import com.jk.apps.banking.fx.sor.admin.metrics.ServiceMetrics;
import com.jk.apps.banking.fx.sor.admin.repository.WfExecLogRepository;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AdminWorkflowService {

    private static final List<String> DEFAULT_DATASETS = List.of(
            "SPREAD_SCHEDULES",
            "SETTLEMENT_POLICIES",
            "BILATERAL_CREDIT",
            "LP_TELEMETRY",
            "EXECUTION_ORDERS");
    private static final String TOPIC_POLICY_OVERRIDE = "fx.admin.policy-or-override-applied.v1";
    private static final String TOPIC_SYNTHETIC_COMPLETED = "fx.admin.synthetic-workflow-completed.v1";

    private final WfExecLogRepository repository;
    private final ExecutorService workflowExecutor;
    private final ObjectMapper objectMapper;
    private final ServiceMetrics metrics;
    private final DomainEventPublisher domainEvents;
    private final Set<UUID> cancelSignals = ConcurrentHashMap.newKeySet();

    public AdminWorkflowService(
            WfExecLogRepository repository,
            ExecutorService workflowExecutor,
            ObjectMapper objectMapper,
            ServiceMetrics metrics,
            DomainEventPublisher domainEvents) {
        this.repository = repository;
        this.workflowExecutor = workflowExecutor;
        this.objectMapper = objectMapper;
        this.metrics = metrics;
        this.domainEvents = domainEvents;
    }

    public WorkflowAcceptedResponse generateSynthetic(SyntheticGenerateRequest request) {
        int numBatches = request.getNumBatches() == null ? 10 : request.getNumBatches();
        int batchSize = request.getBatchSize() == null ? 1000 : request.getBatchSize();
        List<String> datasets = request.getDatasetTypes() == null || request.getDatasetTypes().isEmpty()
                ? DEFAULT_DATASETS
                : request.getDatasetTypes();
        String targetEnv = request.getTargetEnv() == null ? "PERF_SIM" : request.getTargetEnv();
        String triggeredBy = request.getTriggeredBy() == null ? "ADMIN_API" : request.getTriggeredBy();

        Map<String, Object> params = new LinkedHashMap<>();
        params.put("numBatches", numBatches);
        params.put("batchSize", batchSize);
        params.put("datasetTypes", datasets);
        params.put("targetEnv", targetEnv);

        WfExecLog log = createQueued(
                "SYNTHETIC_SEED",
                toJson(datasets),
                numBatches,
                batchSize,
                toJson(params),
                triggeredBy);
        metrics.syntheticJobsQueued().increment();
        UUID wfExecId = log.getWfExecId();
        workflowExecutor.execute(() -> runSynthetic(wfExecId, numBatches, batchSize, datasets));
        return new WorkflowAcceptedResponse(log.getWfExecId(), log.getStatus(), log.getWorkflowType());
    }

    public WorkflowAcceptedResponse triggerLoadSim(LoadSimRequest request) {
        int targetRps = request.getTargetRps() == null ? 50_000 : request.getTargetRps();
        int durationSeconds = request.getDurationSeconds() == null ? 60 : request.getDurationSeconds();
        String regime = request.getRegime() == null ? "NY_LONDON_OVERLAP" : request.getRegime();
        String triggeredBy = request.getTriggeredBy() == null ? "ADMIN_API" : request.getTriggeredBy();

        Map<String, Object> params = new LinkedHashMap<>();
        params.put("targetRps", targetRps);
        params.put("durationSeconds", durationSeconds);
        params.put("regime", regime);

        WfExecLog log = createQueued(
                "LOAD_SIMULATION",
                null,
                Math.max(1, durationSeconds / 10),
                targetRps,
                toJson(params),
                triggeredBy);
        metrics.loadSimJobsQueued().increment();
        UUID wfExecId = log.getWfExecId();
        workflowExecutor.execute(() -> runLoadSim(wfExecId, targetRps, durationSeconds, regime));
        return new WorkflowAcceptedResponse(log.getWfExecId(), log.getStatus(), log.getWorkflowType());
    }

    @Transactional(readOnly = true)
    public WorkflowStatusResponse getWorkflow(UUID wfExecId) {
        return toStatus(require(wfExecId));
    }

    @Transactional(readOnly = true)
    public Page<WorkflowStatusResponse> listWorkflows(
            String status, String workflowType, String triggeredBy, Pageable pageable) {
        return repository.findFiltered(status, workflowType, triggeredBy, pageable).map(this::toStatus);
    }

    @Transactional
    public WorkflowStatusResponse cancel(UUID wfExecId) {
        WfExecLog log = require(wfExecId);
        if ("COMPLETED".equals(log.getStatus()) || "FAILED".equals(log.getStatus()) || "CANCELLED".equals(log.getStatus())) {
            return toStatus(log);
        }
        cancelSignals.add(wfExecId);
        log.setStatus("CANCELLED");
        log.setUpdatedAt(Instant.now());
        log.setCompletedAt(Instant.now());
        log.setDurationMs(Math.max(0, Instant.now().toEpochMilli() - log.getStartedAt().toEpochMilli()));
        repository.save(log);
        metrics.workflowsCancelled().increment();
        return toStatus(log);
    }

    @Transactional
    public void delete(UUID wfExecId, boolean purgeSyntheticData) {
        WfExecLog log = require(wfExecId);
        repository.delete(log);
        if (purgeSyntheticData) {
            // Synthetic purge is a no-op placeholder; trading DBs are owned by other services.
        }
    }

    @Transactional
    public WorkflowAcceptedResponse invalidateCache(CacheInvalidateRequest request) {
        Map<String, Object> params = new LinkedHashMap<>();
        params.put("cacheKeys", request.getCacheKeys());
        params.put("reason", request.getReason());
        WfExecLog log = createCompletedOps("CACHE_INVALIDATION", toJson(params), toJson(Map.of(
                "action", "INVALIDATE",
                "keys", request.getCacheKeys() == null ? List.of() : request.getCacheKeys())));
        emitPolicyOrOverride(
                log.getWfExecId(),
                "CACHE_INVALIDATION",
                "cache:" + (request.getCacheKeys() == null ? "ALL" : request.getCacheKeys()),
                "ADMIN_API",
                params.toString());
        return new WorkflowAcceptedResponse(log.getWfExecId(), log.getStatus(), log.getWorkflowType());
    }

    @Transactional
    public WorkflowAcceptedResponse creditOverride(CreditOverrideRequest request) {
        Map<String, Object> params = new LinkedHashMap<>();
        params.put("clientId", request.getClientId());
        params.put("counterpartyLpId", request.getCounterpartyLpId());
        params.put("maxCreditUsd", request.getMaxCreditUsd());
        params.put("reason", request.getReason());
        WfExecLog log = createCompletedOps("CREDIT_OVERRIDE", toJson(params), toJson(Map.of(
                "action", "CREDIT_OVERRIDE",
                "diff", params)));
        emitPolicyOrOverride(
                log.getWfExecId(),
                "CREDIT_OVERRIDE",
                request.getClientId() + "/" + request.getCounterpartyLpId(),
                "ADMIN_API",
                params.toString());
        return new WorkflowAcceptedResponse(log.getWfExecId(), log.getStatus(), log.getWorkflowType());
    }

    @Transactional
    public WorkflowAcceptedResponse haltVenue(String lpId, VenueHaltRequest request) {
        Map<String, Object> params = new LinkedHashMap<>();
        params.put("lpId", lpId);
        params.put("halt", request.isHalt());
        params.put("reason", request.getReason());
        WfExecLog log = createCompletedOps("LP_KILL_SWITCH", toJson(params), toJson(Map.of(
                "action", request.isHalt() ? "HALT" : "RESUME",
                "lpId", lpId)));
        emitPolicyOrOverride(
                log.getWfExecId(),
                request.isHalt() ? "LP_VENUE_HALT" : "LP_VENUE_RESUME",
                lpId,
                "ADMIN_API",
                params.toString());
        return new WorkflowAcceptedResponse(log.getWfExecId(), log.getStatus(), log.getWorkflowType());
    }

    private void runSynthetic(UUID wfExecId, int numBatches, int batchSize, List<String> datasets) {
        WfExecLog log = repository.findById(wfExecId).orElse(null);
        if (log == null) {
            return;
        }
        try {
            log.setStatus("RUNNING");
            log.setUpdatedAt(Instant.now());
            repository.save(log);
            long inserted = 0;
            for (int i = 1; i <= numBatches; i++) {
                if (cancelSignals.contains(wfExecId) || "CANCELLED".equals(log.getStatus())) {
                    markCancelled(wfExecId);
                    return;
                }
                inserted += (long) batchSize * datasets.size();
                log = repository.findById(wfExecId).orElseThrow();
                log.setBatchesCompleted(i);
                log.setRecordsInserted(inserted);
                log.setUpdatedAt(Instant.now());
                log.setExecutionSummary(toJson(Map.of(
                        "datasets", datasets,
                        "batchesCompleted", i,
                        "recordsInserted", inserted)));
                repository.save(log);
                Thread.sleep(5);
            }
            complete(wfExecId, "COMPLETED", null);
        } catch (Exception ex) {
            complete(wfExecId, "FAILED", ex.getMessage());
        } finally {
            cancelSignals.remove(wfExecId);
        }
    }

    private void runLoadSim(UUID wfExecId, int targetRps, int durationSeconds, String regime) {
        try {
            WfExecLog log = repository.findById(wfExecId).orElseThrow();
            log.setStatus("RUNNING");
            log.setUpdatedAt(Instant.now());
            repository.save(log);

            int chunks = Math.max(1, durationSeconds / 10);
            long totalEvents = 0;
            for (int i = 1; i <= chunks; i++) {
                if (cancelSignals.contains(wfExecId)) {
                    markCancelled(wfExecId);
                    return;
                }
                totalEvents += (long) targetRps * 10;
                log = repository.findById(wfExecId).orElseThrow();
                log.setBatchesCompleted(i);
                log.setRecordsInserted(totalEvents);
                log.setUpdatedAt(Instant.now());
                log.setExecutionSummary(toJson(Map.of(
                        "regime", regime,
                        "targetRps", targetRps,
                        "eventsEmitted", totalEvents)));
                repository.save(log);
                Thread.sleep(5);
            }
            complete(wfExecId, "COMPLETED", null);
        } catch (Exception ex) {
            complete(wfExecId, "FAILED", ex.getMessage());
        } finally {
            cancelSignals.remove(wfExecId);
        }
    }

    private WfExecLog createQueued(
            String type,
            String datasetTypes,
            int numBatches,
            int batchSize,
            String requestParams,
            String triggeredBy) {
        Instant now = Instant.now();
        WfExecLog log = new WfExecLog();
        log.setWfExecId(UUID.randomUUID());
        log.setWorkflowType(type);
        log.setDatasetTypes(datasetTypes);
        log.setNumBatches(numBatches);
        log.setBatchSize(batchSize);
        log.setBatchesCompleted(0);
        log.setRecordsInserted(0);
        log.setRequestParams(requestParams);
        log.setStatus("QUEUED");
        log.setStartedAt(now);
        log.setUpdatedAt(now);
        log.setTriggeredBy(triggeredBy);
        return repository.save(log);
    }

    private WfExecLog createCompletedOps(String type, String requestParams, String summary) {
        Instant now = Instant.now();
        WfExecLog log = new WfExecLog();
        log.setWfExecId(UUID.randomUUID());
        log.setWorkflowType(type);
        log.setNumBatches(1);
        log.setBatchSize(1);
        log.setBatchesCompleted(1);
        log.setRecordsInserted(0);
        log.setRequestParams(requestParams);
        log.setExecutionSummary(summary);
        log.setStatus("COMPLETED");
        log.setStartedAt(now);
        log.setUpdatedAt(now);
        log.setCompletedAt(now);
        log.setDurationMs(0L);
        log.setTriggeredBy("ADMIN_API");
        return repository.save(log);
    }

    private void complete(UUID wfExecId, String status, String error) {
        repository.findById(wfExecId).ifPresent(log -> {
            Instant now = Instant.now();
            log.setStatus(status);
            log.setUpdatedAt(now);
            log.setCompletedAt(now);
            log.setDurationMs(Math.max(0, now.toEpochMilli() - log.getStartedAt().toEpochMilli()));
            log.setErrorDetails(error);
            repository.save(log);
            if ("COMPLETED".equals(status)) {
                emitSyntheticWorkflowCompleted(log);
            }
        });
    }

    private void emitPolicyOrOverride(
            UUID wfExecId, String actionType, String targetResource, String approvedBy, String diffSummary) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("eventId", UUID.randomUUID().toString());
        payload.put("wfExecId", wfExecId.toString());
        payload.put("actionType", actionType);
        payload.put("targetResource", targetResource);
        payload.put("approvedBy", approvedBy);
        payload.put("diffSummary", diffSummary);
        payload.put("timestamp", Instant.now().toString());
        domainEvents.publish(TOPIC_POLICY_OVERRIDE, payload);
    }

    private void emitSyntheticWorkflowCompleted(WfExecLog log) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("eventId", UUID.randomUUID().toString());
        payload.put("wfExecId", log.getWfExecId().toString());
        payload.put("workflowType", log.getWorkflowType());
        payload.put("batchesCompleted", log.getBatchesCompleted());
        payload.put("recordsInserted", log.getRecordsInserted());
        payload.put("durationMs", log.getDurationMs());
        payload.put("timestamp", Instant.now().toString());
        domainEvents.publish(TOPIC_SYNTHETIC_COMPLETED, payload);
    }

    private void markCancelled(UUID wfExecId) {
        repository.findById(wfExecId).ifPresent(log -> {
            Instant now = Instant.now();
            log.setStatus("CANCELLED");
            log.setUpdatedAt(now);
            log.setCompletedAt(now);
            log.setDurationMs(Math.max(0, now.toEpochMilli() - log.getStartedAt().toEpochMilli()));
            repository.save(log);
            metrics.workflowsCancelled().increment();
        });
    }

    private WfExecLog require(UUID wfExecId) {
        return repository
                .findById(wfExecId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "workflow not found"));
    }

    private WorkflowStatusResponse toStatus(WfExecLog log) {
        WorkflowStatusResponse response = new WorkflowStatusResponse();
        response.setWfExecId(log.getWfExecId());
        response.setWorkflowType(log.getWorkflowType());
        response.setStatus(log.getStatus());
        response.setStartedAt(log.getStartedAt());
        response.setCompletedAt(log.getCompletedAt());
        response.setBatchesCompleted(log.getBatchesCompleted());
        response.setRecordsInserted(log.getRecordsInserted());
        response.setErrorDetails(log.getErrorDetails());
        response.setExecutionSummary(log.getExecutionSummary());
        return response;
    }

    private String toJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            return String.valueOf(value);
        }
    }
}
