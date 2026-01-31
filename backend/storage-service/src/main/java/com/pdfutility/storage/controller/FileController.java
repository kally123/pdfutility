package com.pdfutility.storage.controller;

import com.pdfutility.common.dto.ApiResponse;
import com.pdfutility.storage.dto.StorageDto.*;
import com.pdfutility.storage.service.FileStorageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.ByteBuffer;
import java.time.Duration;

/**
 * File Storage Controller - Reactive REST API.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/files")
@RequiredArgsConstructor
@Tag(name = "File Storage", description = "File upload, download, and management")
public class FileController {

    private final FileStorageService storageService;

    /**
     * Upload a file.
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload file", description = "Upload a file to storage")
    public Mono<ResponseEntity<ApiResponse<FileUploadResponse>>> uploadFile(
            @RequestPart("file") FilePart file,
            @RequestParam(defaultValue = "false") boolean temporary,
            @RequestHeader("X-User-Id") String userId) {

        return storageService.uploadFile(file, userId, temporary)
                .map(response -> ResponseEntity.status(HttpStatus.CREATED)
                        .body(ApiResponse.success(response, "File uploaded successfully")))
                .timeout(Duration.ofMinutes(5))
                .onErrorResume(e -> Mono.just(ResponseEntity.badRequest()
                        .body(ApiResponse.error(e.getMessage()))));
    }

    /**
     * Download a file.
     */
    @GetMapping("/{fileId}/download")
    @Operation(summary = "Download file", description = "Download a file from storage")
    public Mono<ResponseEntity<Flux<DataBuffer>>> downloadFile(
            @PathVariable String fileId) {

        return storageService.getFileInfo(fileId)
                .flatMap(info -> 
                    storageService.downloadFile(fileId)
                            .map(content -> {
                                DataBuffer buffer = new DefaultDataBufferFactory().wrap(content);
                                return ResponseEntity.ok()
                                        .header(HttpHeaders.CONTENT_DISPOSITION, 
                                                "attachment; filename=\"" + info.getFileName() + "\"")
                                        .header(HttpHeaders.CONTENT_TYPE, info.getContentType())
                                        .header(HttpHeaders.CONTENT_LENGTH, String.valueOf(info.getSizeBytes()))
                                        .body(Flux.just(buffer));
                            }))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    /**
     * Get file content (for internal service use).
     */
    @GetMapping("/{fileId}/content")
    @Operation(summary = "Get file content", description = "Get raw file content")
    public Mono<ResponseEntity<Flux<DataBuffer>>> getFileContent(
            @PathVariable String fileId) {

        return storageService.downloadFile(fileId)
                .map(content -> {
                    DataBuffer buffer = new DefaultDataBufferFactory().wrap(content);
                    return ResponseEntity.ok()
                            .contentType(MediaType.APPLICATION_OCTET_STREAM)
                            .body(Flux.just(buffer));
                })
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    /**
     * Get file metadata.
     */
    @GetMapping("/{fileId}/metadata")
    @Operation(summary = "Get file metadata", description = "Get file information and metadata")
    public Mono<ResponseEntity<ApiResponse<FileInfoResponse>>> getFileInfo(
            @PathVariable String fileId) {

        return storageService.getFileInfo(fileId)
                .map(response -> ResponseEntity.ok(ApiResponse.success(response)))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    /**
     * Delete a file.
     */
    @DeleteMapping("/{fileId}")
    @Operation(summary = "Delete file", description = "Delete a file from storage")
    public Mono<ResponseEntity<ApiResponse<Void>>> deleteFile(
            @PathVariable String fileId,
            @RequestHeader("X-User-Id") String userId) {

        return storageService.deleteFile(fileId, userId)
                .then(Mono.just(ResponseEntity.ok(
                        ApiResponse.<Void>success(null, "File deleted successfully"))))
                .onErrorResume(e -> Mono.just(ResponseEntity.badRequest()
                        .body(ApiResponse.error(e.getMessage()))));
    }

    /**
     * List user files.
     */
    @GetMapping
    @Operation(summary = "List files", description = "List all files for the current user")
    public Mono<ResponseEntity<ApiResponse<FileListResponse>>> listFiles(
            @RequestHeader("X-User-Id") String userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        return storageService.listFiles(userId, page, size)
                .map(response -> ResponseEntity.ok(ApiResponse.success(response)));
    }

    /**
     * Get storage usage.
     */
    @GetMapping("/usage")
    @Operation(summary = "Get storage usage", description = "Get storage usage statistics for the current user")
    public Mono<ResponseEntity<ApiResponse<StorageUsageResponse>>> getStorageUsage(
            @RequestHeader("X-User-Id") String userId) {

        return storageService.getStorageUsage(userId)
                .map(response -> ResponseEntity.ok(ApiResponse.success(response)));
    }
}
