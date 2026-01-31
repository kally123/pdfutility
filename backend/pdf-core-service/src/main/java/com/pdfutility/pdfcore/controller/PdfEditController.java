package com.pdfutility.pdfcore.controller;

import com.pdfutility.common.dto.ApiResponse;
import com.pdfutility.pdfcore.dto.PdfOperationRequests.*;
import com.pdfutility.pdfcore.dto.PdfOperationResponses.JobCreatedResponse;
import com.pdfutility.pdfcore.dto.PdfOperationResponses.PdfInfoResponse;
import com.pdfutility.pdfcore.model.JobStatus;
import com.pdfutility.pdfcore.model.JobType;
import com.pdfutility.pdfcore.service.PdfEditService;
import com.pdfutility.pdfcore.service.StorageClient;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * PDF Edit Controller - Reactive REST API.
 * Provides various PDF editing operations.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/pdf/edit")
@RequiredArgsConstructor
@Tag(name = "PDF Edit", description = "PDF editing operations")
public class PdfEditController {

    private final PdfEditService editService;
    private final StorageClient storageClient;

    /**
     * Add watermark to PDF.
     */
    @PostMapping("/watermark")
    @Operation(summary = "Add watermark", description = "Add text watermark to PDF")
    public Mono<ResponseEntity<ApiResponse<JobCreatedResponse>>> addWatermark(
            @Valid @RequestBody WatermarkRequest request,
            @RequestHeader("X-User-Id") String userId) {

        float opacity = request.getOpacity() != null ? request.getOpacity() : 0.5f;
        int rotation = request.getRotation() != null ? request.getRotation() : 45;

        return storageClient.downloadFile(request.getFileId())
                .flatMap(content -> editService.addWatermark(content, request.getWatermarkText(), opacity, rotation))
                .flatMap(result -> storageClient.uploadFile(result, 
                        "watermarked_" + UUID.randomUUID() + ".pdf", "application/pdf"))
                .map(outputFileId -> JobCreatedResponse.builder()
                        .jobId(outputFileId)
                        .jobType(JobType.WATERMARK)
                        .status(JobStatus.COMPLETED)
                        .message("Watermark added successfully")
                        .createdAt(LocalDateTime.now())
                        .trackingUrl("/api/v1/files/" + outputFileId)
                        .build())
                .map(response -> ResponseEntity.status(HttpStatus.CREATED)
                        .body(ApiResponse.success(response)))
                .timeout(Duration.ofMinutes(2))
                .onErrorReturn(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(ApiResponse.error("Failed to add watermark")));
    }

    /**
     * Rotate PDF pages.
     */
    @PostMapping("/rotate")
    @Operation(summary = "Rotate pages", description = "Rotate PDF pages by specified angle")
    public Mono<ResponseEntity<ApiResponse<JobCreatedResponse>>> rotatePages(
            @Valid @RequestBody RotateRequest request,
            @RequestHeader("X-User-Id") String userId) {

        return storageClient.downloadFile(request.getFileId())
                .flatMap(content -> editService.rotatePages(content, request.getAngle(), request.getPageNumbers()))
                .flatMap(result -> storageClient.uploadFile(result, 
                        "rotated_" + UUID.randomUUID() + ".pdf", "application/pdf"))
                .map(outputFileId -> JobCreatedResponse.builder()
                        .jobId(outputFileId)
                        .jobType(JobType.ROTATE)
                        .status(JobStatus.COMPLETED)
                        .message("Pages rotated successfully")
                        .createdAt(LocalDateTime.now())
                        .trackingUrl("/api/v1/files/" + outputFileId)
                        .build())
                .map(response -> ResponseEntity.status(HttpStatus.CREATED)
                        .body(ApiResponse.success(response)))
                .timeout(Duration.ofMinutes(2))
                .onErrorReturn(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(ApiResponse.error("Failed to rotate pages")));
    }

