package com.lekha.langchain.controller;

import com.lekha.langchain.exception.ExtractionFailedException;
import com.lekha.langchain.model.ApiError;
import dev.langchain4j.service.output.OutputParsingException;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.util.stream.Collectors;

/**
 * Without this, every failure below surfaced as a whitelabel 500 and callers could not
 * tell a bad request from a model outage.
 */
@Slf4j
@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(BindException.class)
    public ResponseEntity<ApiError> onInvalidRequest(BindException e) {
        String detail = e.getBindingResult().getFieldErrors().stream()
                .map(fieldError -> fieldError.getField() + " " + describe(fieldError))
                .collect(Collectors.joining("; "));
        return build(HttpStatus.BAD_REQUEST, detail.isBlank() ? "invalid request" : detail);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiError> onIllegalArgument(IllegalArgumentException e) {
        return build(HttpStatus.BAD_REQUEST, e.getMessage());
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ApiError> onTooLarge(MaxUploadSizeExceededException e) {
        return build(HttpStatus.PAYLOAD_TOO_LARGE, "document exceeds the maximum upload size");
    }

    @ExceptionHandler(ExtractionFailedException.class)
    public ResponseEntity<ApiError> onExtractionFailed(ExtractionFailedException e) {
        log.warn("Extraction failed", e);
        return build(HttpStatus.UNPROCESSABLE_ENTITY, e.getMessage());
    }

    /**
     * The model answered, but not with something that fits DocumentInfo (e.g. it omitted a
     * schema-required field). That is a model-quality problem, not a server fault.
     */
    @ExceptionHandler(OutputParsingException.class)
    public ResponseEntity<ApiError> onUnparseableOutput(OutputParsingException e) {
        log.warn("Model returned output that does not match the DocumentInfo schema", e);
        return build(HttpStatus.UNPROCESSABLE_ENTITY, "model did not return the expected document structure");
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> onUnexpected(Exception e) {
        log.error("Unexpected failure handling request", e);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "unexpected error");
    }

    private static String describe(FieldError fieldError) {
        return fieldError.getDefaultMessage() == null ? "is invalid" : fieldError.getDefaultMessage();
    }

    private static ResponseEntity<ApiError> build(HttpStatus status, String detail) {
        return ResponseEntity.status(status)
                .body(new ApiError(status.value(), status.getReasonPhrase(), detail, MDC.get("referenceId")));
    }
}
