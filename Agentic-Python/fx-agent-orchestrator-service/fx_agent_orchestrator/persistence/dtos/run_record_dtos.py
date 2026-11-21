from __future__ import annotations

from typing import Optional
from uuid import UUID

from pydantic import BaseModel, ConfigDict


class PersistAgentRunDto(BaseModel):
    model_config = ConfigDict(frozen=True)

    agent_type: str
    dry_run: bool
    request_params: str
    approval_token: Optional[str] = None
    triggered_by: str = "LANGGRAPH_AGENT"


class PersistRunCompleteDto(BaseModel):
    model_config = ConfigDict(frozen=True)

    run_id: UUID
    mcp_tools_invoked: str
    result_summary: str
    duration_ms: int


class PersistRunFailDto(BaseModel):
    model_config = ConfigDict(frozen=True)

    run_id: UUID
    error_details: str
    duration_ms: int


class AgentRunIdDto(BaseModel):
    model_config = ConfigDict(frozen=True)

    run_id: UUID
