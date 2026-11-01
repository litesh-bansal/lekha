package com.lekha.langchain.service.extraction;

import com.lekha.langchain.enums.Model;
import com.lekha.langchain.model.extraction.DocumentInfo;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Hits a real local Ollama, so it is excluded from the default build (see the
 * surefire excludedGroups in pom.xml). Run it with:
 * {@code mvn test -Dgroups=ollama}
 * <p>
 * This is the regression guard for "structured output is unavailable": whether a model
 * can honour an Ollama {@code format} schema is a property of that model, and the only
 * way to know the structuredOutput flags are set correctly is to call the models.
 */
@Tag("ollama")
@SpringBootTest
class ExtractionServiceOllamaLiveTest {

    private static final String OCR_TEXT = """
            CERTIFICATE OF COMPLETION

            Holder
            Name: Jane Doe
            Course: Introduction to Bookbinding

            Issued
            Date: 4 March 2024
            Issuer: Riverside Community College
            """;

    @Autowired
    private ExtractionService extractionService;

    @ParameterizedTest
    @EnumSource(Model.class)
    void everyRegisteredModelCanExtractWithItsConfiguredStructuredOutputSetting(Model model) {
        DocumentInfo info = extractionService.extract(model, OCR_TEXT);

        assertThat(info).isNotNull();
        // title is deliberately optional; summary and sectionHeadings are schema-required.
        assertThat(info.summary()).isNotBlank();
        assertThat(info.sectionHeadings()).isNotNull();
    }

    @Test
    void extractsTheExpectedContentFromACertificate() {
        DocumentInfo info = extractionService.extract(Model.GEMMA, OCR_TEXT);

        assertThat(info.title()).containsIgnoringCase("certificate");
        assertThat(info.summary()).isNotBlank();
    }
}
