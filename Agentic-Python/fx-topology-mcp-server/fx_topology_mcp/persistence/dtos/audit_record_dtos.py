from __future__ import annotations

from typing import Any, Optional
from uuid import UUID

from pydantic import BaseModel, ConfigDict


class PersistInvocationDto(BaseModel):
    model_config = ConfigDict(frozen=True)

    tool_name: str
    request_payload: str
    safety_tier: str
    dry_run: bool
    triggered_by: str = "MCP_CLIENT"


class PersistCompleteDto(BaseModel):
    model_config = ConfigDict(frozen=True)

    invocation_id: UUID
    response_payload: str
    duration_ms: int


class PersistFailDto(BaseModel):
    model_config = ConfigDict(frozen=True)

    invocation_id: UUID
    error_details: str
    duration_ms: int
    response_payload: Optional[str] = None


class InvocationIdDto(BaseModel):
    model_config = ConfigDict(frozen=True)

    invocation_id: UUID
