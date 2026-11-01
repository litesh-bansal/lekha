package com.lekha.langchain.config.chatModel;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@ConfigurationProperties(prefix = "langchain.lekha.ocr")
public class PaddleOCR extends LLMProperties{
}
