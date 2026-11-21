from __future__ import annotations

from typing import Any, List

from pydantic import BaseModel, ConfigDict


class WorkflowExecuteCommandDto(BaseModel):
    model_config = ConfigDict(frozen=True)

    agent_type: str
    dry_run: bool
    approval_token: str | None
    request_payload: Any


class WorkflowResultDto(BaseModel):
    model_config = ConfigDict(frozen=True)

    mcp_invocations: List[Any]
    summary: dict[str, Any]
