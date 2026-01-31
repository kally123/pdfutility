package com.pdfutility.pdfcore.controller;

import com.pdfutility.common.dto.ApiResponse;
import com.pdfutility.pdfcore.dto.PdfOperationRequests.MergeRequest;
import com.pdfutility.pdfcore.dto.PdfOperationResponses.JobCreatedResponse;
import com.pdfutility.pdfcore.dto.PdfOperationResponses.JobStatusResponse;
import com.pdfutility.pdfcore.dto.PdfOperationResponses.MergeResultResponse;
import com.pdfutility.pdfcore.model.JobStatus;
import com.pdfutility.pdfcore.model.PdfJob;
import com.pdfutility.pdfcore.repository.PdfJobRepository;
import com.pdfutility.pdfcore.service.PdfMergeService;
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
 * PDF Merge Controller - Reactive REST API.
 * Follows WebFlux guidelines with proper reactive chains.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/pdf/merge")
@RequiredArgsConstructor
@Tag(name = "PDF Merge", description = "PDF merge operations")
public class PdfMergeController {

    private final PdfMergeService mergeService;
    private final PdfJobRepository jobRepository;

    /**
     * Merge multiple PDFs asynchronously.
     */
    @PostMapping
    @Operation(summary = "Merge PDFs", description = "Merge multiple PDF files into one")
    public Mono<ResponseEntity<ApiResponse<JobCreatedResponse>>> mergePdfs(
            @Valid @RequestBody MergeRequest request,
            @RequestHeader("X-User-Id") String userId) {

        boolean preserveBookmarks = request.getPreserveBookmarks() != null && request.getPreserveBookmarks();

        return mergeService.mergePdfsAsync(request.getFileIds(), preserveBookmarks, userId)
                .map(jobId -> JobCreatedResponse.builder()
                        .jobId(jobId)
                        .jobType(com.pdfutility.pdfcore.model.JobType.MERGE)
                        .status(JobStatus.PENDING)
                        .message("Merge job created successfully")
                        .createdAt(LocalDateTime.now())
                        .trackingUrl("/api/v1/pdf/jobs/" + jobId)
                        .build())
                .map(response -> ResponseEntity.status(HttpStatus.ACCEPTED)
                        .body(ApiResponse.success(response, "Merge job submitted")))
                .timeout(Duration.ofSeconds(30))
                .onErrorReturn(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(ApiResponse.error("Failed to create merge job")));
    }

    /**
     * Get merge job status.
     */
    @GetMapping("/jobs/{jobId}")
    @Operation(summary = "Get merge job status", description = "Check the status of a merge job")
    public Mono<ResponseEntity<ApiResponse<JobStatusResponse>>> getJobStatus(
            @PathVariable String jobId) {

        return jobRepository.findById(jobId)
                .map(this::mapToJobStatusResponse)
                .map(response -> ResponseEntity.ok(ApiResponse.success(response)))
                .defaultIfEmpty(ResponseEntity.notFound().build())
                .timeout(Duration.ofSeconds(10));
    }

    /**
     * Get merge result (for completed jobs).
     */
    @GetMapping("/jobs/{jobId}/result")
    @Operation(summary = "Get merge result", description = "Get the result of a completed merge job")
    public Mono<ResponseEntity<ApiResponse<MergeResultResponse>>> getMergeResult(
            @PathVariable String jobId) {

        return jobRepository.findById(jobId)
                .filter(job -> job.status() == JobStatus.COMPLETED)
                .map(job -> MergeResultResponse.builder()
                        .jobId(job.id())
                        .outputFileId(job.outputFileId())
                        .downloadUrl("/api/v1/files/" + job.outputFileId() + "/download")
                        .inputFileCount(job.inputFileIds().size())
                        .build())
                .map(response -> ResponseEntity.ok(ApiResponse.success(response)))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    /**
     * Cancel a pending merge job.
     */
    @DeleteMapping("/jobs/{jobId}")
    @Operation(summary = "Cancel merge job", description = "Cancel a pending merge job")
    public Mono<ResponseEntity<ApiResponse<Void>>> cancelJob(
            @PathVariable String jobId) {

        return jobRepository.findById(jobId)
                .filter(job -> job.status() == JobStatus.PENDING)
                .flatMap(job -> jobRepository.updateStatus(jobId, JobStatus.CANCELLED, LocalDateTime.now()))
                .map(updated -> ResponseEntity.ok(ApiResponse.<Void>success(null, "Job cancelled")))
                .defaultIfEmpty(ResponseEntity.badRequest()
                        .body(ApiResponse.error("Job cannot be cancelled")));
    }

    private JobStatusResponse mapToJobStatusResponse(PdfJob job) {
        String downloadUrl = job.status() == JobStatus.COMPLETED && job.outputFileId() != null
                ? "/api/v1/files/" + job.outputFileId() + "/download"
                : null;

        return JobStatusResponse.builder()
                .jobId(job.id())
                .jobType(job.jobType())
                .status(job.status())
                .progress(job.progress())
                .inputFileIds(job.inputFileIds())
                .outputFileId(job.outputFileId())
                .downloadUrl(downloadUrl)
                .errorMessage(job.errorMessage())
                .createdAt(job.createdAt())
                .updatedAt(job.updatedAt())
                .completedAt(job.completedAt())
                .build();
    }
}
