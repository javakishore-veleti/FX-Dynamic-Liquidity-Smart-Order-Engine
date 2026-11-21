from __future__ import annotations

from typing import Any, Literal

from pydantic import BaseModel, ConfigDict, Field

ModelProvider = Literal["BEDROCK", "HUGGINGFACE", "LOCAL_OLLAMA"]
ModelTier = Literal["REASONING", "FAST", "VOLUME", "EMBEDDING"]


class ModelProviderConfigDto(BaseModel):
    model_config = ConfigDict(frozen=True)

    provider: ModelProvider
    model_id: str
    region: str = "us-east-1"
    base_url: str | None = None
    temperature: float = 0.0
    guardrail_id: str | None = None


class LlmCompletionRequestDto(BaseModel):
    model_config = ConfigDict(frozen=True)

    system_prompt: str
    user_prompt: str
    tier: ModelTier = "REASONING"
    temperature: float = 0.0
    response_schema_name: str | None = None


class LlmCompletionResultDto(BaseModel):
    model_config = ConfigDict(frozen=True)

    content: str
    provider: ModelProvider
    model_id: str
    structured: dict[str, Any] = Field(default_factory=dict)
    stubbed: bool = False
