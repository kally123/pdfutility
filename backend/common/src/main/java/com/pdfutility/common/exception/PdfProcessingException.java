package com.pdfutility.common.exception;

/**
 * Exception thrown when PDF processing fails.
 */
public class PdfProcessingException extends PdfUtilityException {

    public PdfProcessingException(String message) {
        super(message, "PDF_PROCESSING_ERROR");
    }

    public PdfProcessingException(String message, Throwable cause) {
        super(message, "PDF_PROCESSING_ERROR", cause);
    }
}
