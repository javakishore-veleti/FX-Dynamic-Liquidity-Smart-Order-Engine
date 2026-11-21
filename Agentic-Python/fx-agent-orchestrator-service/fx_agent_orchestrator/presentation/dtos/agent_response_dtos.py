from __future__ import annotations

from datetime import datetime
from uuid import UUID

from pydantic import BaseModel, ConfigDict, Field


class AgentAcceptedResponseDto(BaseModel):
    model_config = ConfigDict(frozen=True, populate_by_name=True)

    wf_exec_id: UUID = Field(alias="wfExecId")
    agent_type: str = Field(alias="agentType")
    status: str
    dry_run: bool = Field(alias="dryRun")
    triggered_by: str = Field(alias="triggeredBy")
    accepted_at: datetime = Field(alias="acceptedAt")


class AgentErrorResponseDto(BaseModel):
    model_config = ConfigDict(frozen=True)

    error: str
