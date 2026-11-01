package com.lekha.langchain.model.ocr;

/** Token counts are normalised to 0 when the model does not report usage. */
public record OcrResult(String text,
                        long latencyMs,
                        int inputTokens,
                        int outputTokens,
                        int totalTokens) {
}
