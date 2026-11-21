package com.jk.apps.banking.fx.sor.agent.llm;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Hugging Face Inference API via RestClient POST {endpoint}/models/{model}.
 */
public class HuggingFaceChatModelGateway implements ChatModelGateway {

    private static final Logger log = LoggerFactory.getLogger(HuggingFaceChatModelGateway.class);

    private final LlmProviderProperties properties;
    private final RestClient restClient;
    private final StubChatModelGateway fallback;

    public HuggingFaceChatModelGateway(LlmProviderProperties properties, RestClient.Builder restClientBuilder) {
        this.properties = properties;
        String base = properties.getHuggingface().getEndpointUrl();
        if (base != null && base.endsWith("/")) {
            base = base.substring(0, base.length() - 1);
        }
        this.restClient = restClientBuilder.baseUrl(base == null ? "https://api-inference.huggingface.co" : base).build();
        this.fallback = new StubChatModelGateway(properties, "HUGGINGFACE");
    }

    @Override
    public LlmCompletionResponse complete(LlmCompletionRequest request) {
        String modelId = properties.getHuggingface().getReasoningModel();
        String prompt = buildPrompt(request);
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("inputs", prompt);
        Map<String, Object> parameters = new LinkedHashMap<>();
        parameters.put("temperature", request.temperature() == null ? 0.2 : request.temperature());
        parameters.put("return_full_text", false);
        body.put("parameters", parameters);

        try {
            @SuppressWarnings("unchecked")
            Object response = restClient.post()
                    .uri("/models/{model}", modelId)
                    .body(body)
                    .retrieve()
                    .body(Object.class);
            String content = extractContent(response);
            return new LlmCompletionResponse(content, "HUGGINGFACE", modelId);
        } catch (RestClientException ex) {
            log.warn("HuggingFace completion failed ({}), falling back to stub: {}", modelId, ex.getMessage());
            return fallback.complete(request);
        }
    }

    private static String buildPrompt(LlmCompletionRequest request) {
        StringBuilder sb = new StringBuilder();
        if (request.systemPrompt() != null && !request.systemPrompt().isBlank()) {
            sb.append(request.systemPrompt()).append("\n\n");
        }
        if (request.prompt() != null) {
            sb.append(request.prompt());
        }
        return sb.toString();
    }

    @SuppressWarnings("unchecked")
    private static String extractContent(Object response) {
        if (response == null) {
            return "";
        }
        if (response instanceof String s) {
            return s;
        }
        if (response instanceof List<?> list && !list.isEmpty()) {
            Object first = list.get(0);
            if (first instanceof Map<?, ?> map) {
                Object generated = map.get("generated_text");
                if (generated != null) {
                    return generated.toString();
                }
            }
            return first.toString();
        }
        if (response instanceof Map<?, ?> map) {
            Object generated = map.get("generated_text");
            if (generated != null) {
                return generated.toString();
            }
            return map.toString();
        }
        return response.toString();
    }
}
