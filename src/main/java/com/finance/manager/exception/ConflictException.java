package com.finance.manager.exception;

/**
 * Thrown on duplicate resource conflicts (409).
 */
public class ConflictException extends RuntimeException {
    public ConflictException(String message) {
        super(message);
    }
}
