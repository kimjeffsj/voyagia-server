package com.voyagia.backend.exception;

/**
 * Product is already exists exception
 * <p>
 * - Trying to create product with the same sku
 * - Trying to create product with the same slug
 * <p>
 * TODO: will be mapped with HTTP 409 at @ControllerAdvice
 */
public class ProductAlreadyExistsException extends RuntimeException {
    private final String field; // "sku" or "slug"
    private final String value; // Duplicated value

    public ProductAlreadyExistsException(String field, String value) {
        super("Product already exists with " + field + ": " + value);
        this.field = field;
        this.value = value;
    }

    public ProductAlreadyExistsException(String message) {
        super(message);
        this.field = null;
        this.value = null;
    }

    public ProductAlreadyExistsException(String message, Throwable cause) {
        super(message, cause);
        this.field = null;
        this.value = null;
    }

    public String getField() {
        return field;
    }

    public String getValue() {
        return value;
    }
}
