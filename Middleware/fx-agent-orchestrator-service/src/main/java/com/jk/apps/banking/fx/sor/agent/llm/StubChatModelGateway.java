package com.jk.apps.banking.fx.sor.agent.llm;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Deterministic stub for local/CI without AWS, HF, or Ollama.
 */
public class StubChatModelGateway implements ChatModelGateway {

    private static final Logger log = LoggerFactory.getLogger(StubChatModelGateway.class);

    private final LlmProviderProperties properties;
    private final String providerLabel;

    public StubChatModelGateway(LlmProviderProperties properties) {
        this(properties, null);
    }

    public StubChatModelGateway(LlmProviderProperties properties, String providerLabel) {
        this.properties = properties;
        this.providerLabel = providerLabel == null
                ? (properties.getProvider() == null ? "STUB" : properties.getProvider())
                : providerLabel;
    }

    @Override
    public LlmCompletionResponse complete(LlmCompletionRequest request) {
        ModelTier tier = request.modelTier() == null ? ModelTier.REASONING : request.modelTier();
        String modelId = resolveModelId(tier);
        String promptPreview = request.prompt() == null ? "" : request.prompt();
        if (promptPreview.length() > 120) {
            promptPreview = promptPreview.substring(0, 120) + "...";
        }
        String content = String.format(
                "{\"stub\":true,\"provider\":\"%s\",\"modelId\":\"%s\",\"tier\":\"%s\","
                        + "\"summary\":\"Deterministic stub completion for agent orchestration\","
                        + "\"promptPreview\":\"%s\"}",
                escape(providerLabel),
                escape(modelId),
                tier.name(),
                escape(promptPreview)
        );
        log.debug("StubChatModelGateway complete provider={} modelId={}", providerLabel, modelId);
        return new LlmCompletionResponse(content, providerLabel, modelId);
    }

    private String resolveModelId(ModelTier tier) {
        String provider = properties.getProvider() == null ? "LOCAL_OLLAMA" : properties.getProvider().toUpperCase();
        return switch (provider) {
            case "BEDROCK" -> switch (tier) {
                case FAST -> properties.getBedrock().getFastModel();
                case VOLUME, REASONING -> properties.getBedrock().getReasoningModel();
            };
            case "HUGGINGFACE" -> properties.getHuggingface().getReasoningModel();
            default -> properties.getLocalOllama().getReasoningModel();
        };
    }

    private static String escape(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
