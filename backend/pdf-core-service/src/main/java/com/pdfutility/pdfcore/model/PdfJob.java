package com.pdfutility.pdfcore.model;

import lombok.Builder;
import lombok.With;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;
import java.util.List;

/**
 * PDF Job entity - R2DBC compatible (no JPA annotations).
 * Using @With for immutable updates as per WebFlux guidelines.
 */
@Table("pdf_jobs")
@Builder
@With
public record PdfJob(
        @Id
        String id,

        @Column("user_id")
        String userId,

        @Column("job_type")
        JobType jobType,

        @Column("status")
        JobStatus status,

        @Column("input_file_ids")
        List<String> inputFileIds,

        @Column("output_file_id")
        String outputFileId,

        @Column("parameters")
        String parameters,

        @Column("error_message")
        String errorMessage,

        @Column("progress")
        Integer progress,

        @Column("created_at")
        LocalDateTime createdAt,

        @Column("updated_at")
        LocalDateTime updatedAt,

        @Column("completed_at")
        LocalDateTime completedAt
) {
    public static PdfJob createNew(String id, String userId, JobType jobType, List<String> inputFileIds, String parameters) {
        LocalDateTime now = LocalDateTime.now();
        return PdfJob.builder()
                .id(id)
                .userId(userId)
                .jobType(jobType)
                .status(JobStatus.PENDING)
                .inputFileIds(inputFileIds)
                .parameters(parameters)
                .progress(0)
                .createdAt(now)
                .updatedAt(now)
                .build();
    }

    public PdfJob markProcessing() {
        return this.withStatus(JobStatus.PROCESSING)
                .withUpdatedAt(LocalDateTime.now());
    }

    public PdfJob markCompleted(String outputFileId) {
        LocalDateTime now = LocalDateTime.now();
        return this.withStatus(JobStatus.COMPLETED)
                .withOutputFileId(outputFileId)
                .withProgress(100)
                .withUpdatedAt(now)
                .withCompletedAt(now);
    }

    public PdfJob markFailed(String errorMessage) {
        LocalDateTime now = LocalDateTime.now();
        return this.withStatus(JobStatus.FAILED)
                .withErrorMessage(errorMessage)
                .withUpdatedAt(now)
                .withCompletedAt(now);
    }

    public PdfJob updateProgress(int progress) {
        return this.withProgress(progress)
                .withUpdatedAt(LocalDateTime.now());
    }
}
