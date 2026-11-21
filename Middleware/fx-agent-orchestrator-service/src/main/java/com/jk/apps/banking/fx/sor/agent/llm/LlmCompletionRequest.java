package com.jk.apps.banking.fx.sor.agent.llm;

public record LlmCompletionRequest(
        String prompt,
        String systemPrompt,
        ModelTier modelTier,
        Double temperature
) {
    public LlmCompletionRequest {
        if (modelTier == null) {
            modelTier = ModelTier.REASONING;
        }
        if (temperature == null) {
            temperature = 0.2;
        }
    }
}
