package com.pdfutility.common.exception;

/**
 * Exception thrown for authentication/authorization failures.
 */
public class AuthenticationException extends PdfUtilityException {

    public AuthenticationException(String message) {
        super(message, "AUTH_ERROR");
    }

    public AuthenticationException(String message, Throwable cause) {
        super(message, "AUTH_ERROR", cause);
    }
}
