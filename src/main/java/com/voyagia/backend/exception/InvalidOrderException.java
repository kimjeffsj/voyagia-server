package com.voyagia.backend.exception;

/**
 * Exception thrown when order data is invalid or business rules are violated
 */
public class InvalidOrderException extends RuntimeException {
    public InvalidOrderException(String message) {
        super(message);
    }

    public InvalidOrderException(String message, Throwable cause) {
        super(message, cause);
    }
}
