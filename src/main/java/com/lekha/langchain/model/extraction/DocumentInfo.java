package com.lekha.langchain.model.extraction;

import com.fasterxml.jackson.annotation.JsonProperty;
import dev.langchain4j.model.output.structured.Description;

import java.util.List;

/**
 * What the LLM must return. LangChain4j turns this record into a JSON schema,
 * the model fills it, and LangChain4j parses the JSON back into this record.
 *
 * Learning phase: generic fields. Later, replace with BirthCertificateData etc.
 *
 * <p>{@code @JsonProperty(required = true)} is what puts a field in the schema's
 * {@code required} array: AiServices defaults every field to optional, and a model given
 * an empty {@code required} list will happily omit fields (nemotron-mini:4b drops
 * {@code summary}). Only {@code title} is left optional, because a document genuinely
 * may not have one.
 */
public record DocumentInfo(

        @Description("Main title of the document, or null if there is none")
        String title,

        @JsonProperty(required = true)
        @Description("Two or three sentences describing what the document says")
        String summary,

        @JsonProperty(required = true)
        @Description("Section headings in the order they appear; empty list if none")
        List<String> sectionHeadings) {
}
