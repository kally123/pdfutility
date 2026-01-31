package com.pdfutility.storage.model;

import lombok.Builder;
import lombok.With;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * File metadata entity - R2DBC compatible.
 */
@Table("files")
@Builder
@With
public record FileMetadata(
        @Id
        String id,

        @Column("user_id")
        String userId,

        @Column("original_name")
        String originalName,

        @Column("stored_name")
        String storedName,

        @Column("content_type")
        String contentType,

        @Column("size_bytes")
        Long sizeBytes,

        @Column("checksum")
        String checksum,

        @Column("storage_provider")
        String storageProvider,

        @Column("storage_path")
        String storagePath,

        @Column("is_temporary")
        Boolean isTemporary,

        @Column("expires_at")
        LocalDateTime expiresAt,

        @Column("created_at")
        LocalDateTime createdAt,

        @Column("updated_at")
        LocalDateTime updatedAt
) {
    public static FileMetadata create(String userId, String originalName, String contentType, 
                                       Long sizeBytes, String checksum, String storageProvider,
                                       String storagePath, boolean isTemporary, LocalDateTime expiresAt) {
        LocalDateTime now = LocalDateTime.now();
        String id = UUID.randomUUID().toString();
        String storedName = id + "_" + sanitizeFileName(originalName);
        
        return FileMetadata.builder()
                .id(id)
                .userId(userId)
                .originalName(originalName)
                .storedName(storedName)
                .contentType(contentType)
                .sizeBytes(sizeBytes)
                .checksum(checksum)
                .storageProvider(storageProvider)
                .storagePath(storagePath)
                .isTemporary(isTemporary)
                .expiresAt(expiresAt)
                .createdAt(now)
                .updatedAt(now)
                .build();
    }

    private static String sanitizeFileName(String fileName) {
        return fileName.replaceAll("[^a-zA-Z0-9.-]", "_");
    }

    public String getDownloadUrl() {
        return "/api/v1/files/" + id + "/download";
    }

    public boolean isExpired() {
        return isTemporary && expiresAt != null && expiresAt.isBefore(LocalDateTime.now());
    }
}
