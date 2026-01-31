package com.pdfutility.common.exception;

/**
 * Exception thrown when file storage operations fail.
 */
public class StorageException extends PdfUtilityException {

    public StorageException(String message) {
        super(message, "STORAGE_ERROR");
    }

    public StorageException(String message, Throwable cause) {
        super(message, "STORAGE_ERROR", cause);
    }
}
