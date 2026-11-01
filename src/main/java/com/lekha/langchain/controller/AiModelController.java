package com.lekha.langchain.controller;

import com.lekha.langchain.model.extraction.ExtractionRequest;
import com.lekha.langchain.model.extraction.ExtractionResponse;
import com.lekha.langchain.service.DocumentExtractionPipeline;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.MDC;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class AiModelController {

    private final DocumentExtractionPipeline documentExtractionPipeline;

    @PostMapping(value = "/extract", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ExtractionResponse> extract(@Valid @ModelAttribute ExtractionRequest request)
            throws IOException {
        // Cleared by MdcCleanupFilter after the response is written, so that
        // ApiExceptionHandler can still read the id when this throws.
        MDC.put("referenceId", request.getReferenceId());
        return ResponseEntity.ok(documentExtractionPipeline.process(
                request.getDocumentContent().getBytes(),
                request.resolveMimeType(),
                request.getModels()));
    }
}
