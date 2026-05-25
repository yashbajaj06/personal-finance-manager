package com.finance.manager.exception;

/**
 * Thrown when access to another user's resource is attempted (403).
 */
public class ForbiddenException extends RuntimeException {
    public ForbiddenException(String message) {
        super(message);
    }
}
