package com.pdfutility.storage.dto;

import lombok.Builder;
import lombok.Value;

import java.time.LocalDateTime;

/**
 * Storage DTOs - Immutable.
 */
public final class StorageDto {

    private StorageDto() {}

    @Value
    @Builder
    public static class FileUploadResponse {
        String fileId;
        String fileName;
        String contentType;
        Long sizeBytes;
        String downloadUrl;
        LocalDateTime createdAt;
        LocalDateTime expiresAt;
    }

    @Value
    @Builder
    public static class FileInfoResponse {
        String fileId;
        String fileName;
        String contentType;
        Long sizeBytes;
        String checksum;
        String downloadUrl;
        Boolean isTemporary;
        LocalDateTime createdAt;
        LocalDateTime expiresAt;
    }

    @Value
    @Builder
    public static class StorageUsageResponse {
        Long totalBytes;
        Long usedBytes;
        Long availableBytes;
        Integer fileCount;
        String formattedUsed;
        String formattedTotal;
    }

    @Value
    @Builder
    public static class FileListResponse {
        java.util.List<FileInfoResponse> files;
        Integer page;
        Integer size;
        Long totalElements;
        Integer totalPages;
    }
}
