package com.voyagia.backend.exception;

/**
 * Insufficient Stock exception
 * <p>
 * - order quantity > stock
 * - add cart quantity > stock
 * - When stock becomes negative
 * <p>
 * TODO: will be mapped as HTTP 409 Conflict, in @ControllerAdvice
 */
public class InsufficientStockException extends RuntimeException {
    private final Long productId;
    private final String productName;
    private final Integer requestedQuantity;
    private final Integer availableQuantity;

    public InsufficientStockException(Long productId, String productName, Integer requestedQuantity,
            Integer availableQuantity) {
        super(String.format("Insufficient stock for product '%s' (ID: %d). " +
                "Requested: %d, Available: %d",
                productName, productId, requestedQuantity, availableQuantity));
        this.productId = productId;
        this.productName = productName;
        this.requestedQuantity = requestedQuantity;
        this.availableQuantity = availableQuantity;
    }

    public InsufficientStockException(String productName, Integer requestedQuantity, Integer availableQuantity) {
        super(String.format("Insufficient stock for product '%s'. " +
                "Requested: %d, Available: %d",
                productName, requestedQuantity, availableQuantity));
        this.productId = null;
        this.productName = productName;
        this.requestedQuantity = requestedQuantity;
        this.availableQuantity = availableQuantity;
    }

    public InsufficientStockException(String message) {
        super(message);
        this.productId = null;
        this.productName = null;
        this.requestedQuantity = null;
        this.availableQuantity = null;
    }

    public InsufficientStockException(String message, Throwable cause) {
        super(message, cause);
        this.productId = null;
        this.productName = null;
        this.requestedQuantity = null;
        this.availableQuantity = null;
    }

    public Long getProductId() {
        return productId;
    }

    public String getProductName() {
        return productName;
    }

    public Integer getRequestedQuantity() {
        return requestedQuantity;
    }

    public Integer getAvailableQuantity() {
        return availableQuantity;
    }
}
