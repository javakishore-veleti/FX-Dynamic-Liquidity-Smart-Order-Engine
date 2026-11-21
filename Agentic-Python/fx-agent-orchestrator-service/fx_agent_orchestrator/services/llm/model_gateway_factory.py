from __future__ import annotations

import json
import logging
from typing import Any, Protocol

from fx_agent_orchestrator.services.dtos.llm_dtos import (
    LlmCompletionRequestDto,
    LlmCompletionResultDto,
    ModelProviderConfigDto,
)

logger = logging.getLogger(__name__)


class ChatModelClient(Protocol):
    def complete(self, request: LlmCompletionRequestDto) -> LlmCompletionResultDto: ...


class StubChatModelClient:
    """Deterministic offline client when Bedrock/HF/Ollama SDKs or runtimes are absent."""

    def __init__(self, config: ModelProviderConfigDto) -> None:
        self._config = config

    def complete(self, request: LlmCompletionRequestDto) -> LlmCompletionResultDto:
        structured = {
            "agentInsight": "stub-completion",
            "tier": request.tier,
            "schema": request.response_schema_name,
            "promptChars": len(request.user_prompt),
            "guardrailId": self._config.guardrail_id,
        }
        content = json.dumps(structured)
        return LlmCompletionResultDto(
            content=content,
            provider=self._config.provider,
            model_id=self._config.model_id,
            structured=structured,
            stubbed=True,
        )


class ModelGatewayFactory:
    """Closed-layer factory: routes chat models by provider without agent graph changes."""

    def create_chat_model(self, config_dto: ModelProviderConfigDto) -> ChatModelClient:
        if config_dto.provider == "BEDROCK":
            client = self._try_bedrock(config_dto)
            if client is not None:
                return client
        elif config_dto.provider == "HUGGINGFACE":
            client = self._try_huggingface(config_dto)
            if client is not None:
                return client
        else:
            client = self._try_ollama(config_dto)
            if client is not None:
                return client
        logger.info(
            "LLM provider %s model %s unavailable — using StubChatModelClient",
            config_dto.provider,
            config_dto.model_id,
        )
        return StubChatModelClient(config_dto)

    def _try_bedrock(self, config: ModelProviderConfigDto) -> ChatModelClient | None:
        try:
            from langchain_aws import ChatBedrockConverse  # type: ignore
        except Exception:  # noqa: BLE001
            return None
        return _LangChainAdapter(
            config,
            ChatBedrockConverse(
                model=config.model_id,
                region_name=config.region,
                temperature=config.temperature,
            ),
        )

    def _try_huggingface(self, config: ModelProviderConfigDto) -> ChatModelClient | None:
        try:
            from langchain_huggingface import ChatHuggingFace, HuggingFaceEndpoint  # type: ignore
        except Exception:  # noqa: BLE001
            return None
        endpoint = HuggingFaceEndpoint(
            repo_id=config.model_id,
            temperature=config.temperature,
            huggingfacehub_api_token=None,
        )
        return _LangChainAdapter(config, ChatHuggingFace(llm=endpoint))

    def _try_ollama(self, config: ModelProviderConfigDto) -> ChatModelClient | None:
        try:
            from langchain_ollama import ChatOllama  # type: ignore
        except Exception:  # noqa: BLE001
            return None
        return _LangChainAdapter(
            config,
            ChatOllama(
                model=config.model_id,
                base_url=config.base_url or "http://localhost:11434",
                temperature=config.temperature,
            ),
        )


class _LangChainAdapter:
    def __init__(self, config: ModelProviderConfigDto, llm: Any) -> None:
        self._config = config
        self._llm = llm

    def complete(self, request: LlmCompletionRequestDto) -> LlmCompletionResultDto:
        messages = [
            ("system", request.system_prompt),
            ("human", request.user_prompt),
        ]
        result = self._llm.invoke(messages)
        content = getattr(result, "content", str(result))
        structured: dict[str, Any]
        try:
            structured = json.loads(content) if isinstance(content, str) else {"raw": content}
        except json.JSONDecodeError:
            structured = {"text": content}
        return LlmCompletionResultDto(
            content=content if isinstance(content, str) else str(content),
            provider=self._config.provider,
            model_id=self._config.model_id,
            structured=structured,
            stubbed=False,
        )
