package com.lekha.langchain.service.ocr;

import com.lekha.langchain.exception.ExtractionFailedException;
import com.lekha.langchain.model.ocr.OcrResult;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.output.TokenUsage;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OCRServiceTest {

    private static final byte[] IMAGE = {1, 2, 3};

    private static OCRService serviceReturning(ChatResponse response) {
        return new OCRService(new ChatModel() {
            @Override
            public ChatResponse chat(ChatRequest chatRequest) {
                return response;
            }
        });
    }

    private static ChatResponse responseWith(String text, TokenUsage usage) {
        return ChatResponse.builder().aiMessage(AiMessage.from(text)).tokenUsage(usage).build();
    }

    @Test
    void reportsTheTotalTokenCountRatherThanTheOutputCount() {
        OcrResult result = serviceReturning(responseWith("hello", new TokenUsage(11, 7)))
                .ocr(IMAGE, "image/png");

        assertThat(result.inputTokens()).isEqualTo(11);
        assertThat(result.outputTokens()).isEqualTo(7);
        assertThat(result.totalTokens()).isEqualTo(18);
    }

    @Test
    void defaultsTokenCountsToZeroWhenTheModelReportsNoUsage() {
        OcrResult result = serviceReturning(responseWith("hello", null)).ocr(IMAGE, "image/png");

        assertThat(result.inputTokens()).isZero();
        assertThat(result.outputTokens()).isZero();
        assertThat(result.totalTokens()).isZero();
    }

    @Test
    void rejectsEmptyImage() {
        OCRService service = serviceReturning(responseWith("hello", null));

        assertThatThrownBy(() -> service.ocr(new byte[0], "image/png"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> service.ocr(null, "image/png"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejectsMissingMimeType() {
        OCRService service = serviceReturning(responseWith("hello", null));

        assertThatThrownBy(() -> service.ocr(IMAGE, " "))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void failsWhenTheModelReturnsNoText() {
        OCRService service = serviceReturning(responseWith("   ", null));

        assertThatThrownBy(() -> service.ocr(IMAGE, "image/png"))
                .isInstanceOf(ExtractionFailedException.class);
    }
}
