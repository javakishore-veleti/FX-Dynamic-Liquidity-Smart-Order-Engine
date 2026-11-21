from __future__ import annotations

from fastapi import APIRouter

from fx_topology_mcp.business.dtos.tool_contract_dtos import CatalogRequestDto, McpCatalogDto
from fx_topology_mcp.business.facades.mcp_tool_catalog import McpToolCatalog


class McpCatalogController:
    def __init__(self, catalog: McpToolCatalog) -> None:
        self._catalog = catalog
        self.router = APIRouter(tags=["mcp-catalog"])
        self.router.add_api_route("/mcp", self.get_catalog, methods=["GET"], response_model=McpCatalogDto)

    def get_catalog(self) -> McpCatalogDto:
        return self._catalog.catalog(CatalogRequestDto())
