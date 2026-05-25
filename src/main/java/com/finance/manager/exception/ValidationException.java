package com.finance.manager.exception;

/**
 * Thrown on invalid input data (400).
 */
public class ValidationException extends RuntimeException {
    public ValidationException(String message) {
        super(message);
    }
}
