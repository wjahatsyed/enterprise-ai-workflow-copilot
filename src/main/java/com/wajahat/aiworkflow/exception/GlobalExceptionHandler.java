package com.wajahat.aiworkflow.exception;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(
            IllegalArgumentException ex,
            HttpServletRequest request
    ) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        String message = ex.getMessage();
        if (message != null && (message.contains("not found") || message.contains("Not found"))) {
            status = HttpStatus.NOT_FOUND;
        }

        if (status.is4xxClientError()) {
            log.warn("IllegalArgumentException: {} at path {}", message, request.getRequestURI());
        } else {
            log.error("IllegalArgumentException: {} at path {}", message, request.getRequestURI());
        }

        ErrorResponse error = ErrorResponse.builder()
                .message(message)
                .status(status.value())
                .timestamp(LocalDateTime.now())
                .path(request.getRequestURI())
                .build();

        return new ResponseEntity<>(error, status);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentTypeMismatch(
            MethodArgumentTypeMismatchException ex,
            HttpServletRequest request
    ) {
        String message = String.format("Parameter '%s' has invalid value '%s'. Expected type is '%s'",
                ex.getName(), ex.getValue(), ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "unknown");

        log.warn("MethodArgumentTypeMismatchException: {} at path {}", message, request.getRequestURI());

        ErrorResponse error = ErrorResponse.builder()
                .message(message)
                .status(HttpStatus.BAD_REQUEST.value())
                .timestamp(LocalDateTime.now())
                .path(request.getRequestURI())
                .details(Map.of("parameter", ex.getName(), "value", String.valueOf(ex.getValue())))
                .build();

        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }
}
