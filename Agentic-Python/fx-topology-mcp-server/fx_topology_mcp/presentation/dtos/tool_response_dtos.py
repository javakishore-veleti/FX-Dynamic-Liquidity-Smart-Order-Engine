from __future__ import annotations

from typing import Any, List, Optional

from pydantic import BaseModel, ConfigDict


class McpToolResultDto(BaseModel):
    model_config = ConfigDict(frozen=True)

    success: bool
    tool_name: str
    data: Optional[Any] = None
    error: Optional[str] = None
    upstream_status: Optional[str] = None
    duration_ms: Optional[int] = None


class McpToolDescriptorDto(BaseModel):
    model_config = ConfigDict(frozen=True)

    name: str
    description: str
    safety_tier: str
    backend: str
    path: str


class McpCatalogDto(BaseModel):
    model_config = ConfigDict(frozen=True)

    server: str
    transport: str
    tools: List[McpToolDescriptorDto]
