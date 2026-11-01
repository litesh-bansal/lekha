package com.lekha.langchain.config.chatModel;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;
import org.springframework.validation.annotation.Validated;

/**
 * Shared Ollama tuning knobs. Constrained so that a missing or mistyped property
 * fails at startup rather than silently sending topP=0 / maxTokens=0 to the model.
 */
@Getter
@Setter
@Validated
public abstract class LLMProperties {

    @NotBlank
    private String baseUrl;

    @NotBlank
    private String modelName;

    @DecimalMin("0.0")
    @DecimalMax("1.0")
    private double topP = 1.0;

    @DecimalMin("0.0")
    @DecimalMax("2.0")
    private double temperature = 0.0;

    @Positive
    private int maxTokens = 1024;

    @Min(0)
    private int maxRetries = 1;

    /** Per-request timeout, in seconds. */
    @Positive
    private int requestTimeout = 120;

    /**
     * Whether this model can honour an Ollama {@code format} JSON schema. Support is per
     * model, not per Ollama version: gemma4:e4b answers
     * {@code {"error":"structured output is unavailable"}}, while nemotron-mini:4b accepts it.
     * Off by default; when off, AiServices falls back to asking for JSON in the prompt.
     */
    private boolean structuredOutput = false;

    /** Logs the outgoing prompt at DEBUG on the langchain4j logger. */
    private boolean logRequests = false;

    /** Logs the raw model response at DEBUG on the langchain4j logger. */
    private boolean logResponses = false;
}
