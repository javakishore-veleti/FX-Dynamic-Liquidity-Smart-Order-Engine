from __future__ import annotations

import json
import time
from datetime import datetime, timezone

from fx_agent_orchestrator.business.dtos.agent_command_dtos import (
    AgentAcceptedResultDto,
    BackpressureCommandDto,
    BlastRadiusCommandDto,
    ChaosSimCommandDto,
    IncidentRcaCommandDto,
)
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

    def analyze_blast_radius(self, dto: BlastRadiusCommandDto) -> AgentAcceptedResultDto:
        return self._accept_and_run(
            agent_type="BLAST_RADIUS",
            dry_run=dto.dry_run,
            approval_token=dto.approval_token,
            request_payload=dto.model_dump(),
            runner=lambda: self._blast_radius.execute(dto),
        )

    def assess_backpressure(self, dto: BackpressureCommandDto) -> AgentAcceptedResultDto:
        return self._accept_and_run(
            agent_type="BACKPRESSURE",
            dry_run=dto.dry_run,
            approval_token=dto.approval_token,
            request_payload=dto.model_dump(),
            runner=lambda: self._backpressure.execute(dto),
        )

    def investigate_incident_rca(self, dto: IncidentRcaCommandDto) -> AgentAcceptedResultDto:
        if not dto.dry_run and not dto.approval_token:
            raise ValueError("ADR-032: dryRun=false mutations require Maker-Checker approvalToken")
        return self._accept_and_run(
            agent_type="INCIDENT_RCA",
            dry_run=dto.dry_run,
            approval_token=dto.approval_token,
            request_payload=dto.model_dump(),
            runner=lambda: self._incident_rca.execute(dto),
        )

    def execute_chaos_sim(self, dto: ChaosSimCommandDto) -> AgentAcceptedResultDto:
        return self._accept_and_run(
            agent_type="CHAOS_SIM",
            dry_run=dto.dry_run,
            approval_token=dto.approval_token,
            request_payload=dto.model_dump(),
            runner=lambda: self._chaos_sim.execute(dto),
        )

    def _accept_and_run(
        self,
        *,
        agent_type: str,
        dry_run: bool,
        approval_token: str | None,
        request_payload: dict,
        runner: object,
    ) -> AgentAcceptedResultDto:
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
            result = runner()  # type: ignore[operator]
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

        return AgentAcceptedResultDto(
            wf_exec_id=run_id_dto.run_id,
            agent_type=agent_type,
            status=status,
            dry_run=dry_run,
            triggered_by=TRIGGERED_BY,
            accepted_at=accepted_at,
        )
