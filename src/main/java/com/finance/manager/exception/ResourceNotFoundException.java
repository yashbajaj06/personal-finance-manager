package com.finance.manager.exception;

/**
 * Thrown when a requested resource (transaction, category, goal, etc.) does
 * not exist or is not visible to the current user. Mapped to HTTP 404 Not Found.
 */
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
