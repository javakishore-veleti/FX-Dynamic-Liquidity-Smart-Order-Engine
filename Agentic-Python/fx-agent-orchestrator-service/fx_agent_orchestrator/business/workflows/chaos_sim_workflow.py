from __future__ import annotations

from fx_agent_orchestrator.business.dtos.agent_command_dtos import ChaosSimCommandDto
from fx_agent_orchestrator.business.dtos.workflow_dtos import WorkflowResultDto
from fx_agent_orchestrator.services.adapters.topology_mcp_client import TopologyMcpClient
from fx_agent_orchestrator.services.dtos.mcp_call_dtos import McpToolCallDto


class ChaosSimWorkflow:
    """LangGraph-ready stub: synthetic seed → skewed load → poll_workflow_status."""

    def __init__(self, mcp_client: TopologyMcpClient) -> None:
        self._mcp = mcp_client

    def execute(self, dto: ChaosSimCommandDto) -> WorkflowResultDto:
        seed_payload: dict = {
            "numBatches": dto.num_batches,
            "batchSize": dto.batch_size,
            "dryRun": dto.dry_run,
        }
        if dto.datasets is not None:
            seed_payload["datasets"] = dto.datasets

        load_result = self._mcp.invoke_tool(
            McpToolCallDto(
                tool_name="trigger_skewed_load_sim",
                payload={
                    "targetRps": dto.target_rps,
                    "durationSec": dto.duration_sec,
                    "ccyPair": dto.ccy_pair,
                    "dryRun": dto.dry_run,
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
                    payload={"wfExecId": nested_wf, "dryRun": dto.dry_run},
                )
            ),
        ]
        summary = {
            "agent": "CHAOS_SIM",
            "dryRun": dto.dry_run,
            "ccyPair": dto.ccy_pair,
            "targetRps": dto.target_rps,
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
