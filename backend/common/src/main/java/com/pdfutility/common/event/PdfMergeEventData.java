package com.pdfutility.common.event;

import lombok.Builder;
import lombok.Value;

import java.util.List;

/**
 * Event data for PDF merge operations.
 */
@Value
@Builder
public class PdfMergeEventData {
    String jobId;
    String userId;
    List<String> sourceFileIds;
    String outputFileName;
    String outputFileId;
    Long totalSizeBytes;
    Integer pageCount;
    String errorMessage;
}
