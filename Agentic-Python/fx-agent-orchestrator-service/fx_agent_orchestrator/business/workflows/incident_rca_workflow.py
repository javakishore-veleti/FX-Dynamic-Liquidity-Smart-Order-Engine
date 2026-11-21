from __future__ import annotations

from fx_agent_orchestrator.business.dtos.workflow_dtos import WorkflowExecuteCommandDto, WorkflowResultDto
from fx_agent_orchestrator.presentation.dtos.agent_request_dtos import IncidentRcaInvestigateRequestDto
from fx_agent_orchestrator.services.adapters.topology_mcp_client import TopologyMcpClient
from fx_agent_orchestrator.services.dtos.mcp_call_dtos import McpToolCallDto


class IncidentRcaWorkflow:
    """LangGraph-ready stub: outlier traces → IPC edge health → propose_or_execute_lp_halt."""

    def __init__(self, mcp_client: TopologyMcpClient) -> None:
        self._mcp = mcp_client

    def execute(self, dto: WorkflowExecuteCommandDto) -> WorkflowResultDto:
        request = IncidentRcaInvestigateRequestDto.model_validate(dto.request_payload)
        halt_payload = {
            "lpId": request.lp_id or "",
            "reason": request.reason or "Incident RCA proposed halt",
            "dryRun": request.dry_run,
        }
        if request.approval_token:
            halt_payload["approvalToken"] = request.approval_token

        invocations = [
            self._mcp.invoke_tool(
                McpToolCallDto(
                    tool_name="query_iceberg_outlier_traces",
                    payload={
                        "serviceName": request.service_name or "",
                        "minLatencyMs": request.min_latency_ms,
                        "limit": request.limit,
                    },
                )
            ),
            self._mcp.invoke_tool(
                McpToolCallDto(
                    tool_name="get_ipc_edge_health",
                    payload={
                        "serviceName": request.service_name or "",
                        "ccyPair": request.ccy_pair or "",
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
            "dryRun": request.dry_run,
            "serviceName": request.service_name,
            "lpId": request.lp_id,
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
