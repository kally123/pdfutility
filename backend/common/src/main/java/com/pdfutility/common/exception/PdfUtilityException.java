package com.pdfutility.common.exception;

/**
 * Base exception for PDF utility application.
 */
public class PdfUtilityException extends RuntimeException {

    private final String errorCode;

    public PdfUtilityException(String message) {
        super(message);
        this.errorCode = "PDF_ERROR";
    }

    public PdfUtilityException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    public PdfUtilityException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = "PDF_ERROR";
    }

    public PdfUtilityException(String message, String errorCode, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }
}
