package com.pdfutility.pdfcore.config;

import com.pdfutility.common.dto.ApiResponse;
import com.pdfutility.common.exception.PdfProcessingException;
import com.pdfutility.common.exception.PdfUtilityException;
import com.pdfutility.common.exception.ResourceNotFoundException;
import com.pdfutility.common.exception.StorageException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;
import reactor.core.publisher.Mono;

import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

/**
 * Global exception handler for PDF Core Service.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public Mono<ResponseEntity<ApiResponse<Void>>> handleResourceNotFound(ResourceNotFoundException ex) {
        log.warn("Resource not found: {}", ex.getMessage());
        return Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(ex.getMessage(), ex.getErrorCode())));
    }

    @ExceptionHandler(PdfProcessingException.class)
    public Mono<ResponseEntity<ApiResponse<Void>>> handlePdfProcessing(PdfProcessingException ex) {
        log.error("PDF processing error: {}", ex.getMessage(), ex);
        return Mono.just(ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(ApiResponse.error(ex.getMessage(), ex.getErrorCode())));
    }

    @ExceptionHandler(StorageException.class)
    public Mono<ResponseEntity<ApiResponse<Void>>> handleStorage(StorageException ex) {
        log.error("Storage error: {}", ex.getMessage(), ex);
        return Mono.just(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(ApiResponse.error("Storage service unavailable", ex.getErrorCode())));
    }

    @ExceptionHandler(PdfUtilityException.class)
    public Mono<ResponseEntity<ApiResponse<Void>>> handlePdfUtility(PdfUtilityException ex) {
        log.error("PDF utility error: {}", ex.getMessage(), ex);
        return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(ex.getMessage(), ex.getErrorCode())));
    }

    @ExceptionHandler(WebExchangeBindException.class)
    public Mono<ResponseEntity<ApiResponse<Void>>> handleValidation(WebExchangeBindException ex) {
        String errors = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));
        log.warn("Validation error: {}", errors);
        return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error("Validation failed: " + errors, "VALIDATION_ERROR")));
    }

    @ExceptionHandler(TimeoutException.class)
    public Mono<ResponseEntity<ApiResponse<Void>>> handleTimeout(TimeoutException ex) {
        log.error("Request timeout: {}", ex.getMessage());
        return Mono.just(ResponseEntity.status(HttpStatus.GATEWAY_TIMEOUT)
                .body(ApiResponse.error("Request timed out", "TIMEOUT_ERROR")));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public Mono<ResponseEntity<ApiResponse<Void>>> handleIllegalArgument(IllegalArgumentException ex) {
        log.warn("Invalid argument: {}", ex.getMessage());
        return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(ex.getMessage(), "INVALID_ARGUMENT")));
    }

    @ExceptionHandler(Exception.class)
    public Mono<ResponseEntity<ApiResponse<Void>>> handleGeneral(Exception ex) {
        log.error("Unexpected error: {}", ex.getMessage(), ex);
        return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("An unexpected error occurred", "INTERNAL_ERROR")));
    }
}
