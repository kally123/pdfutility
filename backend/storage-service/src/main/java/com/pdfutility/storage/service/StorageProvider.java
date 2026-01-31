package com.pdfutility.storage.service;

import reactor.core.publisher.Mono;

import java.nio.ByteBuffer;

/**
 * Storage Provider Interface - abstraction for different storage backends.
 */
public interface StorageProvider {

    /**
     * Store a file.
     *
     * @param path Storage path
     * @param content File content
     * @param contentType Content type
     * @return Storage result with path
     */
    Mono<String> store(String path, ByteBuffer content, String contentType);

    /**
     * Retrieve a file.
     *
     * @param path Storage path
     * @return File content
     */
    Mono<ByteBuffer> retrieve(String path);

    /**
     * Delete a file.
     *
     * @param path Storage path
     * @return Completion signal
     */
    Mono<Void> delete(String path);

    /**
     * Check if file exists.
     *
     * @param path Storage path
     * @return True if exists
     */
    Mono<Boolean> exists(String path);

    /**
     * Get storage provider name.
     *
     * @return Provider name
     */
    String getProviderName();
}
