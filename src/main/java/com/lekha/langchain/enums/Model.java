package com.lekha.langchain.enums;

/**
 * Models callers may request for the extraction step. The OCR model is not listed
 * here: it is a fixed part of the pipeline, wired by qualifier in OCRService, and
 * cannot be swapped per request.
 */
public enum Model {

    GEMMA,
    NEMOTRON

}
