package com.lekha.langchain.service.extraction;

import com.lekha.langchain.model.extraction.DocumentInfo;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

public interface DocumentExtractor {

    @SystemMessage("""
            You extract information from the OCR text of a document.
            The text may contain OCR errors.
            Use only information present in the text. Use null for anything not present.""")
    @UserMessage("OCR text:\n{{text}}")
    DocumentInfo extract(@V("text") String text);
}
