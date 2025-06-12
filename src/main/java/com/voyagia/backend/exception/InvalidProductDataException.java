package com.voyagia.backend.exception;

/**
 * Invalid product data exception
 * <p>
 * - negative price or 0
 * - negative inventory
 * - required field is missing
 * - invalid category
 * - invalid product weight or size
 * <p>
 * TODO: will be mapped as HTTP 400 Bad Request, at @ControllerAdvice
 */
public class InvalidProductDataException extends RuntimeException {
    private final String field; // invalid fields
    private final Object invalidValue; // invalid value

    public InvalidProductDataException(String field, Object invalidValue) {
        super("Invalid data for field '" + field + "': " + invalidValue);
        this.field = field;
        this.invalidValue = invalidValue;
    }

    public InvalidProductDataException(String message) {
        super(message);
        this.field = null;
        this.invalidValue = null;
    }

    public InvalidProductDataException(String message, Throwable cause) {
        super(message, cause);
        this.field = null;
        this.invalidValue = null;
    }

    public String getField() {
        return field;
    }

    public Object getInvalidValue() {
        return invalidValue;
    }
}
