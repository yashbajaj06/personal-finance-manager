package com.finance.manager.exception;

/**
 * Thrown when an authenticated user attempts to access or modify a resource
 * they do not own, or a protected system resource. Mapped to HTTP 403 Forbidden.
 */
public class ForbiddenException extends RuntimeException {
    public ForbiddenException(String message) {
        super(message);
    }
}
