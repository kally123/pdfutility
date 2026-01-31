package com.pdfutility.pdfcore.controller;

import com.pdfutility.common.dto.ApiResponse;
import com.pdfutility.pdfcore.dto.PdfOperationRequests.CompressRequest;
import com.pdfutility.pdfcore.dto.PdfOperationResponses.CompressionResultResponse;
import com.pdfutility.pdfcore.dto.PdfOperationResponses.JobCreatedResponse;
import com.pdfutility.pdfcore.model.CompressionLevel;
import com.pdfutility.pdfcore.model.JobStatus;
import com.pdfutility.pdfcore.model.JobType;
import com.pdfutility.pdfcore.repository.PdfJobRepository;
import com.pdfutility.pdfcore.service.PdfCompressService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * PDF Compress Controller - Reactive REST API.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/pdf/compress")
@RequiredArgsConstructor
@Tag(name = "PDF Compress", description = "PDF compression operations")
public class PdfCompressController {

    private final PdfCompressService compressService;
    private final PdfJobRepository jobRepository;

    /**
     * Compress a PDF asynchronously.
     */
    @PostMapping
    @Operation(summary = "Compress PDF", description = "Compress a PDF file to reduce size")
    public Mono<ResponseEntity<ApiResponse<JobCreatedResponse>>> compressPdf(
            @Valid @RequestBody CompressRequest request,
            @RequestHeader("X-User-Id") String userId) {

        CompressionLevel level = request.getCompressionLevel() != null 
                ? request.getCompressionLevel() 
                : CompressionLevel.MEDIUM;
        boolean removeMetadata = request.getRemoveMetadata() != null && request.getRemoveMetadata();
        boolean optimizeImages = request.getOptimizeImages() != null && request.getOptimizeImages();

        return compressService.compressPdfAsync(request.getFileId(), level, removeMetadata, optimizeImages, userId)
                .map(jobId -> JobCreatedResponse.builder()
                        .jobId(jobId)
                        .jobType(JobType.COMPRESS)
                        .status(JobStatus.PENDING)
                        .message("Compression job created successfully")
                        .createdAt(LocalDateTime.now())
                        .trackingUrl("/api/v1/pdf/jobs/" + jobId)
                        .build())
                .map(response -> ResponseEntity.status(HttpStatus.ACCEPTED)
                        .body(ApiResponse.success(response, "Compression job submitted")))
                .timeout(Duration.ofSeconds(30))
                .onErrorReturn(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(ApiResponse.error("Failed to create compression job")));
    }

    /**
     * Get compression result.
     */
    @GetMapping("/jobs/{jobId}/result")
    @Operation(summary = "Get compression result", description = "Get the result of a completed compression job")
    public Mono<ResponseEntity<ApiResponse<CompressionResultResponse>>> getCompressionResult(
            @PathVariable String jobId) {

        return jobRepository.findById(jobId)
                .filter(job -> job.status() == JobStatus.COMPLETED)
                .map(job -> CompressionResultResponse.builder()
                        .jobId(job.id())
                        .outputFileId(job.outputFileId())
                        .downloadUrl("/api/v1/files/" + job.outputFileId() + "/download")
                        .build())
                .map(response -> ResponseEntity.ok(ApiResponse.success(response)))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }
}
