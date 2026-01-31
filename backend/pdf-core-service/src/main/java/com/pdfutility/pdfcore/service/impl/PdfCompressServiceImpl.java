package com.pdfutility.pdfcore.service.impl;

import com.pdfutility.common.exception.PdfProcessingException;
import com.pdfutility.pdfcore.model.CompressionLevel;
import com.pdfutility.pdfcore.model.JobStatus;
import com.pdfutility.pdfcore.model.JobType;
import com.pdfutility.pdfcore.model.PdfJob;
import com.pdfutility.pdfcore.repository.PdfJobRepository;
import com.pdfutility.pdfcore.service.PdfCompressService;
import com.pdfutility.pdfcore.service.StorageClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.graphics.PDXObject;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * PDF Compression Service Implementation.
 * Uses PDFBox for compression with various optimization levels.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PdfCompressServiceImpl implements PdfCompressService {

    private final PdfJobRepository jobRepository;
    private final StorageClient storageClient;

    @Override
    public Mono<ByteBuffer> compressPdf(ByteBuffer fileContent, CompressionLevel level,
                                         boolean removeMetadata, boolean optimizeImages) {
        return Mono.fromCallable(() -> performCompression(fileContent, level, removeMetadata, optimizeImages))
                .subscribeOn(Schedulers.boundedElastic())
                .doOnSuccess(result -> log.info("Successfully compressed PDF with level: {}", level))
                .doOnError(error -> log.error("Failed to compress PDF", error));
    }

    @Override
    public Mono<String> compressPdfAsync(String fileId, CompressionLevel level,
                                          boolean removeMetadata, boolean optimizeImages, String userId) {
        String jobId = UUID.randomUUID().toString();
        String parameters = String.format(
                "{\"compressionLevel\":\"%s\",\"removeMetadata\":%b,\"optimizeImages\":%b}",
                level, removeMetadata, optimizeImages);

        PdfJob job = PdfJob.createNew(jobId, userId, JobType.COMPRESS, List.of(fileId), parameters);

        return jobRepository.save(job)
                .doOnSuccess(savedJob -> {
                    // Fire and forget - process asynchronously
                    processCompressJob(savedJob, level, removeMetadata, optimizeImages)
                            .subscribe(
                                    result -> log.info("Compress job {} completed", jobId),
                                    error -> log.error("Compress job {} failed", jobId, error)
                            );
                })
                .map(PdfJob::id);
    }

    @Override
    public Mono<CompressionResult> getCompressionResult(String jobId) {
        return jobRepository.findById(jobId)
                .filter(job -> job.status() == JobStatus.COMPLETED)
                .map(job -> new CompressionResult(
                        job.id(),
                        job.outputFileId(),
                        0L, // Would need to store these in parameters
                        0L,
                        0.0
                ));
    }

    /**
     * Process compression job asynchronously.
     */
    private Mono<Void> processCompressJob(PdfJob job, CompressionLevel level,
                                           boolean removeMetadata, boolean optimizeImages) {
        String fileId = job.inputFileIds().get(0);

        return jobRepository.updateStatus(job.id(), JobStatus.PROCESSING, LocalDateTime.now())
                .then(storageClient.downloadFile(fileId))
                .flatMap(fileContent -> compressPdf(fileContent, level, removeMetadata, optimizeImages))
                .flatMap(compressedContent ->
                        storageClient.uploadFile(compressedContent, "compressed_" + job.id() + ".pdf", "application/pdf"))
                .flatMap(outputFileId ->
                        jobRepository.markCompleted(job.id(), outputFileId, LocalDateTime.now()))
                .then()
                .onErrorResume(error -> {
                    log.error("Compress job {} failed", job.id(), error);
                    return jobRepository.markFailed(job.id(), error.getMessage(), LocalDateTime.now())
                            .then(Mono.error(error));
                });
    }

    /**
     * Perform the actual PDF compression.
     */
    private ByteBuffer performCompression(ByteBuffer fileContent, CompressionLevel level,
                                           boolean removeMetadata, boolean optimizeImages) {
        byte[] inputBytes = new byte[fileContent.remaining()];
        fileContent.get(inputBytes);
        long originalSize = inputBytes.length;

        try (PDDocument document = Loader.loadPDF(inputBytes)) {
            // Remove metadata if requested
            if (removeMetadata) {
                document.getDocumentInformation().setAuthor(null);
                document.getDocumentInformation().setTitle(null);
                document.getDocumentInformation().setSubject(null);
                document.getDocumentInformation().setKeywords(null);
                document.getDocumentInformation().setCreator(null);
                document.getDocumentInformation().setProducer(null);
            }

            // Optimize images if requested
            if (optimizeImages) {
                optimizeImagesInDocument(document, level);
            }

            // Save with compression
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            document.save(outputStream);

            byte[] compressedBytes = outputStream.toByteArray();
            long compressedSize = compressedBytes.length;
            double ratio = 1.0 - ((double) compressedSize / originalSize);
            
            log.info("Compression complete. Original: {} bytes, Compressed: {} bytes, Ratio: {:.2f}%",
                    originalSize, compressedSize, ratio * 100);

            return ByteBuffer.wrap(compressedBytes);

        } catch (IOException e) {
            throw new PdfProcessingException("Failed to compress PDF: " + e.getMessage(), e);
        }
    }

    /**
     * Optimize images in the document based on compression level.
     */
    private void optimizeImagesInDocument(PDDocument document, CompressionLevel level) {
        float quality = switch (level) {
            case LOW -> 0.9f;
            case MEDIUM -> 0.7f;
            case HIGH -> 0.5f;
        };

        for (PDPage page : document.getPages()) {
            PDResources resources = page.getResources();
            if (resources == null) continue;

            try {
                for (COSName name : resources.getXObjectNames()) {
                    PDXObject xObject = resources.getXObject(name);
                    if (xObject instanceof PDImageXObject) {
                        // Image optimization would be done here
                        // PDFBox 3.x has different API for image manipulation
                        log.debug("Found image: {} for optimization", name.getName());
                    }
                }
            } catch (IOException e) {
                log.warn("Error optimizing images on page", e);
            }
        }
    }
}
