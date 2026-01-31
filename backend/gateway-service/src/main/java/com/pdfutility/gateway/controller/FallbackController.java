package com.pdfutility.gateway.controller;

import com.pdfutility.common.dto.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

/**
 * Fallback Controller for circuit breaker.
 * Provides fallback responses when downstream services are unavailable.
 */
@Slf4j
@RestController
@RequestMapping("/fallback")
public class FallbackController {

    @GetMapping("/auth")
    public Mono<ResponseEntity<ApiResponse<Void>>> authFallback() {
        log.warn("Auth service fallback triggered");
        return Mono.just(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(ApiResponse.error("Authentication service is temporarily unavailable. Please try again later.", "AUTH_SERVICE_UNAVAILABLE")));
    }

    @GetMapping("/pdf")
    public Mono<ResponseEntity<ApiResponse<Void>>> pdfFallback() {
        log.warn("PDF service fallback triggered");
        return Mono.just(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(ApiResponse.error("PDF processing service is temporarily unavailable. Please try again later.", "PDF_SERVICE_UNAVAILABLE")));
    }

    @GetMapping("/storage")
    public Mono<ResponseEntity<ApiResponse<Void>>> storageFallback() {
        log.warn("Storage service fallback triggered");
        return Mono.just(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(ApiResponse.error("File storage service is temporarily unavailable. Please try again later.", "STORAGE_SERVICE_UNAVAILABLE")));
    }
}
