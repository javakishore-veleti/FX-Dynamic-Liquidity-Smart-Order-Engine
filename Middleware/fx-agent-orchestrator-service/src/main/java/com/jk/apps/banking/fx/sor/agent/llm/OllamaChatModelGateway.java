package com.jk.apps.banking.fx.sor.agent.llm;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Local Ollama via RestClient POST {baseUrl}/api/chat.
 */
public class OllamaChatModelGateway implements ChatModelGateway {

    private static final Logger log = LoggerFactory.getLogger(OllamaChatModelGateway.class);

    private final LlmProviderProperties properties;
    private final RestClient restClient;
    private final StubChatModelGateway fallback;

    public OllamaChatModelGateway(LlmProviderProperties properties, RestClient.Builder restClientBuilder) {
        this.properties = properties;
        String base = properties.getLocalOllama().getBaseUrl();
        if (base != null && base.endsWith("/")) {
            base = base.substring(0, base.length() - 1);
        }
        this.restClient = restClientBuilder.baseUrl(base == null ? "http://localhost:11434" : base).build();
        this.fallback = new StubChatModelGateway(properties, "LOCAL_OLLAMA");
    }

    @Override
    public LlmCompletionResponse complete(LlmCompletionRequest request) {
        String modelId = properties.getLocalOllama().getReasoningModel();
        List<Map<String, String>> messages = new ArrayList<>();
        if (request.systemPrompt() != null && !request.systemPrompt().isBlank()) {
            messages.add(Map.of("role", "system", "content", request.systemPrompt()));
        }
        messages.add(Map.of("role", "user", "content", request.prompt() == null ? "" : request.prompt()));

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("model", modelId);
        body.put("messages", messages);
        body.put("stream", false);
        body.put("options", Map.of(
                "temperature", request.temperature() == null ? 0.2 : request.temperature()
        ));

        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> response = restClient.post()
                    .uri("/api/chat")
                    .body(body)
                    .retrieve()
                    .body(Map.class);
            String content = extractMessageContent(response);
            return new LlmCompletionResponse(content, "LOCAL_OLLAMA", modelId);
        } catch (RestClientException ex) {
            log.warn("Ollama completion failed ({}), falling back to stub: {}", modelId, ex.getMessage());
            return fallback.complete(request);
        }
    }

    @SuppressWarnings("unchecked")
    private static String extractMessageContent(Map<String, Object> response) {
        if (response == null) {
            return "";
        }
        Object message = response.get("message");
        if (message instanceof Map<?, ?> msg) {
            Object content = msg.get("content");
            if (content != null) {
                return content.toString();
            }
        }
        Object responseText = response.get("response");
        if (responseText != null) {
            return responseText.toString();
        }
        return response.toString();
    }
}
