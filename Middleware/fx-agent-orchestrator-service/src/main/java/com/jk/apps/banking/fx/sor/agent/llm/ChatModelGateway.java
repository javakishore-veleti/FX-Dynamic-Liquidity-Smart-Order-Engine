package com.jk.apps.banking.fx.sor.agent.llm;

/**
 * Provider-agnostic chat completion gateway (Bedrock / Hugging Face / Ollama / Stub).
 */
public interface ChatModelGateway {

    LlmCompletionResponse complete(LlmCompletionRequest request);
}
