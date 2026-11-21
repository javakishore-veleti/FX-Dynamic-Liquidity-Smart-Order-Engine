package com.jk.apps.banking.fx.sor.agent.llm;

public record LlmCompletionResponse(
        String content,
        String provider,
        String modelId
) {
}
