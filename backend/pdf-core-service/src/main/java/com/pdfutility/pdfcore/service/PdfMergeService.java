package com.pdfutility.pdfcore.service;

import reactor.core.publisher.Mono;

import java.nio.ByteBuffer;
import java.util.List;

/**
 * PDF Merge Service Interface - Reactive.
 */
public interface PdfMergeService {

    /**
     * Merge multiple PDF files into one.
     *
     * @param fileContents List of PDF file contents as byte buffers
     * @param preserveBookmarks Whether to preserve bookmarks from source files
     * @return Merged PDF content as byte buffer
     */
    Mono<ByteBuffer> mergePdfs(List<ByteBuffer> fileContents, boolean preserveBookmarks);

    /**
     * Merge PDFs from storage file IDs.
     *
     * @param fileIds List of file IDs in storage
     * @param preserveBookmarks Whether to preserve bookmarks
     * @param userId User ID for tracking
     * @return Job ID for tracking the merge operation
     */
    Mono<String> mergePdfsAsync(List<String> fileIds, boolean preserveBookmarks, String userId);
}
