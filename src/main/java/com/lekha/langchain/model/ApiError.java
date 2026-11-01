package com.lekha.langchain.model;

public record ApiError(int status,
                       String error,
                       String detail,
                       String referenceId) {
}
