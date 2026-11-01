package com.lekha.langchain;

import com.lekha.langchain.config.LLMRegistry;
import com.lekha.langchain.enums.Model;
import com.lekha.langchain.service.extraction.ExtractionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Loads the real context. Building an Ollama ChatModel does not open a connection, so
 * this runs without a local Ollama; it proves the qualifiers resolve and that every
 * @ConfigurationProperties binding satisfies its constraints.
 */
@SpringBootTest
class LekhaApplicationTest {

    @Autowired
    private LLMRegistry registry;

    @Autowired
    private ExtractionService extractionService;

    @Test
    void registersAChatModelForEveryRequestableModel() {
        assertThat(registry.registeredModels()).containsExactlyInAnyOrder(Model.values());
        for (Model model : Model.values()) {
            assertThat(registry.getModel(model)).isNotNull();
        }
    }

    @Test
    void wiresTheExtractionServiceThroughItsRegistryConstructor() {
        // An empty extractors map is the failure mode that produced the original NPE, and
        // it only shows up once Spring has actually instantiated the bean.
        assertThat(extractionService).isNotNull();
        assertThat(extractionService.extractableModels()).containsExactlyInAnyOrder(Model.values());
    }
}
