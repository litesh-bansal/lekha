package com.lekha.langchain.config;

import com.lekha.langchain.enums.Model;
import dev.langchain4j.model.chat.ChatModel;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.Set;


@Component
public class LLMRegistry {

    private final Map<Model, ChatModel> models = new EnumMap<>(Model.class);

    public LLMRegistry(@Qualifier("gemmaChatModel") ChatModel gemma,
                       @Qualifier("nemotronChatModel") ChatModel nemotron) {
        models.put(Model.GEMMA, gemma);
        models.put(Model.NEMOTRON, nemotron);
    }

    /** Returns null when no chat model is registered for {@code model}. */
    public ChatModel getModel(Model model) {
        return models.get(model);
    }

    public Set<Model> registeredModels() {
        return Collections.unmodifiableSet(models.keySet());
    }

}
