from __future__ import annotations

from typing import Any, Optional

from pydantic import BaseModel, ConfigDict


class McpToolCallDto(BaseModel):
    model_config = ConfigDict(frozen=True)

    tool_name: str
    payload: dict[str, Any]


class McpToolCallResultDto(BaseModel):
    model_config = ConfigDict(frozen=True)

    tool: str
    ok: bool
    response: Optional[Any] = None
    error: Optional[str] = None
