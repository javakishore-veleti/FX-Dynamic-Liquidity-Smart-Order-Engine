from __future__ import annotations

from fx_agent_orchestrator.business.dtos.workflow_dtos import WorkflowExecuteCommandDto, WorkflowResultDto
from fx_agent_orchestrator.presentation.dtos.agent_request_dtos import ChaosSimExecuteRequestDto
from fx_agent_orchestrator.services.adapters.topology_mcp_client import TopologyMcpClient
from fx_agent_orchestrator.services.dtos.mcp_call_dtos import McpToolCallDto


class ChaosSimWorkflow:
    """LangGraph-ready stub: synthetic seed → skewed load → poll_workflow_status."""

    def __init__(self, mcp_client: TopologyMcpClient) -> None:
        self._mcp = mcp_client

    def execute(self, dto: WorkflowExecuteCommandDto) -> WorkflowResultDto:
        request = ChaosSimExecuteRequestDto.model_validate(dto.request_payload)
        seed_payload: dict = {
            "numBatches": request.num_batches,
            "batchSize": request.batch_size,
            "dryRun": request.dry_run,
        }
        if request.datasets is not None:
            seed_payload["datasets"] = request.datasets

        load_result = self._mcp.invoke_tool(
            McpToolCallDto(
                tool_name="trigger_skewed_load_sim",
                payload={
                    "targetRps": request.target_rps,
                    "durationSec": request.duration_sec,
                    "ccyPair": request.ccy_pair,
                    "dryRun": request.dry_run,
                },
            )
        )
        nested_wf = ""
        if isinstance(load_result.response, dict):
            nested_wf = str(load_result.response.get("wfExecId") or "")

        invocations = [
            self._mcp.invoke_tool(
                McpToolCallDto(tool_name="trigger_synthetic_seed", payload=seed_payload)
            ),
            load_result,
            self._mcp.invoke_tool(
                McpToolCallDto(
                    tool_name="poll_workflow_status",
                    payload={"wfExecId": nested_wf, "dryRun": request.dry_run},
                )
            ),
        ]
        summary = {
            "agent": "CHAOS_SIM",
            "dryRun": request.dry_run,
            "ccyPair": request.ccy_pair,
            "targetRps": request.target_rps,
            "toolsInvoked": [
                "trigger_synthetic_seed",
                "trigger_skewed_load_sim",
                "poll_workflow_status",
            ],
            "graph": "stub:linear-langgraph",
        }
        return WorkflowResultDto(
            mcp_invocations=[item.model_dump() for item in invocations],
            summary=summary,
        )
