from __future__ import annotations

import logging

import httpx

from fx_agent_orchestrator.services.dtos.mcp_call_dtos import McpToolCallDto, McpToolCallResultDto

logger = logging.getLogger(__name__)


class TopologyMcpClient:
    """HTTP adapter for fx-topology-mcp-server /mcp/tools/* (default :8091)."""

    def __init__(self, base_url: str, timeout_seconds: float = 10.0) -> None:
        self._base_url = base_url.rstrip("/")
        self._timeout = timeout_seconds

    def invoke_tool(self, dto: McpToolCallDto) -> McpToolCallResultDto:
        url = f"{self._base_url}/mcp/tools/{dto.tool_name}"
        try:
            with httpx.Client(timeout=self._timeout) as client:
                response = client.post(url, json=dto.payload)
                response.raise_for_status()
                body = response.json() if response.content else {}
                return McpToolCallResultDto(tool=dto.tool_name, ok=True, response=body)
        except Exception as exc:  # noqa: BLE001 — graceful MCP degradation
            logger.warning("MCP tool %s failed: %s", dto.tool_name, exc)
            return McpToolCallResultDto(tool=dto.tool_name, ok=False, error=str(exc))
