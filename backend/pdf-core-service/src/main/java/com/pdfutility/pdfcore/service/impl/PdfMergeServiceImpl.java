package com.pdfutility.pdfcore.service.impl;

import com.pdfutility.common.exception.PdfProcessingException;
import com.pdfutility.pdfcore.model.JobType;
import com.pdfutility.pdfcore.model.PdfJob;
import com.pdfutility.pdfcore.repository.PdfJobRepository;
import com.pdfutility.pdfcore.service.PdfMergeService;
import com.pdfutility.pdfcore.service.StorageClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.io.RandomAccessReadBuffer;
import org.apache.pdfbox.multipdf.PDFMergerUtility;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * PDF Merge Service Implementation.
 * Uses PDFBox for merging and runs CPU-intensive operations on boundedElastic scheduler.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PdfMergeServiceImpl implements PdfMergeService {

    private final PdfJobRepository jobRepository;
    private final StorageClient storageClient;

    @Override
    public Mono<ByteBuffer> mergePdfs(List<ByteBuffer> fileContents, boolean preserveBookmarks) {
        return Mono.fromCallable(() -> performMerge(fileContents, preserveBookmarks))
                .subscribeOn(Schedulers.boundedElastic())
                .doOnSuccess(result -> log.info("Successfully merged {} PDFs", fileContents.size()))
                .doOnError(error -> log.error("Failed to merge PDFs", error));
    }

    @Override
    public Mono<String> mergePdfsAsync(List<String> fileIds, boolean preserveBookmarks, String userId) {
        String jobId = UUID.randomUUID().toString();
        
        PdfJob job = PdfJob.createNew(jobId, userId, JobType.MERGE, fileIds, 
                "{\"preserveBookmarks\":" + preserveBookmarks + "}");

        return jobRepository.save(job)
                .doOnSuccess(savedJob -> {
                    // Fire and forget - process asynchronously
                    processMergeJob(savedJob, preserveBookmarks)
                            .subscribe(
                                    result -> log.info("Merge job {} completed", jobId),
                                    error -> log.error("Merge job {} failed", jobId, error)
                            );
                })
                .map(PdfJob::id)
                .doOnSuccess(id -> log.info("Created merge job: {}", id));
    }

    /**
     * Process merge job asynchronously.
     */
    private Mono<Void> processMergeJob(PdfJob job, boolean preserveBookmarks) {
        return Mono.defer(() -> {
            // Update status to processing
            return jobRepository.updateStatus(job.id(), 
                    com.pdfutility.pdfcore.model.JobStatus.PROCESSING, LocalDateTime.now())
                    .then(downloadAndMerge(job, preserveBookmarks));
        });
    }

    /**
     * Download files and merge them.
     */
    private Mono<Void> downloadAndMerge(PdfJob job, boolean preserveBookmarks) {
        return Flux.fromIterable(job.inputFileIds())
                .flatMap(storageClient::downloadFile)
                .collectList()
                .flatMap(fileContents -> mergePdfs(fileContents, preserveBookmarks))
                .flatMap(mergedContent -> 
                        storageClient.uploadFile(mergedContent, "merged_" + job.id() + ".pdf", "application/pdf"))
                .flatMap(outputFileId -> 
                        jobRepository.markCompleted(job.id(), outputFileId, LocalDateTime.now()))
                .then()
                .onErrorResume(error -> {
                    log.error("Merge job {} failed", job.id(), error);
                    return jobRepository.markFailed(job.id(), error.getMessage(), LocalDateTime.now())
                            .then(Mono.error(error));
                });
    }

    /**
     * Perform the actual PDF merge using PDFBox.
     * This is a blocking operation, so it runs on boundedElastic scheduler.
     */
    private ByteBuffer performMerge(List<ByteBuffer> fileContents, boolean preserveBookmarks) {
        PDFMergerUtility merger = new PDFMergerUtility();
        
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            // Add all source PDFs
            for (int i = 0; i < fileContents.size(); i++) {
                ByteBuffer buffer = fileContents.get(i);
                byte[] bytes = new byte[buffer.remaining()];
                buffer.get(bytes);
                merger.addSource(new RandomAccessReadBuffer(bytes));
            }

            merger.setDestinationStream(outputStream);

            // Merge documents (PDFBox 3.x API)
            merger.mergeDocuments(null);

            byte[] mergedBytes = outputStream.toByteArray();
            log.debug("Merged PDF size: {} bytes", mergedBytes.length);
            
            return ByteBuffer.wrap(mergedBytes);
            
        } catch (IOException e) {
            throw new PdfProcessingException("Failed to merge PDFs: " + e.getMessage(), e);
        }
    }
}
