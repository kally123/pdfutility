package com.pdfutility.pdfcore.controller;

import com.pdfutility.common.dto.ApiResponse;
import com.pdfutility.common.dto.PageResponse;
import com.pdfutility.pdfcore.dto.PdfOperationResponses.JobStatusResponse;
import com.pdfutility.pdfcore.model.JobStatus;
import com.pdfutility.pdfcore.model.JobType;
import com.pdfutility.pdfcore.model.PdfJob;
import com.pdfutility.pdfcore.repository.PdfJobRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

/**
 * PDF Jobs Controller - Manages all PDF job operations.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/pdf/jobs")
@RequiredArgsConstructor
@Tag(name = "PDF Jobs", description = "PDF job management operations")
public class PdfJobController {

    private final PdfJobRepository jobRepository;

    /**
     * Get all jobs for a user with pagination.
     */
    @GetMapping
    @Operation(summary = "List jobs", description = "Get all PDF jobs for the current user")
    public Mono<ResponseEntity<ApiResponse<PageResponse<JobStatusResponse>>>> getJobs(
            @RequestHeader("X-User-Id") String userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        return jobRepository.findByUserIdOrderByCreatedAtDesc(userId, PageRequest.of(page, size))
                .map(this::mapToJobStatusResponse)
                .collectList()
                .zipWith(jobRepository.countByUserId(userId))
                .map(tuple -> PageResponse.of(tuple.getT1(), page, size, tuple.getT2()))
                .map(pageResponse -> ResponseEntity.ok(ApiResponse.success(pageResponse)))
                .timeout(Duration.ofSeconds(30));
    }

    /**
     * Get job by ID.
     */
    @GetMapping("/{jobId}")
    @Operation(summary = "Get job", description = "Get a specific PDF job by ID")
    public Mono<ResponseEntity<ApiResponse<JobStatusResponse>>> getJob(
            @PathVariable String jobId) {

        return jobRepository.findById(jobId)
                .map(this::mapToJobStatusResponse)
                .map(response -> ResponseEntity.ok(ApiResponse.success(response)))
                .defaultIfEmpty(ResponseEntity.notFound().build())
                .timeout(Duration.ofSeconds(10));
    }

    /**
     * Get jobs by status.
     */
    @GetMapping("/status/{status}")
    @Operation(summary = "Get jobs by status", description = "Get all jobs with a specific status")
    public Mono<ResponseEntity<ApiResponse<List<JobStatusResponse>>>> getJobsByStatus(
            @RequestHeader("X-User-Id") String userId,
            @PathVariable JobStatus status) {

        return jobRepository.findByUserIdAndStatus(userId, status)
                .map(this::mapToJobStatusResponse)
                .collectList()
                .map(jobs -> ResponseEntity.ok(ApiResponse.success(jobs)))
                .timeout(Duration.ofSeconds(30));
    }

    /**
     * Get jobs by type.
     */
    @GetMapping("/type/{type}")
    @Operation(summary = "Get jobs by type", description = "Get all jobs of a specific type")
    public Mono<ResponseEntity<ApiResponse<List<JobStatusResponse>>>> getJobsByType(
            @RequestHeader("X-User-Id") String userId,
            @PathVariable JobType type) {

        return jobRepository.findByUserIdAndJobType(userId, type)
                .map(this::mapToJobStatusResponse)
                .collectList()
                .map(jobs -> ResponseEntity.ok(ApiResponse.success(jobs)))
                .timeout(Duration.ofSeconds(30));
    }

    /**
     * Cancel a job.
     */
    @PostMapping("/{jobId}/cancel")
    @Operation(summary = "Cancel job", description = "Cancel a pending job")
    public Mono<ResponseEntity<ApiResponse<Void>>> cancelJob(
            @PathVariable String jobId,
            @RequestHeader("X-User-Id") String userId) {

        return jobRepository.findById(jobId)
                .filter(job -> job.userId().equals(userId))
                .filter(job -> job.status() == JobStatus.PENDING || job.status() == JobStatus.PROCESSING)
                .flatMap(job -> jobRepository.updateStatus(jobId, JobStatus.CANCELLED, LocalDateTime.now()))
                .map(updated -> ResponseEntity.ok(ApiResponse.<Void>success(null, "Job cancelled successfully")))
                .defaultIfEmpty(ResponseEntity.badRequest()
                        .body(ApiResponse.error("Job cannot be cancelled")));
    }

    /**
     * Delete a completed/failed job.
     */
    @DeleteMapping("/{jobId}")
    @Operation(summary = "Delete job", description = "Delete a completed or failed job")
    public Mono<ResponseEntity<ApiResponse<Void>>> deleteJob(
            @PathVariable String jobId,
            @RequestHeader("X-User-Id") String userId) {

        return jobRepository.findById(jobId)
                .filter(job -> job.userId().equals(userId))
                .filter(job -> job.status() == JobStatus.COMPLETED || 
                              job.status() == JobStatus.FAILED || 
                              job.status() == JobStatus.CANCELLED)
                .flatMap(job -> jobRepository.delete(job))
                .then(Mono.just(ResponseEntity.ok(ApiResponse.<Void>success(null, "Job deleted successfully"))))
                .defaultIfEmpty(ResponseEntity.badRequest()
                        .body(ApiResponse.error("Job cannot be deleted")));
    }

    /**
     * Retry a failed job.
     */
    @PostMapping("/{jobId}/retry")
    @Operation(summary = "Retry job", description = "Retry a failed job")
    public Mono<ResponseEntity<ApiResponse<JobStatusResponse>>> retryJob(
            @PathVariable String jobId,
            @RequestHeader("X-User-Id") String userId) {

        return jobRepository.findById(jobId)
                .filter(job -> job.userId().equals(userId))
                .filter(job -> job.status() == JobStatus.FAILED)
                .flatMap(job -> {
                    PdfJob retriedJob = job.withStatus(JobStatus.PENDING)
                            .withProgress(0)
                            .withErrorMessage(null)
                            .withUpdatedAt(LocalDateTime.now())
                            .withCompletedAt(null);
                    return jobRepository.save(retriedJob);
                })
                .map(this::mapToJobStatusResponse)
                .map(response -> ResponseEntity.ok(ApiResponse.success(response, "Job queued for retry")))
                .defaultIfEmpty(ResponseEntity.badRequest()
                        .body(ApiResponse.error("Job cannot be retried")));
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
