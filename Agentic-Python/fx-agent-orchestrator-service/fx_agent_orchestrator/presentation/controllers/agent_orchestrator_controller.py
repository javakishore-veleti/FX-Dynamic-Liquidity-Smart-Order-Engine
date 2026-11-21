from __future__ import annotations

from fastapi import APIRouter, HTTPException, status

from fx_agent_orchestrator.business.dtos.agent_command_dtos import (
    AgentAcceptedResultDto,
    BackpressureCommandDto,
    BlastRadiusCommandDto,
    ChaosSimCommandDto,
    IncidentRcaCommandDto,
)
from fx_agent_orchestrator.business.facades.agent_orchestrator_facade import AgentOrchestratorFacade
from fx_agent_orchestrator.presentation.dtos.agent_request_dtos import (
    BackpressureAssessRequestDto,
    BlastRadiusAnalyzeRequestDto,
    ChaosSimExecuteRequestDto,
    IncidentRcaInvestigateRequestDto,
)
from fx_agent_orchestrator.presentation.dtos.agent_response_dtos import (
    AgentAcceptedResponseDto,
    AgentErrorResponseDto,
)


class AgentOrchestratorController:
    """Presentation adapter: maps frozen API DTOs ↔ business command DTOs."""

    def __init__(self, facade: AgentOrchestratorFacade) -> None:
        self._facade = facade
        self._router = APIRouter(prefix="/api/v1/agents", tags=["agents"])
        self._register_routes()

    @property
    def router(self) -> APIRouter:
        return self._router

    def _register_routes(self) -> None:
        self._router.add_api_route(
            "/blast-radius/analyze",
            self.analyze_blast_radius,
            methods=["POST"],
            status_code=status.HTTP_202_ACCEPTED,
            response_model=AgentAcceptedResponseDto,
        )
        self._router.add_api_route(
            "/backpressure/assess",
            self.assess_backpressure,
            methods=["POST"],
            status_code=status.HTTP_202_ACCEPTED,
            response_model=AgentAcceptedResponseDto,
        )
        self._router.add_api_route(
            "/incident-rca/investigate",
            self.investigate_incident_rca,
            methods=["POST"],
            status_code=status.HTTP_202_ACCEPTED,
            response_model=AgentAcceptedResponseDto,
        )
        self._router.add_api_route(
            "/chaos-sim/execute",
            self.execute_chaos_sim,
            methods=["POST"],
            status_code=status.HTTP_202_ACCEPTED,
            response_model=AgentAcceptedResponseDto,
        )

    def analyze_blast_radius(
        self, body: BlastRadiusAnalyzeRequestDto | None = None
    ) -> AgentAcceptedResponseDto:
        request = body or BlastRadiusAnalyzeRequestDto()
        result = self._facade.analyze_blast_radius(
            BlastRadiusCommandDto(
                service_name=request.service_name,
                ccy_pair=request.ccy_pair,
                from_ts=request.from_ts,
                to_ts=request.to_ts,
                depth=request.depth,
                dry_run=request.dry_run,
                approval_token=request.approval_token,
            )
        )
        return self._to_response(result)

    def assess_backpressure(
        self, body: BackpressureAssessRequestDto | None = None
    ) -> AgentAcceptedResponseDto:
        request = body or BackpressureAssessRequestDto()
        result = self._facade.assess_backpressure(
            BackpressureCommandDto(
                consumer_group=request.consumer_group,
                service_name=request.service_name,
                apply_scaling=request.apply_scaling,
                dry_run=request.dry_run,
                approval_token=request.approval_token,
            )
        )
        return self._to_response(result)

    def investigate_incident_rca(
        self, body: IncidentRcaInvestigateRequestDto | None = None
    ) -> AgentAcceptedResponseDto:
        request = body or IncidentRcaInvestigateRequestDto()
        try:
            result = self._facade.investigate_incident_rca(
                IncidentRcaCommandDto(
                    service_name=request.service_name,
                    min_latency_ms=request.min_latency_ms,
                    limit=request.limit,
                    ccy_pair=request.ccy_pair,
                    lp_id=request.lp_id,
                    reason=request.reason,
                    dry_run=request.dry_run,
                    approval_token=request.approval_token,
                )
            )
        except ValueError as exc:
            raise HTTPException(
                status_code=status.HTTP_400_BAD_REQUEST,
                detail=AgentErrorResponseDto(error=str(exc)).model_dump(),
            ) from exc
        return self._to_response(result)

    def execute_chaos_sim(
        self, body: ChaosSimExecuteRequestDto | None = None
    ) -> AgentAcceptedResponseDto:
        request = body or ChaosSimExecuteRequestDto()
        result = self._facade.execute_chaos_sim(
            ChaosSimCommandDto(
                num_batches=request.num_batches,
                batch_size=request.batch_size,
                datasets=request.datasets,
                target_rps=request.target_rps,
                duration_sec=request.duration_sec,
                ccy_pair=request.ccy_pair,
                dry_run=request.dry_run,
                approval_token=request.approval_token,
            )
        )
        return self._to_response(result)

    def _to_response(self, result: AgentAcceptedResultDto) -> AgentAcceptedResponseDto:
        return AgentAcceptedResponseDto(
            wfExecId=result.wf_exec_id,
            agentType=result.agent_type,
            status=result.status,
            dryRun=result.dry_run,
            triggeredBy=result.triggered_by,
            acceptedAt=result.accepted_at,
        )
