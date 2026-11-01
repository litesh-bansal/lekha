package com.lekha.langchain.config;

import com.lekha.langchain.config.chatModel.Gemma;
import com.lekha.langchain.config.chatModel.LLMProperties;
import com.lekha.langchain.config.chatModel.Nemotron;
import com.lekha.langchain.config.chatModel.PaddleOCR;
import dev.langchain4j.model.chat.Capability;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.listener.ChatModelListener;
import dev.langchain4j.model.ollama.OllamaChatModel;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.Set;

@Configuration
public class ChatModelConfig {

    @Bean
    public ChatModel gemmaChatModel(Gemma gemma, ObjectProvider<ChatModelListener> listeners) {
        return buildChatModel(gemma, listeners);
    }

    @Bean
    public ChatModel nemotronChatModel(Nemotron nemotron, ObjectProvider<ChatModelListener> listeners) {
        return buildChatModel(nemotron, listeners);
    }

    /** OCR returns prose, not a schema, so leave structuredOutput off for this one. */
    @Bean
    public ChatModel paddleOcrChatModel(PaddleOCR paddleOCR, ObjectProvider<ChatModelListener> listeners) {
        return buildChatModel(paddleOCR, listeners);
    }

    private static ChatModel buildChatModel(LLMProperties llmProperties,
                                            ObjectProvider<ChatModelListener> chatModelListeners) {

        // AiServices only derives a JSON schema for the return type when the model
        // advertises RESPONSE_FORMAT_JSON_SCHEMA, and Ollama defaults the set to empty.
        // Declaring it on a model whose runner cannot do constrained decoding makes every
        // call fail, so it is opt-in per model.
        Set<Capability> capabilities = llmProperties.isStructuredOutput()
                ? Set.of(Capability.RESPONSE_FORMAT_JSON_SCHEMA)
                : Set.of();

        return OllamaChatModel.builder()
                .baseUrl(llmProperties.getBaseUrl())
                .modelName(llmProperties.getModelName())
                .topP(llmProperties.getTopP())
                .temperature(llmProperties.getTemperature())
                .numPredict(llmProperties.getMaxTokens())
                .maxRetries(llmProperties.getMaxRetries())
                .timeout(Duration.ofSeconds(llmProperties.getRequestTimeout()))
                .logRequests(llmProperties.isLogRequests())
                .logResponses(llmProperties.isLogResponses())
                .supportedCapabilities(capabilities)
                .listeners(chatModelListeners.stream().toList())
                .build();
    }
}
