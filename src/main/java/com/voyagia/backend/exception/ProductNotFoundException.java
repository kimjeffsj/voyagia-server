package com.voyagia.backend.exception;

/**
 * Product not found exception
 * <p>
 * - Find by product id
 * - Find by SKU
 * - Find by Slug
 * <p>
 * TODO: will be mapped with HTTP 404 in @ControllerAdvice
 */
public class ProductNotFoundException extends RuntimeException {
    private final Long productId;
    private final String identifier; // sku or slug
    private final String field; // "id", "sku", "slug"

    public ProductNotFoundException(Long productId) {
        super("Product not found with id: " + productId);
        this.productId = productId;
        this.identifier = null;
        this.field = "id";
    }

    public ProductNotFoundException(String identifier, String field) {
        super("Product not found with " + field + ": " + identifier);
        this.productId = null;
        this.identifier = identifier;
        this.field = field;
    }

    public ProductNotFoundException(String message) {
        super(message);
        this.productId = null;
        this.identifier = null;
        this.field = null;
    }

    public ProductNotFoundException(String message, Throwable cause) {
        super(message, cause);
        this.productId = null;
        this.identifier = null;
        this.field = null;
    }

    public Long getProductId() {
        return productId;
    }

    public String getIdentifier() {
        return identifier;
    }

    public String getField() {
        return field;
    }
}
