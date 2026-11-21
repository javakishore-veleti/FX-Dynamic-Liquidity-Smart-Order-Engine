package com.jk.apps.banking.fx.sor.agent.llm;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.List;
import java.util.Map;

/**
 * Routes chat completions through LiteLLM Proxy (OpenAI-compatible /v1/chat/completions).
 * Used when fx.llm.provider=OPENAI_COMPAT / LITELLM.
 */
public class OpenAiCompatibleChatModelGateway implements ChatModelGateway {

    private static final Logger log = LoggerFactory.getLogger(OpenAiCompatibleChatModelGateway.class);

    private final LlmProviderProperties properties;
    private final RestClient restClient;
    private final StubChatModelGateway fallback;

    public OpenAiCompatibleChatModelGateway(LlmProviderProperties properties, RestClient.Builder restClientBuilder) {
        this.properties = properties;
        String base = properties.getOpenaiCompatible().getBaseUrl();
        this.restClient = restClientBuilder.baseUrl(base).build();
        this.fallback = new StubChatModelGateway(properties, "OPENAI_COMPAT");
    }

    @Override
    public LlmCompletionResponse complete(LlmCompletionRequest request) {
        String model = resolveModel(request.modelTier());
        try {
            Map<String, Object> body = Map.of(
                    "model", model,
                    "temperature", request.temperature(),
                    "messages", List.of(
                            Map.of("role", "system", "content", nullToEmpty(request.systemPrompt())),
                            Map.of("role", "user", "content", nullToEmpty(request.prompt()))
                    )
            );
            @SuppressWarnings("unchecked")
            Map<String, Object> response = restClient.post()
                    .uri("/v1/chat/completions")
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Bearer " + properties.getOpenaiCompatible().getApiKey())
                    .body(body)
                    .retrieve()
                    .body(Map.class);
            String content = extractContent(response);
            return new LlmCompletionResponse(content, "OPENAI_COMPAT", model);
        } catch (RestClientException ex) {
            log.warn("LiteLLM gateway call failed ({}): {}", model, ex.getMessage());
            return fallback.complete(request);
        }
    }

    private String resolveModel(ModelTier tier) {
        LlmProviderProperties.OpenaiCompatible cfg = properties.getOpenaiCompatible();
        if (tier == null) {
            return cfg.getFastModel();
        }
        return switch (tier) {
            case REASONING -> cfg.getReasoningModel();
            case FAST, VOLUME -> cfg.getFastModel();
        };
    }

    @SuppressWarnings("unchecked")
    private static String extractContent(Map<String, Object> response) {
        if (response == null) {
            return "";
        }
        Object choices = response.get("choices");
        if (choices instanceof List<?> list && !list.isEmpty() && list.get(0) instanceof Map<?, ?> first) {
            Object message = first.get("message");
            if (message instanceof Map<?, ?> msg) {
                Object content = msg.get("content");
                return content == null ? "" : content.toString();
            }
        }
        return response.toString();
    }

    private static String nullToEmpty(String value) {
        return value == null ? "" : value;
    }
}
