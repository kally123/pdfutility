package com.pdfutility.pdfcore.dto;

import com.pdfutility.pdfcore.model.JobStatus;
import com.pdfutility.pdfcore.model.JobType;
import lombok.Builder;
import lombok.Value;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Response DTOs for PDF operations - Immutable using @Value.
 */
public final class PdfOperationResponses {

    private PdfOperationResponses() {
    }

    /**
     * Response for job creation.
     */
    @Value
    @Builder
    public static class JobCreatedResponse {
        String jobId;
        JobType jobType;
        JobStatus status;
        String message;
        LocalDateTime createdAt;
        String trackingUrl;
    }

    /**
     * Response for job status query.
     */
    @Value
    @Builder
    public static class JobStatusResponse {
        String jobId;
        JobType jobType;
        JobStatus status;
        Integer progress;
        List<String> inputFileIds;
        String outputFileId;
        String downloadUrl;
        String errorMessage;
        LocalDateTime createdAt;
        LocalDateTime updatedAt;
        LocalDateTime completedAt;
    }

    /**
     * Response for PDF information.
     */
    @Value
    @Builder(toBuilder = true)
    public static class PdfInfoResponse {
        String fileId;
        String fileName;
        Long fileSizeBytes;
        Integer pageCount;
        String author;
        String title;
        String subject;
        LocalDateTime createdDate;
        LocalDateTime modifiedDate;
        Boolean isEncrypted;
        Boolean hasSignatures;
        String pdfVersion;
        PdfDimensions dimensions;
    }

    /**
     * PDF dimensions.
     */
    @Value
    @Builder
    public static class PdfDimensions {
        Float width;
        Float height;
        String unit;
    }

    /**
     * Response for compression result.
     */
    @Value
    @Builder
    public static class CompressionResultResponse {
        String jobId;
        String outputFileId;
        String downloadUrl;
        Long originalSizeBytes;
        Long compressedSizeBytes;
        Double compressionRatio;
        String compressionPercentage;
    }

    /**
     * Response for merge result.
     */
    @Value
    @Builder
    public static class MergeResultResponse {
        String jobId;
        String outputFileId;
        String downloadUrl;
        Integer inputFileCount;
        Integer totalPageCount;
        Long totalSizeBytes;
    }

    /**
     * Response for split result.
     */
    @Value
    @Builder
    public static class SplitResultResponse {
        String jobId;
        List<SplitFileInfo> outputFiles;
        Integer totalPagesExtracted;
    }

    /**
     * Information about a split output file.
     */
    @Value
    @Builder
    public static class SplitFileInfo {
        String fileId;
        String fileName;
        String downloadUrl;
        Integer pageCount;
        List<Integer> pageNumbers;
    }
}
