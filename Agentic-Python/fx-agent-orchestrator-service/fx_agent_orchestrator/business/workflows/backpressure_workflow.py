from __future__ import annotations

from fx_agent_orchestrator.business.dtos.agent_command_dtos import BackpressureCommandDto
from fx_agent_orchestrator.business.dtos.workflow_dtos import WorkflowResultDto
from fx_agent_orchestrator.services.adapters.topology_mcp_client import TopologyMcpClient
from fx_agent_orchestrator.services.dtos.mcp_call_dtos import McpToolCallDto


class BackpressureWorkflow:
    """LangGraph-ready stub: lag → ZGC/CPU → recommend_or_apply_scaling."""

    def __init__(self, mcp_client: TopologyMcpClient) -> None:
        self._mcp = mcp_client

    def execute(self, dto: BackpressureCommandDto) -> WorkflowResultDto:
        invocations = [
            self._mcp.invoke_tool(
                McpToolCallDto(
                    tool_name="get_kafka_consumer_lag",
                    payload={"consumerGroup": dto.consumer_group or ""},
                )
            ),
            self._mcp.invoke_tool(
                McpToolCallDto(
                    tool_name="get_pod_zgc_and_cpu_metrics",
                    payload={"serviceName": dto.service_name or ""},
                )
            ),
            self._mcp.invoke_tool(
                McpToolCallDto(
                    tool_name="recommend_or_apply_scaling",
                    payload={
                        "serviceName": dto.service_name or "",
                        "applyScaling": dto.apply_scaling,
                        "dryRun": dto.dry_run,
                        "approvalToken": dto.approval_token or "",
                    },
                )
            ),
        ]
        summary = {
            "agent": "BACKPRESSURE",
            "dryRun": dto.dry_run,
            "consumerGroup": dto.consumer_group,
            "serviceName": dto.service_name,
            "applyScaling": dto.apply_scaling and not dto.dry_run,
            "toolsInvoked": [
                "get_kafka_consumer_lag",
                "get_pod_zgc_and_cpu_metrics",
                "recommend_or_apply_scaling",
            ],
            "graph": "stub:linear-langgraph",
        }
        return WorkflowResultDto(
            mcp_invocations=[item.model_dump() for item in invocations],
            summary=summary,
        )
