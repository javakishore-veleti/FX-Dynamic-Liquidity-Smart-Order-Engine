package com.jk.apps.banking.fx.sor.agent.llm;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class ChatModelGatewayConfig {

    private static final Logger log = LoggerFactory.getLogger(ChatModelGatewayConfig.class);

    @Bean
    ChatModelGateway chatModelGateway(LlmProviderProperties properties, RestClient.Builder restClientBuilder) {
        String provider = properties.getProvider() == null ? "LOCAL_OLLAMA" : properties.getProvider().trim().toUpperCase();
        log.info("Configuring ChatModelGateway for provider={}", provider);
        return switch (provider) {
            case "OPENAI_COMPAT", "LITELLM" -> new OpenAiCompatibleChatModelGateway(properties, restClientBuilder);
            case "BEDROCK" -> new BedrockChatModelGateway(properties);
            case "HUGGINGFACE" -> new HuggingFaceChatModelGateway(properties, restClientBuilder);
            case "LOCAL_OLLAMA" -> new OllamaChatModelGateway(properties, restClientBuilder);
            default -> {
                log.warn("Unknown fx.llm.provider='{}', using StubChatModelGateway", provider);
                yield new StubChatModelGateway(properties, "STUB");
            }
        };
    }
}
