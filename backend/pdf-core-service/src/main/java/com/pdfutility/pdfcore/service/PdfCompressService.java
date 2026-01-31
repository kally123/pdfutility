package com.pdfutility.pdfcore.service;

import com.pdfutility.pdfcore.model.CompressionLevel;
import reactor.core.publisher.Mono;

import java.nio.ByteBuffer;

/**
 * PDF Compression Service Interface - Reactive.
 */
public interface PdfCompressService {

    /**
     * Compress a PDF file.
     *
     * @param fileContent PDF file content as byte buffer
     * @param level Compression level
     * @param removeMetadata Whether to remove metadata
     * @param optimizeImages Whether to optimize images
     * @return Compressed PDF content as byte buffer
     */
    Mono<ByteBuffer> compressPdf(ByteBuffer fileContent, CompressionLevel level, 
                                  boolean removeMetadata, boolean optimizeImages);

    /**
     * Compress PDF from storage file ID asynchronously.
     *
     * @param fileId File ID in storage
     * @param level Compression level
     * @param removeMetadata Whether to remove metadata
     * @param optimizeImages Whether to optimize images
     * @param userId User ID for tracking
     * @return Job ID for tracking the compression operation
     */
    Mono<String> compressPdfAsync(String fileId, CompressionLevel level, 
                                   boolean removeMetadata, boolean optimizeImages, String userId);

    /**
     * Get compression result for a completed job.
     *
     * @param jobId Job ID
     * @return Compression result with statistics
     */
    Mono<CompressionResult> getCompressionResult(String jobId);

    /**
     * Compression result record.
     */
    record CompressionResult(
            String jobId,
            String outputFileId,
            long originalSize,
            long compressedSize,
            double compressionRatio
    ) {}
}
