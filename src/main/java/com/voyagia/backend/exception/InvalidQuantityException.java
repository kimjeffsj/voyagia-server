package com.voyagia.backend.exception;

public class InvalidQuantityException extends RuntimeException {

    private final Integer quantity;
    private final Integer availableStock;


    public InvalidQuantityException(String message) {
        super(message);
        this.quantity = null;
        this.availableStock = null;
    }

    public InvalidQuantityException(Integer quantity, String reason) {
        super("Invalid quantity " + quantity + ": " + reason);
        this.quantity = quantity;
        this.availableStock = null;
    }

    public InvalidQuantityException(Integer quantity, Integer availableStock) {
        super("Requested quantity " + quantity + " exceeds available stock " + availableStock);
        this.quantity = quantity;
        this.availableStock = availableStock;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public Integer getAvailableStock() {
        return availableStock;
    }
}
