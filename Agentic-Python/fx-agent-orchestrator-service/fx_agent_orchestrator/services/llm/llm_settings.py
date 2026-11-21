from __future__ import annotations

import os

from fx_agent_orchestrator.services.dtos.llm_dtos import ModelProvider, ModelProviderConfigDto, ModelTier


class LlmSettings:
    """Env-backed LLM runtime settings (DTO-only factory inputs)."""

    def __init__(self) -> None:
        self.provider: ModelProvider = self._provider(
            os.getenv("LLM_PROVIDER", "LOCAL_OLLAMA")
        )
        self.bedrock_region = os.getenv("BEDROCK_REGION", "us-east-1")
        self.bedrock_reasoning = os.getenv(
            "BEDROCK_REASONING_MODEL",
            "us.anthropic.claude-3-7-sonnet-20250219-v1:0",
        )
        self.bedrock_fast = os.getenv(
            "BEDROCK_FAST_MODEL",
            "us.amazon.nova-lite-v1:0",
        )
        self.bedrock_volume = os.getenv(
            "BEDROCK_VOLUME_MODEL",
            "us.anthropic.claude-3-5-haiku-20241022-v1:0",
        )
        self.bedrock_embedding = os.getenv(
            "BEDROCK_EMBEDDING_MODEL",
            "amazon.titan-embed-text-v2:0",
        )
        self.bedrock_guardrail_id = os.getenv("BEDROCK_GUARDRAIL_ID") or None
        self.hf_endpoint = os.getenv(
            "HF_ENDPOINT_URL",
            "https://api-inference.huggingface.co",
        )
        self.hf_reasoning = os.getenv(
            "HF_REASONING_MODEL",
            "Qwen/Qwen2.5-72B-Instruct",
        )
        self.hf_fast = os.getenv(
            "HF_FAST_MODEL",
            "meta-llama/Llama-3.3-70B-Instruct",
        )
        self.hf_volume = os.getenv(
            "HF_VOLUME_MODEL",
            "meta-llama/Llama-3.1-8B-Instruct",
        )
        self.hf_embedding = os.getenv("HF_EMBEDDING_MODEL", "BAAI/bge-m3")
        self.ollama_base_url = os.getenv(
            "LOCAL_OLLAMA_BASE_URL",
            "http://localhost:11434",
        )
        self.ollama_reasoning = os.getenv("LOCAL_REASONING_MODEL", "qwen2.5:14b")
        self.ollama_fast = os.getenv("LOCAL_FAST_MODEL", "llama3.3:8b")
        self.ollama_volume = os.getenv("LOCAL_VOLUME_MODEL", "mistral:7b")

    def config_for_tier(self, tier: ModelTier) -> ModelProviderConfigDto:
        if self.provider == "BEDROCK":
            model_id = {
                "REASONING": self.bedrock_reasoning,
                "FAST": self.bedrock_fast,
                "VOLUME": self.bedrock_volume,
                "EMBEDDING": self.bedrock_embedding,
            }[tier]
            return ModelProviderConfigDto(
                provider="BEDROCK",
                model_id=model_id,
                region=self.bedrock_region,
                guardrail_id=self.bedrock_guardrail_id,
            )
        if self.provider == "HUGGINGFACE":
            model_id = {
                "REASONING": self.hf_reasoning,
                "FAST": self.hf_fast,
                "VOLUME": self.hf_volume,
                "EMBEDDING": self.hf_embedding,
            }[tier]
            return ModelProviderConfigDto(
                provider="HUGGINGFACE",
                model_id=model_id,
                base_url=self.hf_endpoint,
            )
        model_id = {
            "REASONING": self.ollama_reasoning,
            "FAST": self.ollama_fast,
            "VOLUME": self.ollama_volume,
            "EMBEDDING": self.ollama_volume,
        }[tier]
        return ModelProviderConfigDto(
            provider="LOCAL_OLLAMA",
            model_id=model_id,
            base_url=self.ollama_base_url,
        )

    @staticmethod
    def _provider(raw: str) -> ModelProvider:
        value = raw.strip().upper()
        if value in {"BEDROCK", "HUGGINGFACE", "LOCAL_OLLAMA"}:
            return value  # type: ignore[return-value]
        return "LOCAL_OLLAMA"
