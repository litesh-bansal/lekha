package com.lekha.langchain.controller;

import com.lekha.langchain.enums.Model;
import com.lekha.langchain.exception.ExtractionFailedException;
import com.lekha.langchain.model.extraction.DocumentInfo;
import com.lekha.langchain.model.extraction.ExtractionResponse;
import com.lekha.langchain.service.DocumentExtractionPipeline;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AiModelController.class)
class AiModelControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private DocumentExtractionPipeline pipeline;

    private static MockMultipartFile file() {
        return new MockMultipartFile("documentContent", "doc.png", "image/png", new byte[]{1, 2, 3});
    }

    @Test
    void returnsTheExtractedDocument() throws Exception {
        DocumentInfo info = new DocumentInfo("Birth Certificate", "A summary.", List.of("Details"));
        when(pipeline.process(any(), any(), any()))
                .thenReturn(ExtractionResponse.builder()
                        .document(info)
                        .model(Model.GEMMA)
                        .text("raw ocr")
                        .ocrLatencyMs(42)
                        .inputToken(11)
                        .outputToken(7)
                        .totalToken(18)
                        .build());

        mockMvc.perform(multipart("/api/v1/extract").file(file()).param("referenceId", "ref-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.document.title").value("Birth Certificate"))
                .andExpect(jsonPath("$.document.sectionHeadings[0]").value("Details"))
                .andExpect(jsonPath("$.model").value("GEMMA"))
                .andExpect(jsonPath("$.totalToken").value(18));
    }

    @Test
    void rejectsAMissingDocumentWithABadRequestRatherThanA500() throws Exception {
        mockMvc.perform(multipart("/api/v1/extract").param("referenceId", "ref-1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.detail").value(org.hamcrest.Matchers.containsString("documentContent")));
    }

    @Test
    void rejectsAMissingReferenceId() throws Exception {
        mockMvc.perform(multipart("/api/v1/extract").file(file()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value(org.hamcrest.Matchers.containsString("referenceId")));
    }

    @Test
    void reportsAFailedExtractionAsUnprocessableAndKeepsTheReferenceId() throws Exception {
        when(pipeline.process(any(), any(), any()))
                .thenThrow(new ExtractionFailedException("OCR model returned no text"));

        mockMvc.perform(multipart("/api/v1/extract").file(file()).param("referenceId", "ref-9"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.detail").value("OCR model returned no text"))
                .andExpect(jsonPath("$.referenceId").value("ref-9"));
    }
}
