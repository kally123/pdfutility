package com.pdfutility.pdfcore.repository;

import com.pdfutility.pdfcore.model.JobStatus;
import com.pdfutility.pdfcore.model.JobType;
import com.pdfutility.pdfcore.model.PdfJob;
import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.repository.Modifying;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

/**
 * R2DBC Repository for PDF Jobs - REQUIRED for WebFlux.
 * Using ReactiveCrudRepository instead of JPA/Hibernate.
 */
@Repository
public interface PdfJobRepository extends ReactiveCrudRepository<PdfJob, String> {

    /**
     * Find all jobs by user ID with pagination.
     */
    @Query("SELECT * FROM pdf_jobs WHERE user_id = :userId ORDER BY created_at DESC LIMIT :#{#pageable.pageSize} OFFSET :#{#pageable.offset}")
    Flux<PdfJob> findByUserIdOrderByCreatedAtDesc(String userId, Pageable pageable);

    /**
     * Find all jobs by user ID and status.
     */
    @Query("SELECT * FROM pdf_jobs WHERE user_id = :userId AND status = :status ORDER BY created_at DESC")
    Flux<PdfJob> findByUserIdAndStatus(String userId, JobStatus status);

    /**
     * Find all jobs by user ID and job type.
     */
    @Query("SELECT * FROM pdf_jobs WHERE user_id = :userId AND job_type = :jobType ORDER BY created_at DESC")
    Flux<PdfJob> findByUserIdAndJobType(String userId, JobType jobType);

    /**
     * Count jobs by user ID.
     */
    @Query("SELECT COUNT(*) FROM pdf_jobs WHERE user_id = :userId")
    Mono<Long> countByUserId(String userId);

    /**
     * Update job status - Direct update query for performance.
     */
    @Modifying
    @Query("UPDATE pdf_jobs SET status = :status, updated_at = :updatedAt WHERE id = :id")
    Mono<Integer> updateStatus(String id, JobStatus status, LocalDateTime updatedAt);

    /**
     * Update job progress.
     */
    @Modifying
    @Query("UPDATE pdf_jobs SET progress = :progress, updated_at = :updatedAt WHERE id = :id")
    Mono<Integer> updateProgress(String id, Integer progress, LocalDateTime updatedAt);

    /**
     * Mark job as completed.
     */
    @Modifying
    @Query("""
        UPDATE pdf_jobs 
        SET status = 'COMPLETED', 
            output_file_id = :outputFileId, 
            progress = 100, 
            updated_at = :completedAt, 
            completed_at = :completedAt 
        WHERE id = :id
        """)
    Mono<Integer> markCompleted(String id, String outputFileId, LocalDateTime completedAt);

    /**
     * Mark job as failed.
     */
    @Modifying
    @Query("""
        UPDATE pdf_jobs 
        SET status = 'FAILED', 
            error_message = :errorMessage, 
            updated_at = :failedAt, 
            completed_at = :failedAt 
        WHERE id = :id
        """)
    Mono<Integer> markFailed(String id, String errorMessage, LocalDateTime failedAt);

    /**
     * Find pending jobs for processing (for worker/scheduler).
     */
    @Query("SELECT * FROM pdf_jobs WHERE status = 'PENDING' ORDER BY created_at ASC LIMIT :limit")
    Flux<PdfJob> findPendingJobs(int limit);

    /**
     * Find stale processing jobs (jobs stuck in processing for too long).
     */
    @Query("SELECT * FROM pdf_jobs WHERE status = 'PROCESSING' AND updated_at < :threshold")
    Flux<PdfJob> findStaleProcessingJobs(LocalDateTime threshold);

    /**
     * Delete old completed jobs for cleanup.
     */
    @Modifying
    @Query("DELETE FROM pdf_jobs WHERE status = 'COMPLETED' AND completed_at < :threshold")
    Mono<Integer> deleteOldCompletedJobs(LocalDateTime threshold);
}
