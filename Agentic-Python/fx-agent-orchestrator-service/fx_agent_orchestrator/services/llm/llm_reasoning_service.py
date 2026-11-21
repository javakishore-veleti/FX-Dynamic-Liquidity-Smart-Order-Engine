from __future__ import annotations

from fx_agent_orchestrator.services.dtos.llm_dtos import (
    LlmCompletionRequestDto,
    LlmCompletionResultDto,
    ModelTier,
)
from fx_agent_orchestrator.services.llm.llm_settings import LlmSettings
from fx_agent_orchestrator.services.llm.model_gateway_factory import ModelGatewayFactory


class LlmReasoningService:
    """DTO-only reasoning façade used by all 10 agent workflows."""

    def __init__(
        self,
        settings: LlmSettings | None = None,
        factory: ModelGatewayFactory | None = None,
    ) -> None:
        self._settings = settings or LlmSettings()
        self._factory = factory or ModelGatewayFactory()

    def complete(
        self,
        *,
        system_prompt: str,
        user_prompt: str,
        tier: ModelTier,
        response_schema_name: str | None = None,
        temperature: float = 0.0,
    ) -> LlmCompletionResultDto:
        config = self._settings.config_for_tier(tier)
        # temperature override stays on request; config remains frozen for provider/model
        client = self._factory.create_chat_model(config)
        return client.complete(
            LlmCompletionRequestDto(
                system_prompt=system_prompt,
                user_prompt=user_prompt,
                tier=tier,
                temperature=temperature,
                response_schema_name=response_schema_name,
            )
        )