    /**
     * Split PDF / Extract pages.
     */
    @PostMapping("/split")
    @Operation(summary = "Split PDF", description = "Extract pages from PDF")
    public Mono<ResponseEntity<ApiResponse<JobCreatedResponse>>> splitPdf(
            @Valid @RequestBody SplitRequest request,
            @RequestHeader("X-User-Id") String userId) {

        int fromPage = request.getFromPage() != null ? request.getFromPage() : 1;
        int toPage = request.getToPage() != null ? request.getToPage() : Integer.MAX_VALUE;

        return storageClient.downloadFile(request.getFileId())
                .flatMap(content -> editService.extractPages(content, fromPage, toPage))
                .flatMap(result -> storageClient.uploadFile(result, 
                        "extracted_" + UUID.randomUUID() + ".pdf", "application/pdf"))
                .map(outputFileId -> JobCreatedResponse.builder()
                        .jobId(outputFileId)
                        .jobType(JobType.SPLIT)
                        .status(JobStatus.COMPLETED)
                        .message("Pages extracted successfully")
                        .createdAt(LocalDateTime.now())
                        .trackingUrl("/api/v1/files/" + outputFileId)
                        .build())
                .map(response -> ResponseEntity.status(HttpStatus.CREATED)
                        .body(ApiResponse.success(response)))
                .timeout(Duration.ofMinutes(2))
                .onErrorReturn(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(ApiResponse.error("Failed to split PDF")));
    }

    /**
     * Protect PDF with password.
     */
    @PostMapping("/protect")
    @Operation(summary = "Protect PDF", description = "Add password protection to PDF")
    public Mono<ResponseEntity<ApiResponse<JobCreatedResponse>>> protectPdf(
            @Valid @RequestBody ProtectRequest request,
            @RequestHeader("X-User-Id") String userId) {

        boolean allowPrinting = request.getAllowPrinting() != null && request.getAllowPrinting();
        boolean allowCopying = request.getAllowCopying() != null && request.getAllowCopying();

        return storageClient.downloadFile(request.getFileId())
                .flatMap(content -> editService.protectPdf(content, request.getPassword(), 
                        request.getOwnerPassword(), allowPrinting, allowCopying))
                .flatMap(result -> storageClient.uploadFile(result, 
                        "protected_" + UUID.randomUUID() + ".pdf", "application/pdf"))
                .map(outputFileId -> JobCreatedResponse.builder()
                        .jobId(outputFileId)
                        .jobType(JobType.PROTECT)
                        .status(JobStatus.COMPLETED)
                        .message("PDF protected successfully")
                        .createdAt(LocalDateTime.now())
                        .trackingUrl("/api/v1/files/" + outputFileId)
                        .build())
                .map(response -> ResponseEntity.status(HttpStatus.CREATED)
                        .body(ApiResponse.success(response)))
                .timeout(Duration.ofMinutes(2))
                .onErrorReturn(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(ApiResponse.error("Failed to protect PDF")));
    }

    /**
     * Unlock protected PDF.
     */
    @PostMapping("/unlock")
    @Operation(summary = "Unlock PDF", description = "Remove password protection from PDF")
    public Mono<ResponseEntity<ApiResponse<JobCreatedResponse>>> unlockPdf(
            @Valid @RequestBody UnlockRequest request,
            @RequestHeader("X-User-Id") String userId) {

        return storageClient.downloadFile(request.getFileId())
                .flatMap(content -> editService.unlockPdf(content, request.getPassword()))
                .flatMap(result -> storageClient.uploadFile(result, 
                        "unlocked_" + UUID.randomUUID() + ".pdf", "application/pdf"))
                .map(outputFileId -> JobCreatedResponse.builder()
                        .jobId(outputFileId)
                        .jobType(JobType.UNLOCK)
                        .status(JobStatus.COMPLETED)
                        .message("PDF unlocked successfully")
                        .createdAt(LocalDateTime.now())
                        .trackingUrl("/api/v1/files/" + outputFileId)
                        .build())
                .map(response -> ResponseEntity.status(HttpStatus.CREATED)
                        .body(ApiResponse.success(response)))
                .timeout(Duration.ofMinutes(2))
                .onErrorReturn(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(ApiResponse.error("Failed to unlock PDF")));
    }

    /**
     * Get PDF information.
     */
    @GetMapping("/info/{fileId}")
    @Operation(summary = "Get PDF info", description = "Get metadata and information about a PDF")
    public Mono<ResponseEntity<ApiResponse<PdfInfoResponse>>> getPdfInfo(
            @PathVariable String fileId) {

        return storageClient.downloadFile(fileId)
                .flatMap(editService::getPdfInfo)
                .map(info -> info.toBuilder().fileId(fileId).build())
                .map(response -> ResponseEntity.ok(ApiResponse.success(response)))
                .timeout(Duration.ofSeconds(30))
                .onErrorReturn(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(ApiResponse.error("Failed to get PDF info")));
    }

