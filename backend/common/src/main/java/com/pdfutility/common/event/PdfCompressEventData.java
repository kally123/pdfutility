package com.pdfutility.common.event;

import lombok.Builder;
import lombok.Value;

/**
 * Event data for PDF compress operations.
 */
@Value
@Builder
public class PdfCompressEventData {
    String jobId;
    String userId;
    String sourceFileId;
    String outputFileId;
    Long originalSizeBytes;
    Long compressedSizeBytes;
    Double compressionRatio;
    String compressionLevel; // LOW, MEDIUM, HIGH
    String errorMessage;
}
