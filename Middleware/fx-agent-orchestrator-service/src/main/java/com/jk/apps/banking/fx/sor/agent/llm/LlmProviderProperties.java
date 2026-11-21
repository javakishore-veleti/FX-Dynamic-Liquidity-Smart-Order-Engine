package com.jk.apps.banking.fx.sor.agent.llm;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "fx.llm")
public class LlmProviderProperties {

    /**
     * Active provider: OPENAI_COMPAT (LiteLLM) | BEDROCK | HUGGINGFACE | LOCAL_OLLAMA
     */
    private String provider = "OPENAI_COMPAT";

    private Bedrock bedrock = new Bedrock();
    private HuggingFace huggingface = new HuggingFace();
    private LocalOllama localOllama = new LocalOllama();
    private OpenaiCompatible openaiCompatible = new OpenaiCompatible();

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public Bedrock getBedrock() {
        return bedrock;
    }

    public void setBedrock(Bedrock bedrock) {
        this.bedrock = bedrock;
    }

    public HuggingFace getHuggingface() {
        return huggingface;
    }

    public void setHuggingface(HuggingFace huggingface) {
        this.huggingface = huggingface;
    }

    public LocalOllama getLocalOllama() {
        return localOllama;
    }

    public void setLocalOllama(LocalOllama localOllama) {
        this.localOllama = localOllama;
    }

    public OpenaiCompatible getOpenaiCompatible() {
        return openaiCompatible;
    }

    public void setOpenaiCompatible(OpenaiCompatible openaiCompatible) {
        this.openaiCompatible = openaiCompatible;
    }

    public static class OpenaiCompatible {
        private String baseUrl = "http://localhost:4000";
        private String apiKey = "sk-local-fx-sor";
        private String reasoningModel = "fx-reasoning-tier";
        private String fastModel = "fx-fast-triage-tier";
        private String graphModel = "fx-graph-tier";
        private String embeddingModel = "fx-embedding-tier";

        public String getBaseUrl() {
            return baseUrl;
        }

        public void setBaseUrl(String baseUrl) {
            this.baseUrl = baseUrl;
        }

        public String getApiKey() {
            return apiKey;
        }

        public void setApiKey(String apiKey) {
            this.apiKey = apiKey;
        }

        public String getReasoningModel() {
            return reasoningModel;
        }

        public void setReasoningModel(String reasoningModel) {
            this.reasoningModel = reasoningModel;
        }

        public String getFastModel() {
            return fastModel;
        }

        public void setFastModel(String fastModel) {
            this.fastModel = fastModel;
        }

        public String getGraphModel() {
            return graphModel;
        }

        public void setGraphModel(String graphModel) {
            this.graphModel = graphModel;
        }

        public String getEmbeddingModel() {
            return embeddingModel;
        }

        public void setEmbeddingModel(String embeddingModel) {
            this.embeddingModel = embeddingModel;
        }
    }

    public static class Bedrock {
        private String region = "us-east-1";
        private String reasoningModel = "us.anthropic.claude-3-7-sonnet-20250219-v1:0";
        private String fastModel = "us.amazon.nova-lite-v1:0";
        private String embeddingModel = "amazon.titan-embed-text-v2:0";
        private String guardrailId = "";

        public String getRegion() {
            return region;
        }

        public void setRegion(String region) {
            this.region = region;
        }

        public String getReasoningModel() {
            return reasoningModel;
        }

        public void setReasoningModel(String reasoningModel) {
            this.reasoningModel = reasoningModel;
        }

        public String getFastModel() {
            return fastModel;
        }

        public void setFastModel(String fastModel) {
            this.fastModel = fastModel;
        }

        public String getEmbeddingModel() {
            return embeddingModel;
        }

        public void setEmbeddingModel(String embeddingModel) {
            this.embeddingModel = embeddingModel;
        }

        public String getGuardrailId() {
            return guardrailId;
        }

        public void setGuardrailId(String guardrailId) {
            this.guardrailId = guardrailId;
        }
    }

    public static class HuggingFace {
        private String endpointUrl = "https://api-inference.huggingface.co";
        private String reasoningModel = "Qwen/Qwen2.5-72B-Instruct";
        private String embeddingModel = "BAAI/bge-m3";

        public String getEndpointUrl() {
            return endpointUrl;
        }

        public void setEndpointUrl(String endpointUrl) {
            this.endpointUrl = endpointUrl;
        }

        public String getReasoningModel() {
            return reasoningModel;
        }

        public void setReasoningModel(String reasoningModel) {
            this.reasoningModel = reasoningModel;
        }

        public String getEmbeddingModel() {
            return embeddingModel;
        }

        public void setEmbeddingModel(String embeddingModel) {
            this.embeddingModel = embeddingModel;
        }
    }

    public static class LocalOllama {
        private String baseUrl = "http://localhost:11434";
        private String reasoningModel = "qwen2.5:14b";

        public String getBaseUrl() {
            return baseUrl;
        }

        public void setBaseUrl(String baseUrl) {
            this.baseUrl = baseUrl;
        }

        public String getReasoningModel() {
            return reasoningModel;
        }

        public void setReasoningModel(String reasoningModel) {
            this.reasoningModel = reasoningModel;
        }
    }
}
