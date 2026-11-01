package com.lekha.langchain.service.ocr;

import com.lekha.langchain.exception.ExtractionFailedException;
import com.lekha.langchain.model.ocr.OcrResult;
import dev.langchain4j.data.message.ImageContent;
import dev.langchain4j.data.message.TextContent;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.output.TokenUsage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Base64;
import java.util.Objects;
import java.util.Optional;

@Slf4j
@Service
public class OCRService {

    private final ChatModel ocrChatModel;

    public OCRService(@Qualifier("paddleOcrChatModel") ChatModel ocrModel) {
        this.ocrChatModel = ocrModel;
    }

    public OcrResult ocr(byte[] imageBytes, String mimeType) {

        if (Objects.isNull(imageBytes) || imageBytes.length == 0) {
            throw new IllegalArgumentException("image can't be null or empty");
        }
        if (Objects.isNull(mimeType) || mimeType.isBlank()) {
            throw new IllegalArgumentException("mimeType is required to send the image to the OCR model");
        }

        UserMessage userMessage = UserMessage.from(
                ImageContent.from(Base64.getEncoder().encodeToString(imageBytes), mimeType),
                TextContent.from("OCR:")
        );

        long start = System.nanoTime();
        ChatResponse response = ocrChatModel.chat(userMessage);
        long latency = Duration.ofNanos(System.nanoTime() - start).toMillis();

        String text = response.aiMessage().text();
        if (Objects.isNull(text) || text.isBlank()) {
            throw new ExtractionFailedException("OCR model returned no text");
        }
        log.info("OCR extraction completed in: {}ms", latency);

        // Ollama does not always report usage; absent counts become 0 rather than
        // propagating nulls into the response DTO's primitive fields.
        TokenUsage usage = response.tokenUsage();
        return new OcrResult(text, latency,
                count(usage, TokenUsage::inputTokenCount),
                count(usage, TokenUsage::outputTokenCount),
                count(usage, TokenUsage::totalTokenCount)
        );

    }

    private static int count(TokenUsage usage, java.util.function.Function<TokenUsage, Integer> field) {
        return Optional.ofNullable(usage).map(field).orElse(0);
    }
}
