package com.lekha.langchain.service.extraction;

import com.lekha.langchain.config.LLMRegistry;
import com.lekha.langchain.enums.Model;
import com.lekha.langchain.model.extraction.DocumentInfo;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.service.AiServices;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

@Slf4j
@Service
public class ExtractionService {

    private final Map<Model, DocumentExtractor> extractors = new EnumMap<>(Model.class);

    public ExtractionService(LLMRegistry registry) {
        for (Model model : Model.values()) {
            ChatModel chatModel = registry.getModel(model);
            if (Objects.isNull(chatModel)) {
                log.warn("No chat model registered for {}; extraction with it will be rejected", model);
                continue;
            }
            extractors.put(model, AiServices.create(DocumentExtractor.class, chatModel));
        }
        log.info("Registered document extractors for: {}", extractors.keySet());
    }

    /** Models this instance can actually extract with. */
    public Set<Model> extractableModels() {
        return Collections.unmodifiableSet(extractors.keySet());
    }

    public DocumentInfo extract(Model model, String ocrText) {
        if (Objects.isNull(ocrText) || ocrText.isBlank()) {
            throw new IllegalArgumentException("ocrText must not be blank");
        }

        DocumentExtractor extractor = extractors.get(model);
        if (Objects.isNull(extractor)) {
            throw new IllegalArgumentException(
                    "no extractor registered for model " + model + "; available: " + extractors.keySet());
        }

        return extractor.extract(ocrText);
    }
}
