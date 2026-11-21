from __future__ import annotations

from fx_agent_orchestrator.business.dtos.workflow_dtos import WorkflowExecuteCommandDto, WorkflowResultDto
from fx_agent_orchestrator.presentation.dtos.agent_request_dtos import BackpressureAssessRequestDto
from fx_agent_orchestrator.services.adapters.topology_mcp_client import TopologyMcpClient
from fx_agent_orchestrator.services.dtos.mcp_call_dtos import McpToolCallDto


class BackpressureWorkflow:
    """LangGraph-ready stub: lag → ZGC/CPU → recommend_or_apply_scaling."""

    def __init__(self, mcp_client: TopologyMcpClient) -> None:
        self._mcp = mcp_client

    def execute(self, dto: WorkflowExecuteCommandDto) -> WorkflowResultDto:
        request = BackpressureAssessRequestDto.model_validate(dto.request_payload)
        invocations = [
            self._mcp.invoke_tool(
                McpToolCallDto(
                    tool_name="get_kafka_consumer_lag",
                    payload={"consumerGroup": request.consumer_group or ""},
                )
            ),
            self._mcp.invoke_tool(
                McpToolCallDto(
                    tool_name="get_pod_zgc_and_cpu_metrics",
                    payload={"serviceName": request.service_name or ""},
                )
            ),
            self._mcp.invoke_tool(
                McpToolCallDto(
                    tool_name="recommend_or_apply_scaling",
                    payload={
                        "serviceName": request.service_name or "",
                        "applyScaling": request.apply_scaling,
                        "dryRun": request.dry_run,
                        "approvalToken": request.approval_token or "",
                    },
                )
            ),
        ]
        summary = {
            "agent": "BACKPRESSURE",
            "dryRun": request.dry_run,
            "consumerGroup": request.consumer_group,
            "serviceName": request.service_name,
            "applyScaling": request.apply_scaling and not request.dry_run,
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
