from __future__ import annotations

import os

from fastapi import FastAPI

from fx_agent_orchestrator.business.facades.agent_orchestrator_facade import AgentOrchestratorFacade
from fx_agent_orchestrator.business.workflows.backpressure_workflow import BackpressureWorkflow
from fx_agent_orchestrator.business.workflows.blast_radius_workflow import BlastRadiusWorkflow
from fx_agent_orchestrator.business.workflows.chaos_sim_workflow import ChaosSimWorkflow
from fx_agent_orchestrator.business.workflows.incident_rca_workflow import IncidentRcaWorkflow
from fx_agent_orchestrator.persistence.repositories.agent_run_repository import AgentRunRepository
from fx_agent_orchestrator.presentation.controllers.agent_orchestrator_controller import (
    AgentOrchestratorController,
)
from fx_agent_orchestrator.services.adapters.topology_mcp_client import TopologyMcpClient


class AgentOrchestratorAppFactory:
    """Class-based application wiring (ADR-034 — zero module-level procedural bootstrap)."""

    def __init__(self) -> None:
        self._mcp_base_url = os.getenv("FX_MCP_BASE_URL", "http://localhost:8091")
        self._database_url = os.getenv(
            "FX_AGENT_ORCHESTRATOR_DB_URL",
            "sqlite:///./fx_agent_orchestrator.db",
        )

    def create(self) -> FastAPI:
        repository = AgentRunRepository(self._database_url)
        mcp_client = TopologyMcpClient(self._mcp_base_url)
        facade = AgentOrchestratorFacade(
            repository=repository,
            blast_radius=BlastRadiusWorkflow(mcp_client),
            backpressure=BackpressureWorkflow(mcp_client),
            incident_rca=IncidentRcaWorkflow(mcp_client),
            chaos_sim=ChaosSimWorkflow(mcp_client),
        )
        controller = AgentOrchestratorController(facade)

        application = FastAPI(
            title="fx-agent-orchestrator-service",
            version="1.0.0",
            description="Python LangGraph dual-stack agent orchestrator (ADR-032/034)",
        )
        application.include_router(controller.router)
        return application


app = AgentOrchestratorAppFactory().create()
