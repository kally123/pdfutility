package com.pdfutility.storage.service;

import com.pdfutility.common.exception.ResourceNotFoundException;
import com.pdfutility.common.exception.StorageException;
import com.pdfutility.storage.dto.StorageDto.*;
import com.pdfutility.storage.model.FileMetadata;
import com.pdfutility.storage.repository.FileMetadataRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;

/**
 * File Storage Service - Reactive implementation.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FileStorageService {

    private final FileMetadataRepository fileMetadataRepository;
    private final StorageProvider storageProvider;

    @Value("${storage.temp-file-expiration:24h}")
    private Duration tempFileExpiration;

    @Value("${storage.allowed-extensions:pdf,png,jpg,jpeg}")
    private List<String> allowedExtensions;

    /**
     * Upload a file.
     */
    public Mono<FileUploadResponse> uploadFile(FilePart filePart, String userId, boolean isTemporary) {
        String originalName = filePart.filename();
        String contentType = filePart.headers().getContentType() != null 
                ? filePart.headers().getContentType().toString() 
                : "application/octet-stream";

        // Validate file extension
        if (!isAllowedExtension(originalName)) {
            return Mono.error(new StorageException("File type not allowed: " + getExtension(originalName)));
        }

        return filePart.content()
                .reduce(ByteBuffer.allocate(0), (acc, buffer) -> {
                    ByteBuffer newBuffer = ByteBuffer.allocate(acc.remaining() + buffer.readableByteCount());
                    newBuffer.put(acc);
                    byte[] bytes = new byte[buffer.readableByteCount()];
                    buffer.read(bytes);
                    newBuffer.put(bytes);
                    newBuffer.flip();
                    return newBuffer;
                })
                .flatMap(content -> uploadContent(content, originalName, contentType, userId, isTemporary));
    }

    /**
     * Upload raw content.
     */
    public Mono<FileUploadResponse> uploadContent(ByteBuffer content, String fileName, 
                                                    String contentType, String userId, 
                                                    boolean isTemporary) {
        String checksum = calculateChecksum(content.duplicate());
        long sizeBytes = content.remaining();
        LocalDateTime expiresAt = isTemporary 
                ? LocalDateTime.now().plus(tempFileExpiration) 
                : null;

        // Create storage path
        String storagePath = generateStoragePath(userId, fileName);

        return storageProvider.store(storagePath, content, contentType)
                .flatMap(path -> {
                    FileMetadata metadata = FileMetadata.create(
                            userId, fileName, contentType, sizeBytes, checksum,
                            storageProvider.getProviderName(), storagePath, isTemporary, expiresAt);

                    return fileMetadataRepository.save(metadata);
                })
                .map(this::mapToUploadResponse)
                .doOnSuccess(response -> log.info("File uploaded: {} for user: {}", response.getFileId(), userId));
    }

    /**
     * Download a file.
     */
    public Mono<ByteBuffer> downloadFile(String fileId) {
        return fileMetadataRepository.findById(fileId)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("File", fileId)))
                .flatMap(metadata -> {
                    if (metadata.isExpired()) {
                        return Mono.error(new StorageException("File has expired"));
                    }
                    return storageProvider.retrieve(metadata.storagePath());
                });
    }

    /**
     * Get file metadata.
     */
    public Mono<FileInfoResponse> getFileInfo(String fileId) {
        return fileMetadataRepository.findById(fileId)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("File", fileId)))
                .map(this::mapToInfoResponse);
    }

    /**
     * Delete a file.
     */
    public Mono<Void> deleteFile(String fileId, String userId) {
        return fileMetadataRepository.findById(fileId)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("File", fileId)))
                .filter(metadata -> metadata.userId().equals(userId))
                .switchIfEmpty(Mono.error(new StorageException("Access denied")))
                .flatMap(metadata -> 
                    storageProvider.delete(metadata.storagePath())
                            .then(fileMetadataRepository.deleteFileById(fileId)))
                .then()
                .doOnSuccess(v -> log.info("File deleted: {} by user: {}", fileId, userId));
    }

    /**
     * List files for a user.
     */
    public Mono<FileListResponse> listFiles(String userId, int page, int size) {
        return fileMetadataRepository.findByUserIdOrderByCreatedAtDesc(userId, PageRequest.of(page, size))
                .map(this::mapToInfoResponse)
                .collectList()
                .zipWith(fileMetadataRepository.countByUserId(userId))
                .map(tuple -> {
                    List<FileInfoResponse> files = tuple.getT1();
                    long total = tuple.getT2();
                    int totalPages = size > 0 ? (int) Math.ceil((double) total / size) : 0;
                    
                    return FileListResponse.builder()
                            .files(files)
                            .page(page)
                            .size(size)
                            .totalElements(total)
                            .totalPages(totalPages)
                            .build();
                });
    }

    /**
     * Get storage usage for a user.
     */
    public Mono<StorageUsageResponse> getStorageUsage(String userId) {
        return fileMetadataRepository.getTotalStorageUsedByUser(userId)
                .zipWith(fileMetadataRepository.countByUserId(userId))
                .map(tuple -> {
                    long usedBytes = tuple.getT1();
                    long fileCount = tuple.getT2();
                    long totalBytes = 10L * 1024 * 1024 * 1024; // 10 GB limit example
                    
                    return StorageUsageResponse.builder()
                            .totalBytes(totalBytes)
                            .usedBytes(usedBytes)
                            .availableBytes(totalBytes - usedBytes)
                            .fileCount((int) fileCount)
                            .formattedUsed(formatBytes(usedBytes))
                            .formattedTotal(formatBytes(totalBytes))
                            .build();
                });
    }

    /**
     * Cleanup expired temporary files.
     */
    public Flux<String> cleanupExpiredFiles() {
        return fileMetadataRepository.findExpiredTemporaryFiles(LocalDateTime.now())
                .flatMap(metadata -> 
                    storageProvider.delete(metadata.storagePath())
                            .then(fileMetadataRepository.deleteFileById(metadata.id()))
                            .thenReturn(metadata.id()))
                .doOnNext(id -> log.info("Cleaned up expired file: {}", id));
    }

    // ========== Helper Methods ==========

    private String generateStoragePath(String userId, String fileName) {
        String date = LocalDateTime.now().toLocalDate().toString();
        String uniqueName = System.currentTimeMillis() + "_" + sanitizeFileName(fileName);
        return userId + "/" + date + "/" + uniqueName;
    }

    private String sanitizeFileName(String fileName) {
        return fileName.replaceAll("[^a-zA-Z0-9.-]", "_");
    }

    private boolean isAllowedExtension(String fileName) {
        String extension = getExtension(fileName).toLowerCase();
        return allowedExtensions.contains(extension);
    }

    private String getExtension(String fileName) {
        int lastDot = fileName.lastIndexOf('.');
        return lastDot > 0 ? fileName.substring(lastDot + 1) : "";
    }

    private String calculateChecksum(ByteBuffer content) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            digest.update(content);
            return Base64.getEncoder().encodeToString(digest.digest());
        } catch (NoSuchAlgorithmException e) {
            return null;
        }
    }

    private String formatBytes(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.2f KB", bytes / 1024.0);
        if (bytes < 1024 * 1024 * 1024) return String.format("%.2f MB", bytes / (1024.0 * 1024));
        return String.format("%.2f GB", bytes / (1024.0 * 1024 * 1024));
    }

    private FileUploadResponse mapToUploadResponse(FileMetadata metadata) {
        return FileUploadResponse.builder()
                .fileId(metadata.id())
                .fileName(metadata.originalName())
                .contentType(metadata.contentType())
                .sizeBytes(metadata.sizeBytes())
                .downloadUrl(metadata.getDownloadUrl())
                .createdAt(metadata.createdAt())
                .expiresAt(metadata.expiresAt())
                .build();
    }

    private FileInfoResponse mapToInfoResponse(FileMetadata metadata) {
        return FileInfoResponse.builder()
                .fileId(metadata.id())
                .fileName(metadata.originalName())
                .contentType(metadata.contentType())
                .sizeBytes(metadata.sizeBytes())
                .checksum(metadata.checksum())
                .downloadUrl(metadata.getDownloadUrl())
                .isTemporary(metadata.isTemporary())
                .createdAt(metadata.createdAt())
                .expiresAt(metadata.expiresAt())
                .build();
    }
}
