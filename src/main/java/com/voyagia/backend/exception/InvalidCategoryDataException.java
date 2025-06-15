package com.voyagia.backend.exception;

/**
 * Invalid category data exception
 * <p>
 * - required field is missing
 * - category depth over the limit
 * - invalid sort order
 * - invalid parent category
 * - category name is too long or wrong format
 * <p>
 * TODO: will be mapped as HTTP 400 Bad Request, in @ControllerAdvice
 */
public class InvalidCategoryDataException extends RuntimeException {

    private final String field;
    private final Object invalidValue;

    public InvalidCategoryDataException(String field, Object invalidValue) {
        super("Invalid data for field '" + field + "': " + invalidValue);
        this.field = field;
        this.invalidValue = invalidValue;
    }

    public InvalidCategoryDataException(String message) {
        super(message);
        this.field = null;
        this.invalidValue = null;
    }

    public InvalidCategoryDataException(String message, Throwable cause) {
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
