from __future__ import annotations

from typing import Any, Optional
from uuid import UUID

from pydantic import BaseModel, ConfigDict


class ToolInvokeCommandDto(BaseModel):
    model_config = ConfigDict(frozen=True)

    tool_name: str
    safety_tier: str
    dry_run: bool
    request_payload: Any


class AuditBeginDto(BaseModel):
    model_config = ConfigDict(frozen=True)

    tool_name: str
    request_payload: Any
    safety_tier: str
    dry_run: bool
    triggered_by: str = "MCP_CLIENT"


class AuditCompleteDto(BaseModel):
    model_config = ConfigDict(frozen=True)

    invocation_id: UUID
    response_payload: Any
    duration_ms: int


class AuditFailDto(BaseModel):
    model_config = ConfigDict(frozen=True)

    invocation_id: UUID
    error_details: str
    duration_ms: int
    response_payload: Optional[Any] = None
