from __future__ import annotations

from fx_agent_orchestrator.business.dtos.agent_command_dtos import BlastRadiusCommandDto
from fx_agent_orchestrator.business.dtos.workflow_dtos import WorkflowResultDto
from fx_agent_orchestrator.services.adapters.topology_mcp_client import TopologyMcpClient
from fx_agent_orchestrator.services.dtos.mcp_call_dtos import McpToolCallDto


class BlastRadiusWorkflow:
    """LangGraph-ready stub: get_service_dependencies → calculate_blast_radius → diff_topology_snapshots."""

    def __init__(self, mcp_client: TopologyMcpClient) -> None:
        self._mcp = mcp_client

    def execute(self, dto: BlastRadiusCommandDto) -> WorkflowResultDto:
        invocations = [
            self._mcp.invoke_tool(
                McpToolCallDto(
                    tool_name="get_service_dependencies",
                    payload={
                        "serviceName": dto.service_name or "",
                        "layer": "UNIFIED",
                        "depth": dto.depth,
                    },
                )
            ),
            self._mcp.invoke_tool(
                McpToolCallDto(
                    tool_name="calculate_blast_radius",
                    payload={
                        "serviceName": dto.service_name or "",
                        "ccyPair": dto.ccy_pair or "",
                    },
                )
            ),
            self._mcp.invoke_tool(
                McpToolCallDto(
                    tool_name="diff_topology_snapshots",
                    payload={
                        k: v
                        for k, v in {"fromTs": dto.from_ts, "toTs": dto.to_ts}.items()
                        if v is not None
                    },
                )
            ),
        ]
        summary = {
            "agent": "BLAST_RADIUS",
            "dryRun": dto.dry_run,
            "serviceName": dto.service_name,
            "ccyPair": dto.ccy_pair,
            "toolsInvoked": [
                "get_service_dependencies",
                "calculate_blast_radius",
                "diff_topology_snapshots",
            ],
            "graph": "stub:linear-langgraph",
        }
        return WorkflowResultDto(
            mcp_invocations=[item.model_dump() for item in invocations],
            summary=summary,
        )
