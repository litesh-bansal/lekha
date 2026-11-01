package com.lekha.langchain.model.extraction;

import com.lekha.langchain.enums.Model;
import lombok.Builder;

/**
 * What /extract returns: the structured data the extraction model produced, the raw
 * OCR text it was derived from, and the OCR cost.
 */
@Builder
public record ExtractionResponse(

        DocumentInfo document,

        Model model,

        String text,

        long ocrLatencyMs,

        int inputToken,

        int outputToken,

        int totalToken) {
}
