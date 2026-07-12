package com.hirepilot.hirepilotai.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResumeNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Map<String, String> handleResumeNotFound(ResumeNotFoundException ex) {
        return Map.of("message", ex.getMessage());
    }

    @ExceptionHandler(ActiveResumeDeletionException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public Map<String, String> handleActiveResumeDeletion(ActiveResumeDeletionException ex) {
        return Map.of("message", ex.getMessage());
    }

    @ExceptionHandler(RuntimeException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Map<String, String> handleRuntimeException(RuntimeException ex) {
        return Map.of("message", ex.getMessage());
    }
}