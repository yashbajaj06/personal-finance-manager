package com.finance.manager.exception;

/**
 * Thrown when a request fails a business-rule validation that isn't captured
 * by standard bean validation annotations. Mapped to HTTP 400 Bad Request.
 */
public class ValidationException extends RuntimeException {
    public ValidationException(String message) {
        super(message);
    }
}
