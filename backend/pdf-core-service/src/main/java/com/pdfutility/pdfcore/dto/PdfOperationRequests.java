package com.pdfutility.pdfcore.dto;

import com.pdfutility.pdfcore.model.CompressionLevel;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Value;

import java.util.List;

/**
 * Request DTOs for PDF operations - Immutable using @Value.
 */
public final class PdfOperationRequests {

    private PdfOperationRequests() {
    }

    /**
     * Request to merge multiple PDFs.
     */
    @Value
    @Builder
    public static class MergeRequest {
        @NotEmpty(message = "At least two file IDs are required for merging")
        List<String> fileIds;
        
        String outputFileName;
        
        Boolean preserveBookmarks;
    }

    /**
     * Request to compress a PDF.
     */
    @Value
    @Builder
    public static class CompressRequest {
        @NotNull(message = "File ID is required")
        String fileId;
        
        CompressionLevel compressionLevel;
        
        Boolean removeMetadata;
        
        Boolean optimizeImages;
    }

    /**
     * Request to split a PDF.
     */
    @Value
    @Builder
    public static class SplitRequest {
        @NotNull(message = "File ID is required")
        String fileId;
        
        List<Integer> pageNumbers; // Specific pages to extract
        
        Integer fromPage;
        
        Integer toPage;
        
        SplitMode splitMode;
    }

    /**
     * Request to add watermark to PDF.
     */
    @Value
    @Builder
    public static class WatermarkRequest {
        @NotNull(message = "File ID is required")
        String fileId;
        
        String watermarkText;
        
        String watermarkImageId;
        
        Float opacity;
        
        Integer rotation;
        
        WatermarkPosition position;
    }

    /**
     * Request to rotate PDF pages.
     */
    @Value
    @Builder
    public static class RotateRequest {
        @NotNull(message = "File ID is required")
        String fileId;
        
        @NotNull(message = "Rotation angle is required")
        Integer angle; // 90, 180, 270
        
        List<Integer> pageNumbers; // null = all pages
    }

    /**
     * Request to protect PDF with password.
     */
    @Value
    @Builder
    public static class ProtectRequest {
        @NotNull(message = "File ID is required")
        String fileId;
        
        @NotNull(message = "Password is required")
        String password;
        
        String ownerPassword;
        
        Boolean allowPrinting;
        
        Boolean allowCopying;
        
        Boolean allowEditing;
    }

    /**
     * Request to unlock a protected PDF.
     */
    @Value
    @Builder
    public static class UnlockRequest {
        @NotNull(message = "File ID is required")
        String fileId;
        
        @NotNull(message = "Password is required")
        String password;
    }

    /**
     * Request to add text to PDF.
     */
    @Value
    @Builder
    public static class AddTextRequest {
        @NotNull(message = "File ID is required")
        String fileId;
        
        @NotNull(message = "Text content is required")
        String text;
        
        Integer pageNumber;
        
        Float x;
        
        Float y;
        
        String fontName;
        
        Integer fontSize;
        
        String color;
    }

    public enum SplitMode {
        EXTRACT_PAGES,    // Extract specific pages
        SPLIT_BY_RANGE,   // Split by page range
        SPLIT_EVERY_N,    // Split every N pages
        SPLIT_BY_SIZE     // Split by file size
    }

    public enum WatermarkPosition {
        CENTER,
        TOP_LEFT,
        TOP_RIGHT,
        BOTTOM_LEFT,
        BOTTOM_RIGHT,
        DIAGONAL
    }
}
