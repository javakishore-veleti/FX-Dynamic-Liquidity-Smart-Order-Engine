from __future__ import annotations

import os
from pathlib import Path

from fastapi import FastAPI
from fastapi.responses import HTMLResponse

from fx_topology_mcp.business.facades.mcp_tool_catalog import McpToolCatalog
from fx_topology_mcp.business.facades.mcp_tool_facade import McpToolFacade
from fx_topology_mcp.persistence.repositories.mcp_invocation_repository import McpInvocationRepository
from fx_topology_mcp.presentation.controllers.mcp_catalog_controller import McpCatalogController
from fx_topology_mcp.presentation.controllers.mcp_tools_controller import McpToolsController
from fx_topology_mcp.observability.otel_setup import ObservabilitySetup
from fx_topology_mcp.services.adapters.admin_service_adapter import AdminServiceAdapter
from fx_topology_mcp.services.adapters.topology_service_adapter import TopologyServiceAdapter


class TopologyMcpServerApp:
    """Class-based FastAPI composition root (ADR-034)."""

    def __init__(self) -> None:
        self._topology_base_url = os.getenv("TOPOLOGY_BASE_URL", "http://localhost:8085")
        self._admin_base_url = os.getenv("ADMIN_BASE_URL", "http://localhost:8084")
        self._database_url = os.getenv(
            "MCP_AUDIT_DATABASE_URL",
            "sqlite:///./fx_topology_mcp_db.sqlite",
        )
        self._kafka_enabled = os.getenv("KAFKA_ENABLED", "false").lower() in {"1", "true", "yes"}

        topology_adapter = TopologyServiceAdapter(self._topology_base_url)
        admin_adapter = AdminServiceAdapter(self._admin_base_url)
        audit_repository = McpInvocationRepository(self._database_url)
        facade = McpToolFacade(
            topology_adapter=topology_adapter,
            admin_adapter=admin_adapter,
            audit_repository=audit_repository,
            kafka_enabled=self._kafka_enabled,
        )
        catalog = McpToolCatalog()

        self._app = FastAPI(
            title="fx-topology-mcp-server",
            version="1.0.0",
            description="Dual-stack MCP tool server (Python / ADR-033)",
        )
        observability = ObservabilitySetup("fx-topology-mcp-server")
        observability.configure(self._app)
        observability.instrument_app(self._app)
        catalog_controller = McpCatalogController(catalog)
        tools_controller = McpToolsController(facade)
        self._app.include_router(catalog_controller.router)
        self._app.include_router(tools_controller.router)
        home_html = (
            Path(__file__).resolve().parent / "presentation" / "home_page.html"
        ).read_text(encoding="utf-8")

        @self._app.get("/", response_class=HTMLResponse, include_in_schema=False)
        def home() -> str:
            return home_html

        @self._app.get("/health")
        def health() -> dict:
            return {"status": "UP", "service": "fx-topology-mcp-server"}

    @property
    def app(self) -> FastAPI:
        return self._app


app = TopologyMcpServerApp().app
