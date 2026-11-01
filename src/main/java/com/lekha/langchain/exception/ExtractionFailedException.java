package com.lekha.langchain.exception;

/** A model was reachable but produced nothing usable. */
public class ExtractionFailedException extends RuntimeException {

    public ExtractionFailedException(String message) {
        super(message);
    }

    public ExtractionFailedException(String message, Throwable cause) {
        super(message, cause);
    }
}
