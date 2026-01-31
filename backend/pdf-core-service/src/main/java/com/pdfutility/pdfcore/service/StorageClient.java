package com.pdfutility.pdfcore.service;

import reactor.core.publisher.Mono;

import java.nio.ByteBuffer;

/**
 * Storage Client Interface - Reactive WebClient based.
 * Used to communicate with the Storage Service.
 */
public interface StorageClient {

    /**
     * Download a file from storage.
     *
     * @param fileId File ID
     * @return File content as ByteBuffer
     */
    Mono<ByteBuffer> downloadFile(String fileId);

    /**
     * Upload a file to storage.
     *
     * @param content File content
     * @param fileName File name
     * @param contentType Content type
     * @return File ID of the uploaded file
     */
    Mono<String> uploadFile(ByteBuffer content, String fileName, String contentType);

    /**
     * Delete a file from storage.
     *
     * @param fileId File ID
     * @return Completion signal
     */
    Mono<Void> deleteFile(String fileId);

    /**
     * Get file metadata.
     *
     * @param fileId File ID
     * @return File metadata
     */
    Mono<FileMetadata> getFileMetadata(String fileId);

    /**
     * File metadata record.
     */
    record FileMetadata(
            String fileId,
            String fileName,
            String contentType,
            long size,
            String checksum
    ) {}
}
