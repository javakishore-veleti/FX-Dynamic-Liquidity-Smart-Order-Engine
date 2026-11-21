from __future__ import annotations

import json
import time
from datetime import datetime, timezone

from fx_agent_orchestrator.business.dtos.workflow_dtos import WorkflowExecuteCommandDto
from fx_agent_orchestrator.business.workflows.backpressure_workflow import BackpressureWorkflow
from fx_agent_orchestrator.business.workflows.blast_radius_workflow import BlastRadiusWorkflow
from fx_agent_orchestrator.business.workflows.chaos_sim_workflow import ChaosSimWorkflow
from fx_agent_orchestrator.business.workflows.incident_rca_workflow import IncidentRcaWorkflow
from fx_agent_orchestrator.persistence.dtos.run_record_dtos import (
    PersistAgentRunDto,
    PersistRunCompleteDto,
    PersistRunFailDto,
)
from fx_agent_orchestrator.persistence.repositories.agent_run_repository import AgentRunRepository
from fx_agent_orchestrator.presentation.dtos.agent_request_dtos import (
    BackpressureAssessRequestDto,
    BlastRadiusAnalyzeRequestDto,
    ChaosSimExecuteRequestDto,
    IncidentRcaInvestigateRequestDto,
)
from fx_agent_orchestrator.presentation.dtos.agent_response_dtos import AgentAcceptedResponseDto

TRIGGERED_BY = "LANGGRAPH_AGENT"


class AgentOrchestratorFacade:
    """Closed-layer business facade: audit + LangGraph workflow stubs + MCP tools."""

    def __init__(
        self,
        repository: AgentRunRepository,
        blast_radius: BlastRadiusWorkflow,
        backpressure: BackpressureWorkflow,
        incident_rca: IncidentRcaWorkflow,
        chaos_sim: ChaosSimWorkflow,
    ) -> None:
        self._repository = repository
        self._blast_radius = blast_radius
        self._backpressure = backpressure
        self._incident_rca = incident_rca
        self._chaos_sim = chaos_sim

    def analyze_blast_radius(self, dto: BlastRadiusAnalyzeRequestDto) -> AgentAcceptedResponseDto:
        return self._accept_and_run(
            agent_type="BLAST_RADIUS",
            dry_run=dto.dry_run,
            approval_token=dto.approval_token,
            request_payload=dto.model_dump(by_alias=True),
            workflow=self._blast_radius,
        )

    def assess_backpressure(self, dto: BackpressureAssessRequestDto) -> AgentAcceptedResponseDto:
        return self._accept_and_run(
            agent_type="BACKPRESSURE",
            dry_run=dto.dry_run,
            approval_token=dto.approval_token,
            request_payload=dto.model_dump(by_alias=True),
            workflow=self._backpressure,
        )

    def investigate_incident_rca(
        self, dto: IncidentRcaInvestigateRequestDto
    ) -> AgentAcceptedResponseDto:
        if not dto.dry_run and not dto.approval_token:
            raise ValueError("ADR-032: dryRun=false mutations require Maker-Checker approvalToken")
        return self._accept_and_run(
            agent_type="INCIDENT_RCA",
            dry_run=dto.dry_run,
            approval_token=dto.approval_token,
            request_payload=dto.model_dump(by_alias=True),
            workflow=self._incident_rca,
        )

    def execute_chaos_sim(self, dto: ChaosSimExecuteRequestDto) -> AgentAcceptedResponseDto:
        return self._accept_and_run(
            agent_type="CHAOS_SIM",
            dry_run=dto.dry_run,
            approval_token=dto.approval_token,
            request_payload=dto.model_dump(by_alias=True),
            workflow=self._chaos_sim,
        )

    def _accept_and_run(
        self,
        *,
        agent_type: str,
        dry_run: bool,
        approval_token: str | None,
        request_payload: dict,
        workflow: object,
    ) -> AgentAcceptedResponseDto:
        accepted_at = datetime.now(timezone.utc)
        started = time.perf_counter()
        run_id_dto = self._repository.begin_run(
            PersistAgentRunDto(
                agent_type=agent_type,
                dry_run=dry_run,
                request_params=json.dumps(request_payload),
                approval_token=approval_token,
                triggered_by=TRIGGERED_BY,
            )
        )
        self._repository.mark_running(run_id_dto)
        status = "COMPLETED"
        try:
            command = WorkflowExecuteCommandDto(
                agent_type=agent_type,
                dry_run=dry_run,
                approval_token=approval_token,
                request_payload=request_payload,
            )
            result = workflow.execute(command)  # type: ignore[attr-defined]
            duration_ms = int((time.perf_counter() - started) * 1000)
            self._repository.complete_run(
                PersistRunCompleteDto(
                    run_id=run_id_dto.run_id,
                    mcp_tools_invoked=json.dumps(result.mcp_invocations),
                    result_summary=json.dumps(result.summary),
                    duration_ms=duration_ms,
                )
            )
        except Exception as exc:  # noqa: BLE001
            status = "FAILED"
            duration_ms = int((time.perf_counter() - started) * 1000)
            self._repository.fail_run(
                PersistRunFailDto(
                    run_id=run_id_dto.run_id,
                    error_details=str(exc),
                    duration_ms=duration_ms,
                )
            )

        return AgentAcceptedResponseDto(
            wfExecId=run_id_dto.run_id,
            agentType=agent_type,
            status=status,
            dryRun=dry_run,
            triggeredBy=TRIGGERED_BY,
            acceptedAt=accepted_at,
        )
