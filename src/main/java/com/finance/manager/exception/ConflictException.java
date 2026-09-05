package com.finance.manager.exception;

/**
 * Thrown when a request would create a duplicate resource, such as
 * registering an already-used email or creating a category name that
 * already exists for the current user. Mapped to HTTP 409 Conflict.
 */
public class ConflictException extends RuntimeException {
    public ConflictException(String message) {
        super(message);
    }
}
