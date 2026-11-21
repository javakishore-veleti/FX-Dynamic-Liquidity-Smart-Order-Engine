package com.jk.apps.banking.fx.sor.agent.llm;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Bedrock gateway stub — AWS Bedrock Runtime SDK is not on the classpath.
 * TODO: install software.amazon.awssdk:bedrockruntime and wire Converse API.
 */
public class BedrockChatModelGateway implements ChatModelGateway {

    private static final Logger log = LoggerFactory.getLogger(BedrockChatModelGateway.class);

    private final LlmProviderProperties properties;
    private final StubChatModelGateway stub;

    public BedrockChatModelGateway(LlmProviderProperties properties) {
        this.properties = properties;
        this.stub = new StubChatModelGateway(properties, "BEDROCK");
    }

    @Override
    public LlmCompletionResponse complete(LlmCompletionRequest request) {
        log.warn(
                "BEDROCK selected — install software.amazon.awssdk:bedrockruntime to enable "
                        + "(region={}, guardrailId={})",
                properties.getBedrock().getRegion(),
                properties.getBedrock().getGuardrailId()
        );
        return stub.complete(request);
    }
}
