package com.jk.apps.banking.fx.sor.agent.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jk.apps.banking.fx.sor.agent.dto.AgentAcceptedResponse;
import com.jk.apps.banking.fx.sor.agent.dto.BackpressureAssessRequest;
import com.jk.apps.banking.fx.sor.agent.dto.BlastRadiusAnalyzeRequest;
import com.jk.apps.banking.fx.sor.agent.dto.ChaosSimExecuteRequest;
import com.jk.apps.banking.fx.sor.agent.dto.IncidentRcaInvestigateRequest;
import com.jk.apps.banking.fx.sor.agent.metrics.ServiceMetrics;
import com.jk.apps.banking.fx.sor.agent.persistence.entity.AgentRunLog;
import com.jk.apps.banking.fx.sor.agent.persistence.repo.AgentRunLogRepository;
import com.jk.apps.banking.fx.sor.agent.service.workflow.AgentWorkflowResult;
import com.jk.apps.banking.fx.sor.agent.service.workflow.BackpressureAgentWorkflow;
import com.jk.apps.banking.fx.sor.agent.service.workflow.BlastRadiusAgentWorkflow;
import com.jk.apps.banking.fx.sor.agent.service.workflow.ChaosSimAgentWorkflow;
import com.jk.apps.banking.fx.sor.agent.service.workflow.IncidentRcaAgentWorkflow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
public class AgentOrchestratorService {

    public static final String TRIGGERED_BY = "SPRING_AI_AGENT";

    private static final Logger log = LoggerFactory.getLogger(AgentOrchestratorService.class);

    private final AgentRunLogRepository repository;
    private final ObjectMapper objectMapper;
    private final ServiceMetrics metrics;
    private final BlastRadiusAgentWorkflow blastRadiusAgentWorkflow;
    private final BackpressureAgentWorkflow backpressureAgentWorkflow;
    private final IncidentRcaAgentWorkflow incidentRcaAgentWorkflow;
    private final ChaosSimAgentWorkflow chaosSimAgentWorkflow;

    public AgentOrchestratorService(
            AgentRunLogRepository repository,
            ObjectMapper objectMapper,
            ServiceMetrics metrics,
            BlastRadiusAgentWorkflow blastRadiusAgentWorkflow,
            BackpressureAgentWorkflow backpressureAgentWorkflow,
            IncidentRcaAgentWorkflow incidentRcaAgentWorkflow,
            ChaosSimAgentWorkflow chaosSimAgentWorkflow) {
        this.repository = repository;
        this.objectMapper = objectMapper;
        this.metrics = metrics;
        this.blastRadiusAgentWorkflow = blastRadiusAgentWorkflow;
        this.backpressureAgentWorkflow = backpressureAgentWorkflow;
        this.incidentRcaAgentWorkflow = incidentRcaAgentWorkflow;
        this.chaosSimAgentWorkflow = chaosSimAgentWorkflow;
    }

    @Transactional
    public AgentAcceptedResponse analyzeBlastRadius(BlastRadiusAnalyzeRequest request) {
        return acceptAndRun(
                "BLAST_RADIUS",
                request.isDryRun(),
                request.getApprovalToken(),
                request,
                () -> blastRadiusAgentWorkflow.execute(request)
        );
    }

    @Transactional
    public AgentAcceptedResponse assessBackpressure(BackpressureAssessRequest request) {
        return acceptAndRun(
                "BACKPRESSURE",
                request.isDryRun(),
                request.getApprovalToken(),
                request,
                () -> backpressureAgentWorkflow.execute(request)
        );
    }

    @Transactional
    public AgentAcceptedResponse investigateIncidentRca(IncidentRcaInvestigateRequest request) {
        enforceLiveMutationGate(request.isDryRun(), request.getApprovalToken());
        return acceptAndRun(
                "INCIDENT_RCA",
                request.isDryRun(),
                request.getApprovalToken(),
                request,
                () -> incidentRcaAgentWorkflow.execute(request)
        );
    }

    @Transactional
    public AgentAcceptedResponse executeChaosSim(ChaosSimExecuteRequest request) {
        return acceptAndRun(
                "CHAOS_SIM",
                request.isDryRun(),
                request.getApprovalToken(),
                request,
                () -> chaosSimAgentWorkflow.execute(request)
        );
    }

    private AgentAcceptedResponse acceptAndRun(
            String agentType,
            boolean dryRun,
            String approvalToken,
            Object request,
            WorkflowSupplier workflow) {
        Instant startedAt = Instant.now();
        UUID runId = UUID.randomUUID();

        AgentRunLog run = new AgentRunLog();
        run.setRunId(runId);
        run.setAgentType(agentType);
        run.setDryRun(dryRun);
        run.setStatus("QUEUED");
        run.setRequestParams(toJson(request));
        run.setApprovalToken(approvalToken);
        run.setTriggeredBy(TRIGGERED_BY);
        run.setStartedAt(startedAt);
        run.setUpdatedAt(startedAt);
        repository.save(run);
        metrics.agentRunsAccepted().increment();

        try {
            run.setStatus("RUNNING");
            run.setUpdatedAt(Instant.now());
            repository.save(run);

            AgentWorkflowResult result = workflow.execute();

            Instant completedAt = Instant.now();
            run.setMcpToolsInvoked(toJson(result.mcpInvocations()));
            run.setResultSummary(toJson(result.summary()));
            run.setStatus("COMPLETED");
            run.setCompletedAt(completedAt);
            run.setDurationMs(completedAt.toEpochMilli() - startedAt.toEpochMilli());
            run.setUpdatedAt(completedAt);
            repository.save(run);
            metrics.agentRunsCompleted().increment();
        } catch (Exception ex) {
            Instant failedAt = Instant.now();
            log.error("Agent {} run {} failed: {}", agentType, runId, ex.getMessage(), ex);
            run.setStatus("FAILED");
            run.setErrorDetails(ex.getMessage());
            run.setCompletedAt(failedAt);
            run.setDurationMs(failedAt.toEpochMilli() - startedAt.toEpochMilli());
            run.setUpdatedAt(failedAt);
            repository.save(run);
            metrics.agentRunsFailed().increment();
        }

        return new AgentAcceptedResponse(
                runId,
                agentType,
                run.getStatus(),
                dryRun,
                TRIGGERED_BY,
                startedAt
        );
    }

    private void enforceLiveMutationGate(boolean dryRun, String approvalToken) {
        if (!dryRun && (approvalToken == null || approvalToken.isBlank())) {
            throw new IllegalArgumentException(
                    "ADR-032: dryRun=false mutations require Maker-Checker approvalToken");
        }
    }

    private String toJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            return "{\"serializationError\":\"" + e.getMessage() + "\"}";
        }
    }

    @FunctionalInterface
    private interface WorkflowSupplier {
        AgentWorkflowResult execute();
    }
}
