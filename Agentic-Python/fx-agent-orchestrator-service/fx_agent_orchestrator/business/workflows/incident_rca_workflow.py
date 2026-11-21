from __future__ import annotations

from fx_agent_orchestrator.business.dtos.agent_command_dtos import IncidentRcaCommandDto
from fx_agent_orchestrator.business.dtos.workflow_dtos import WorkflowResultDto
from fx_agent_orchestrator.services.adapters.topology_mcp_client import TopologyMcpClient
from fx_agent_orchestrator.services.dtos.mcp_call_dtos import McpToolCallDto


class IncidentRcaWorkflow:
    """LangGraph-ready stub: outlier traces → IPC edge health → propose_or_execute_lp_halt."""

    def __init__(self, mcp_client: TopologyMcpClient) -> None:
        self._mcp = mcp_client

    def execute(self, dto: IncidentRcaCommandDto) -> WorkflowResultDto:
        halt_payload = {
            "lpId": dto.lp_id or "",
            "reason": dto.reason or "Incident RCA proposed halt",
            "dryRun": dto.dry_run,
        }
        if dto.approval_token:
            halt_payload["approvalToken"] = dto.approval_token

        invocations = [
            self._mcp.invoke_tool(
                McpToolCallDto(
                    tool_name="query_iceberg_outlier_traces",
                    payload={
                        "serviceName": dto.service_name or "",
                        "minLatencyMs": dto.min_latency_ms,
                        "limit": dto.limit,
                    },
                )
            ),
            self._mcp.invoke_tool(
                McpToolCallDto(
                    tool_name="get_ipc_edge_health",
                    payload={
                        "serviceName": dto.service_name or "",
                        "ccyPair": dto.ccy_pair or "",
                    },
                )
            ),
            self._mcp.invoke_tool(
                McpToolCallDto(
                    tool_name="propose_or_execute_lp_halt",
                    payload=halt_payload,
                )
            ),
        ]
        summary = {
            "agent": "INCIDENT_RCA",
            "dryRun": dto.dry_run,
            "serviceName": dto.service_name,
            "lpId": dto.lp_id,
            "toolsInvoked": [
                "query_iceberg_outlier_traces",
                "get_ipc_edge_health",
                "propose_or_execute_lp_halt",
            ],
            "governance": "ADR-032: live LP halt requires dryRun=false + Maker-Checker approvalToken",
            "graph": "stub:linear-langgraph",
        }
        return WorkflowResultDto(
            mcp_invocations=[item.model_dump() for item in invocations],
            summary=summary,
        )
