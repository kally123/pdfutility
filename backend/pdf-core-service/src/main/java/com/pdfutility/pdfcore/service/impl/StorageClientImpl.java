package com.pdfutility.pdfcore.service.impl;

import com.pdfutility.common.exception.StorageException;
import com.pdfutility.pdfcore.service.StorageClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.nio.ByteBuffer;
import java.time.Duration;

/**
 * Storage Client Implementation using WebClient (Reactive HTTP Client).
 * REQUIRED: Use WebClient instead of RestTemplate for WebFlux.
 */
@Slf4j
@Component
public class StorageClientImpl implements StorageClient {

    private final WebClient webClient;
    private final Duration timeout;

    public StorageClientImpl(
            WebClient.Builder webClientBuilder,
            @Value("${storage.service.url}") String storageServiceUrl,
            @Value("${storage.service.timeout:30s}") Duration timeout) {
        this.webClient = webClientBuilder
                .baseUrl(storageServiceUrl)
                .build();
        this.timeout = timeout;
    }

    @Override
    public Mono<ByteBuffer> downloadFile(String fileId) {
        return webClient.get()
                .uri("/api/v1/files/{fileId}/content", fileId)
                .retrieve()
                .bodyToFlux(DataBuffer.class)
                .reduce(DataBuffer::write)
                .map(dataBuffer -> {
                    ByteBuffer byteBuffer = ByteBuffer.allocate(dataBuffer.readableByteCount());
                    dataBuffer.toByteBuffer(byteBuffer);
                    DataBufferUtils.release(dataBuffer);
                    byteBuffer.flip();
                    return byteBuffer;
                })
                .timeout(timeout)
                .doOnSuccess(buffer -> log.debug("Downloaded file: {}", fileId))
                .onErrorMap(e -> new StorageException("Failed to download file: " + fileId, e));
    }

    @Override
    public Mono<String> uploadFile(ByteBuffer content, String fileName, String contentType) {
        MultipartBodyBuilder bodyBuilder = new MultipartBodyBuilder();
        byte[] bytes = new byte[content.remaining()];
        content.get(bytes);
        bodyBuilder.part("file", bytes)
                .filename(fileName)
                .contentType(MediaType.parseMediaType(contentType));

        return webClient.post()
                .uri("/api/v1/files")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData(bodyBuilder.build()))
                .retrieve()
                .bodyToMono(UploadResponse.class)
                .map(UploadResponse::fileId)
                .timeout(timeout)
                .doOnSuccess(fileId -> log.debug("Uploaded file: {} -> {}", fileName, fileId))
                .onErrorMap(e -> new StorageException("Failed to upload file: " + fileName, e));
    }

    @Override
    public Mono<Void> deleteFile(String fileId) {
        return webClient.delete()
                .uri("/api/v1/files/{fileId}", fileId)
                .retrieve()
                .bodyToMono(Void.class)
                .timeout(timeout)
                .doOnSuccess(v -> log.debug("Deleted file: {}", fileId))
                .onErrorMap(e -> new StorageException("Failed to delete file: " + fileId, e));
    }

    @Override
    public Mono<FileMetadata> getFileMetadata(String fileId) {
        return webClient.get()
                .uri("/api/v1/files/{fileId}/metadata", fileId)
                .retrieve()
                .bodyToMono(FileMetadata.class)
                .timeout(timeout)
                .onErrorMap(e -> new StorageException("Failed to get file metadata: " + fileId, e));
    }

    /**
     * Upload response from storage service.
     */
    private record UploadResponse(String fileId, String fileName, String downloadUrl) {}
}
