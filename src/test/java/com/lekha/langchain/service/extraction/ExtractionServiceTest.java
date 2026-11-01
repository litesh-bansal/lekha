package com.lekha.langchain.service.extraction;

import com.lekha.langchain.config.LLMRegistry;
import com.lekha.langchain.enums.Model;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.service.output.OutputParsingException;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ExtractionServiceTest {

    /** Returns the schema-required fields; "{}" would now fail to parse, by design. */
    private static ChatModel stubModel() {
        return new ChatModel() {
            @Override
            public ChatResponse chat(ChatRequest chatRequest) {
                return ChatResponse.builder()
                        .aiMessage(AiMessage.from(
                                "{\"title\":\"A Title\",\"summary\":\"A summary.\",\"sectionHeadings\":[\"One\"]}"))
                        .build();
            }
        };
    }

    /**
     * Regression test for the NPE on extraction: @RequiredArgsConstructor used to emit a
     * second, no-arg constructor (the only final field has an initializer, so Lombok left
     * it out), and Spring picked that one over the LLMRegistry constructor, leaving the
     * extractors map empty.
     */
    @Test
    void exposesASingleConstructorSoSpringCannotPickANoArgOne() {
        Constructor<?>[] constructors = ExtractionService.class.getDeclaredConstructors();

        assertThat(constructors).hasSize(1);
        assertThat(constructors[0].getParameterTypes()).containsExactly(LLMRegistry.class);
    }

    @Test
    void registersAnExtractorForEveryModelInTheRegistry() {
        ExtractionService service = new ExtractionService(new LLMRegistry(stubModel(), stubModel()));

        // Reaching the model means an extractor was found; a null entry would have thrown.
        for (Model model : Model.values()) {
            assertThat(service.extract(model, "some ocr text")).isNotNull();
        }
    }

    @Test
    void rejectsAModelWithNoChatModelInsteadOfThrowingNpe() {
        ExtractionService service = new ExtractionService(new LLMRegistry(stubModel(), null));

        assertThatThrownBy(() -> service.extract(Model.NEMOTRON, "some ocr text"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("no extractor registered for model NEMOTRON");
    }

    /**
     * Guards the schema's required list: nemotron-mini:4b really does omit summary when
     * nothing is marked required, and silent nulls are worse than a loud failure.
     */
    @Test
    void failsWhenTheModelOmitsASchemaRequiredField() {
        ChatModel omitsSummary = new ChatModel() {
            @Override
            public ChatResponse chat(ChatRequest chatRequest) {
                return ChatResponse.builder()
                        .aiMessage(AiMessage.from("{\"title\":\"A Title\",\"sectionHeadings\":[]}"))
                        .build();
            }
        };
        ExtractionService service = new ExtractionService(new LLMRegistry(omitsSummary, omitsSummary));

        assertThatThrownBy(() -> service.extract(Model.GEMMA, "some ocr text"))
                .isInstanceOf(OutputParsingException.class);
    }

    @Test
    void rejectsBlankOcrText() {
        ExtractionService service = new ExtractionService(new LLMRegistry(stubModel(), stubModel()));

        assertThatThrownBy(() -> service.extract(Model.GEMMA, "  "))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> service.extract(Model.GEMMA, null))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
