package com.lekha.langchain.service;

import com.lekha.langchain.enums.Model;
import com.lekha.langchain.model.extraction.DocumentInfo;
import com.lekha.langchain.model.extraction.ExtractionResponse;
import com.lekha.langchain.model.ocr.OcrResult;
import com.lekha.langchain.service.extraction.ExtractionService;
import com.lekha.langchain.service.ocr.OCRService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentExtractionPipeline {

    private final OCRService ocrService;
    private final ExtractionService extractionService;

    public ExtractionResponse process(byte[] image, String mimeType, List<Model> models) {
        // Only the first requested model runs today; the request accepts a list so that
        // comparing several in one call can be added without changing the API shape.
        if (Objects.isNull(models) || models.isEmpty()) {
            throw new IllegalArgumentException("at least one model must be requested");
        }
        Model model = models.getFirst();

        OcrResult ocrResult = ocrService.ocr(image, mimeType);

        DocumentInfo documentInfo = extractionService.extract(model, ocrResult.text());
        log.info("Extracted data using {}: {}", model, documentInfo);

        return ExtractionResponse.builder()
                .document(documentInfo)
                .model(model)
                .text(ocrResult.text())
                .ocrLatencyMs(ocrResult.latencyMs())
                .inputToken(ocrResult.inputTokens())
                .outputToken(ocrResult.outputTokens())
                .totalToken(ocrResult.totalTokens())
                .build();

    }

}