    /**
     * Add text to PDF.
     */
    @PostMapping("/text")
    @Operation(summary = "Add text", description = "Add text to a PDF page")
    public Mono<ResponseEntity<ApiResponse<JobCreatedResponse>>> addText(
            @Valid @RequestBody AddTextRequest request,
            @RequestHeader("X-User-Id") String userId) {

        int page = request.getPageNumber() != null ? request.getPageNumber() : 1;
        float x = request.getX() != null ? request.getX() : 100f;
        float y = request.getY() != null ? request.getY() : 700f;
        int fontSize = request.getFontSize() != null ? request.getFontSize() : 12;
        String fontName = request.getFontName() != null ? request.getFontName() : "Helvetica";
        String color = request.getColor() != null ? request.getColor() : "#000000";

        return storageClient.downloadFile(request.getFileId())
                .flatMap(content -> editService.addText(content, request.getText(), page, x, y, fontSize, fontName, color))
                .flatMap(result -> storageClient.uploadFile(result, 
                        "text_added_" + UUID.randomUUID() + ".pdf", "application/pdf"))
                .map(outputFileId -> JobCreatedResponse.builder()
                        .jobId(outputFileId)
                        .jobType(JobType.EDIT)
                        .status(JobStatus.COMPLETED)
                        .message("Text added successfully")
                        .createdAt(LocalDateTime.now())
                        .trackingUrl("/api/v1/files/" + outputFileId)
                        .build())
                .map(response -> ResponseEntity.status(HttpStatus.CREATED)
                        .body(ApiResponse.success(response)))
                .timeout(Duration.ofMinutes(2))
                .onErrorReturn(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(ApiResponse.error("Failed to add text")));
    }
}

     */
    @PostMapping("/split")
    @Operation(summary = "Split PDF", description = "Extract pages from PDF")
    public Mono<ResponseEntity<ApiResponse<JobCreatedResponse>>> splitPdf(
            @Valid @RequestBody SplitRequest request,
            @RequestHeader("X-User-Id") String userId) {

        int fromPage = request.getFromPage() != null ? request.getFromPage() : 1;
        int toPage = request.getToPage() != null ? request.getToPage() : Integer.MAX_VALUE;

        return storageClient.downloadFile(request.getFileId())
                .flatMap(content -> editService.extractPages(content, fromPage, toPage))
                .flatMap(result -> storageClient.uploadFile(result, 
                        "extracted_" + UUID.randomUUID() + ".pdf", "application/pdf"))
                .map(outputFileId -> JobCreatedResponse.builder()
                        .jobId(outputFileId)
                        .jobType(JobType.SPLIT)
                        .status(JobStatus.COMPLETED)
                        .message("Pages extracted successfully")
                        .createdAt(LocalDateTime.now())
                        .trackingUrl("/api/v1/files/" + outputFileId)
                        .build())
                .map(response -> ResponseEntity.status(HttpStatus.CREATED)
                        .body(ApiResponse.success(response)))
                .timeout(Duration.ofMinutes(2))
                .onErrorReturn(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(ApiResponse.error("Failed to split PDF")));
    }

    /**
     * Protect PDF with password.
     */
    @PostMapping("/protect")
    @Operation(summary = "Protect PDF", description = "Add password protection to PDF")
    public Mono<ResponseEntity<ApiResponse<JobCreatedResponse>>> protectPdf(
            @Valid @RequestBody ProtectRequest request,
            @RequestHeader("X-User-Id") String userId) {

        boolean allowPrinting = request.getAllowPrinting() != null && request.getAllowPrinting();
        boolean allowCopying = request.getAllowCopying() != null && request.getAllowCopying();

        return storageClient.downloadFile(request.getFileId())
                .flatMap(content -> editService.protectPdf(content, request.getPassword(), 
                        request.getOwnerPassword(), allowPrinting, allowCopying))
                .flatMap(result -> storageClient.uploadFile(result, 
                        "protected_" + UUID.randomUUID() + ".pdf", "application/pdf"))
                .map(outputFileId -> JobCreatedResponse.builder()
                        .jobId(outputFileId)
                        .jobType(JobType.PROTECT)
                        .status(JobStatus.COMPLETED)
                        .message("PDF protected successfully")
                        .createdAt(LocalDateTime.now())
                        .trackingUrl("/api/v1/files/" + outputFileId)
                        .build())
                .map(response -> ResponseEntity.status(HttpStatus.CREATED)
                        .body(ApiResponse.success(response)))
                .timeout(Duration.ofMinutes(2))
                .onErrorReturn(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(ApiResponse.error("Failed to protect PDF")));
    }

    /**
     * Unlock protected PDF.
     */
    @PostMapping("/unlock")
    @Operation(summary = "Unlock PDF", description = "Remove password protection from PDF")
    public Mono<ResponseEntity<ApiResponse<JobCreatedResponse>>> unlockPdf(
            @Valid @RequestBody UnlockRequest request,
            @RequestHeader("X-User-Id") String userId) {

        return storageClient.downloadFile(request.getFileId())
                .flatMap(content -> editService.unlockPdf(content, request.getPassword()))
                .flatMap(result -> storageClient.uploadFile(result, 
                        "unlocked_" + UUID.randomUUID() + ".pdf", "application/pdf"))
                .map(outputFileId -> JobCreatedResponse.builder()
                        .jobId(outputFileId)
                        .jobType(JobType.UNLOCK)
                        .status(JobStatus.COMPLETED)
                        .message("PDF unlocked successfully")
                        .createdAt(LocalDateTime.now())
                        .trackingUrl("/api/v1/files/" + outputFileId)
                        .build())
                .map(response -> ResponseEntity.status(HttpStatus.CREATED)
                        .body(ApiResponse.success(response)))
                .timeout(Duration.ofMinutes(2))
                .onErrorReturn(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(ApiResponse.error("Failed to unlock PDF")));
    }

    /**
     * Get PDF information.
     */
    @GetMapping("/info/{fileId}")
    @Operation(summary = "Get PDF info", description = "Get metadata and information about a PDF")
    public Mono<ResponseEntity<ApiResponse<PdfInfoResponse>>> getPdfInfo(
            @PathVariable String fileId) {

        return storageClient.downloadFile(fileId)
                .flatMap(editService::getPdfInfo)
                .map(info -> info.toBuilder().fileId(fileId).build())
                .map(response -> ResponseEntity.ok(ApiResponse.success(response)))
                .timeout(Duration.ofSeconds(30))
                .onErrorReturn(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(ApiResponse.error("Failed to get PDF info")));
    }

    /**
     * Add text to PDF.
     */
    @PostMapping("/text")
    @Operation(summary = "Add text", description = "Add text to a PDF page")
    public Mono<ResponseEntity<ApiResponse<JobCreatedResponse>>> addText(
            @Valid @RequestBody AddTextRequest request,
            @RequestHeader("X-User-Id") String userId) {

        int page = request.getPageNumber() != null ? request.getPageNumber() : 1;
        float x = request.getX() != null ? request.getX() : 100f;
        float y = request.getY() != null ? request.getY() : 700f;
        int fontSize = request.getFontSize() != null ? request.getFontSize() : 12;
        String fontName = request.getFontName() != null ? request.getFontName() : "Helvetica";
        String color = request.getColor() != null ? request.getColor() : "#000000";

        return storageClient.downloadFile(request.getFileId())
                .flatMap(content -> editService.addText(content, request.getText(), page, x, y, fontSize, fontName, color))
                .flatMap(result -> storageClient.uploadFile(result, 
                        "text_added_" + UUID.randomUUID() + ".pdf", "application/pdf"))
                .map(outputFileId -> JobCreatedResponse.builder()
                        .jobId(outputFileId)
                        .jobType(JobType.EDIT)
                        .status(JobStatus.COMPLETED)
                        .message("Text added successfully")
                        .createdAt(LocalDateTime.now())
                        .trackingUrl("/api/v1/files/" + outputFileId)
                        .build())
                .map(response -> ResponseEntity.status(HttpStatus.CREATED)
                        .body(ApiResponse.success(response)))
                .timeout(Duration.ofMinutes(2))
                .onErrorReturn(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(ApiResponse.error("Failed to add text")));
    }
}

     */
    @PostMapping("/split")
    @Operation(summary = "Split PDF", description = "Extract pages from PDF")
    public Mono<ResponseEntity<ApiResponse<JobCreatedResponse>>> splitPdf(
            @Valid @RequestBody SplitRequest request,
            @RequestHeader("X-User-Id") String userId) {

        int fromPage = request.getFromPage() != null ? request.getFromPage() : 1;
        int toPage = request.getToPage() != null ? request.getToPage() : Integer.MAX_VALUE;

        return storageClient.downloadFile(request.getFileId())
                .flatMap(content -> editService.extractPages(content, fromPage, toPage))
                .flatMap(result -> storageClient.uploadFile(result, 
                        "extracted_" + UUID.randomUUID() + ".pdf", "application/pdf"))
                .map(outputFileId -> JobCreatedResponse.builder()
                        .jobId(outputFileId)
                        .jobType(JobType.SPLIT)
                        .status(JobStatus.COMPLETED)
                        .message("Pages extracted successfully")
                        .createdAt(LocalDateTime.now())
                        .trackingUrl("/api/v1/files/" + outputFileId)
                        .build())
                .map(response -> ResponseEntity.status(HttpStatus.CREATED)
                        .body(ApiResponse.success(response)))
                .timeout(Duration.ofMinutes(2))
                .onErrorReturn(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(ApiResponse.error("Failed to split PDF")));
    }

    /**
     * Protect PDF with password.
     */
    @PostMapping("/protect")
    @Operation(summary = "Protect PDF", description = "Add password protection to PDF")
    public Mono<ResponseEntity<ApiResponse<JobCreatedResponse>>> protectPdf(
            @Valid @RequestBody ProtectRequest request,
            @RequestHeader("X-User-Id") String userId) {

        boolean allowPrinting = request.getAllowPrinting() != null && request.getAllowPrinting();
        boolean allowCopying = request.getAllowCopying() != null && request.getAllowCopying();

        return storageClient.downloadFile(request.getFileId())
                .flatMap(content -> editService.protectPdf(content, request.getPassword(), 
                        request.getOwnerPassword(), allowPrinting, allowCopying))
                .flatMap(result -> storageClient.uploadFile(result, 
                        "protected_" + UUID.randomUUID() + ".pdf", "application/pdf"))
                .map(outputFileId -> JobCreatedResponse.builder()
                        .jobId(outputFileId)
                        .jobType(JobType.PROTECT)
                        .status(JobStatus.COMPLETED)
                        .message("PDF protected successfully")
                        .createdAt(LocalDateTime.now())
                        .trackingUrl("/api/v1/files/" + outputFileId)
                        .build())
                .map(response -> ResponseEntity.status(HttpStatus.CREATED)
                        .body(ApiResponse.success(response)))
                .timeout(Duration.ofMinutes(2))
                .onErrorReturn(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(ApiResponse.error("Failed to protect PDF")));
    }

    /**
     * Unlock protected PDF.
     */
    @PostMapping("/unlock")
    @Operation(summary = "Unlock PDF", description = "Remove password protection from PDF")
    public Mono<ResponseEntity<ApiResponse<JobCreatedResponse>>> unlockPdf(
            @Valid @RequestBody UnlockRequest request,
            @RequestHeader("X-User-Id") String userId) {

        return storageClient.downloadFile(request.getFileId())
                .flatMap(content -> editService.unlockPdf(content, request.getPassword()))
                .flatMap(result -> storageClient.uploadFile(result, 
                        "unlocked_" + UUID.randomUUID() + ".pdf", "application/pdf"))
                .map(outputFileId -> JobCreatedResponse.builder()
                        .jobId(outputFileId)
                        .jobType(JobType.UNLOCK)
                        .status(JobStatus.COMPLETED)
                        .message("PDF unlocked successfully")
                        .createdAt(LocalDateTime.now())
                        .trackingUrl("/api/v1/files/" + outputFileId)
                        .build())
                .map(response -> ResponseEntity.status(HttpStatus.CREATED)
                        .body(ApiResponse.success(response)))
                .timeout(Duration.ofMinutes(2))
                .onErrorReturn(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(ApiResponse.error("Failed to unlock PDF")));
    }

    /**
     * Get PDF information.
     */
    @GetMapping("/info/{fileId}")
    @Operation(summary = "Get PDF info", description = "Get metadata and information about a PDF")
    public Mono<ResponseEntity<ApiResponse<PdfInfoResponse>>> getPdfInfo(
            @PathVariable String fileId) {

        return storageClient.downloadFile(fileId)
                .flatMap(editService::getPdfInfo)
                .map(info -> info.toBuilder().fileId(fileId).build())
                .map(response -> ResponseEntity.ok(ApiResponse.success(response)))
                .timeout(Duration.ofSeconds(30))
                .onErrorReturn(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(ApiResponse.error("Failed to get PDF info")));
    }

    /**
     * Add text to PDF.
     */
    @PostMapping("/text")
    @Operation(summary = "Add text", description = "Add text to a PDF page")
    public Mono<ResponseEntity<ApiResponse<JobCreatedResponse>>> addText(
            @Valid @RequestBody AddTextRequest request,
            @RequestHeader("X-User-Id") String userId) {

        int page = request.getPageNumber() != null ? request.getPageNumber() : 1;
        float x = request.getX() != null ? request.getX() : 100f;
        float y = request.getY() != null ? request.getY() : 700f;
        int fontSize = request.getFontSize() != null ? request.getFontSize() : 12;
        String fontName = request.getFontName() != null ? request.getFontName() : "Helvetica";
        String color = request.getColor() != null ? request.getColor() : "#000000";

        return storageClient.downloadFile(request.getFileId())
                .flatMap(content -> editService.addText(content, request.getText(), page, x, y, fontSize, fontName, color))
                .flatMap(result -> storageClient.uploadFile(result, 
                        "text_added_" + UUID.randomUUID() + ".pdf", "application/pdf"))
                .map(outputFileId -> JobCreatedResponse.builder()
                        .jobId(outputFileId)
                        .jobType(JobType.EDIT)
                        .status(JobStatus.COMPLETED)
                        .message("Text added successfully")
                        .createdAt(LocalDateTime.now())
                        .trackingUrl("/api/v1/files/" + outputFileId)
                        .build())
                .map(response -> ResponseEntity.status(HttpStatus.CREATED)
                        .body(ApiResponse.success(response)))
                .timeout(Duration.ofMinutes(2))
                .onErrorReturn(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(ApiResponse.error("Failed to add text")));
    }
}

     */
    @PostMapping("/split")
    @Operation(summary = "Split PDF", description = "Extract pages from PDF")
    public Mono<ResponseEntity<ApiResponse<JobCreatedResponse>>> splitPdf(
            @Valid @RequestBody SplitRequest request,
            @RequestHeader("X-User-Id") String userId) {

        int fromPage = request.getFromPage() != null ? request.getFromPage() : 1;
        int toPage = request.getToPage() != null ? request.getToPage() : Integer.MAX_VALUE;

        return storageClient.downloadFile(request.getFileId())
                .flatMap(content -> editService.extractPages(content, fromPage, toPage))
                .flatMap(result -> storageClient.uploadFile(result, 
                        "extracted_" + UUID.randomUUID() + ".pdf", "application/pdf"))
                .map(outputFileId -> JobCreatedResponse.builder()
                        .jobId(outputFileId)
                        .jobType(JobType.SPLIT)
                        .status(JobStatus.COMPLETED)
                        .message("Pages extracted successfully")
                        .createdAt(LocalDateTime.now())
                        .trackingUrl("/api/v1/files/" + outputFileId)
                        .build())
                .map(response -> ResponseEntity.status(HttpStatus.CREATED)
                        .body(ApiResponse.success(response)))
                .timeout(Duration.ofMinutes(2))
                .onErrorReturn(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(ApiResponse.error("Failed to split PDF")));
    }

    /**
     * Protect PDF with password.
     */
    @PostMapping("/protect")
    @Operation(summary = "Protect PDF", description = "Add password protection to PDF")
    public Mono<ResponseEntity<ApiResponse<JobCreatedResponse>>> protectPdf(
            @Valid @RequestBody ProtectRequest request,
            @RequestHeader("X-User-Id") String userId) {

        boolean allowPrinting = request.getAllowPrinting() != null && request.getAllowPrinting();
        boolean allowCopying = request.getAllowCopying() != null && request.getAllowCopying();

        return storageClient.downloadFile(request.getFileId())
                .flatMap(content -> editService.protectPdf(content, request.getPassword(), 
                        request.getOwnerPassword(), allowPrinting, allowCopying))
                .flatMap(result -> storageClient.uploadFile(result, 
                        "protected_" + UUID.randomUUID() + ".pdf", "application/pdf"))
                .map(outputFileId -> JobCreatedResponse.builder()
                        .jobId(outputFileId)
                        .jobType(JobType.PROTECT)
                        .status(JobStatus.COMPLETED)
                        .message("PDF protected successfully")
                        .createdAt(LocalDateTime.now())
                        .trackingUrl("/api/v1/files/" + outputFileId)
                        .build())
                .map(response -> ResponseEntity.status(HttpStatus.CREATED)
                        .body(ApiResponse.success(response)))
                .timeout(Duration.ofMinutes(2))
                .onErrorReturn(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(ApiResponse.error("Failed to protect PDF")));
    }

    /**
     * Unlock protected PDF.
     */
    @PostMapping("/unlock")
    @Operation(summary = "Unlock PDF", description = "Remove password protection from PDF")
    public Mono<ResponseEntity<ApiResponse<JobCreatedResponse>>> unlockPdf(
            @Valid @RequestBody UnlockRequest request,
            @RequestHeader("X-User-Id") String userId) {

        return storageClient.downloadFile(request.getFileId())
                .flatMap(content -> editService.unlockPdf(content, request.getPassword()))
                .flatMap(result -> storageClient.uploadFile(result, 
                        "unlocked_" + UUID.randomUUID() + ".pdf", "application/pdf"))
                .map(outputFileId -> JobCreatedResponse.builder()
                        .jobId(outputFileId)
                        .jobType(JobType.UNLOCK)
                        .status(JobStatus.COMPLETED)
                        .message("PDF unlocked successfully")
                        .createdAt(LocalDateTime.now())
                        .trackingUrl("/api/v1/files/" + outputFileId)
                        .build())
                .map(response -> ResponseEntity.status(HttpStatus.CREATED)
                        .body(ApiResponse.success(response)))
                .timeout(Duration.ofMinutes(2))
                .onErrorReturn(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(ApiResponse.error("Failed to unlock PDF")));
    }

    /**
     * Get PDF information.
     */
    @GetMapping("/info/{fileId}")
    @Operation(summary = "Get PDF info", description = "Get metadata and information about a PDF")
    public Mono<ResponseEntity<ApiResponse<PdfInfoResponse>>> getPdfInfo(
            @PathVariable String fileId) {

        return storageClient.downloadFile(fileId)
                .flatMap(editService::getPdfInfo)
                .map(info -> info.toBuilder().fileId(fileId).build())
                .map(response -> ResponseEntity.ok(ApiResponse.success(response)))
                .timeout(Duration.ofSeconds(30))
                .onErrorReturn(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(ApiResponse.error("Failed to get PDF info")));
    }

    /**
     * Add text to PDF.
     */
    @PostMapping("/text")
    @Operation(summary = "Add text", description = "Add text to a PDF page")
    public Mono<ResponseEntity<ApiResponse<JobCreatedResponse>>> addText(
            @Valid @RequestBody AddTextRequest request,
            @RequestHeader("X-User-Id") String userId) {

        int page = request.getPageNumber() != null ? request.getPageNumber() : 1;
        float x = request.getX() != null ? request.getX() : 100f;
        float y = request.getY() != null ? request.getY() : 700f;
        int fontSize = request.getFontSize() != null ? request.getFontSize() : 12;
        String fontName = request.getFontName() != null ? request.getFontName() : "Helvetica";
        String color = request.getColor() != null ? request.getColor() : "#000000";

        return storageClient.downloadFile(request.getFileId())
                .flatMap(content -> editService.addText(content, request.getText(), page, x, y, fontSize, fontName, color))
                .flatMap(result -> storageClient.uploadFile(result, 
                        "text_added_" + UUID.randomUUID() + ".pdf", "application/pdf"))
                .map(outputFileId -> JobCreatedResponse.builder()
                        .jobId(outputFileId)
                        .jobType(JobType.EDIT)
                        .status(JobStatus.COMPLETED)
                        .message("Text added successfully")
                        .createdAt(LocalDateTime.now())
                        .trackingUrl("/api/v1/files/" + outputFileId)
                        .build())
                .map(response -> ResponseEntity.status(HttpStatus.CREATED)
                        .body(ApiResponse.success(response)))
                .timeout(Duration.ofMinutes(2))
                .onErrorReturn(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(ApiResponse.error("Failed to add text")));
    }
}
