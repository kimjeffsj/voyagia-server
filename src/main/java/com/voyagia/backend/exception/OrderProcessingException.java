package com.voyagia.backend.exception;

/**
 * Exception thrown when order processing fails due to business logic violations
 */
public class OrderProcessingException extends RuntimeException {

    private final String orderNumber;
    private final String processingStage;

    public OrderProcessingException(String message) {
        super(message);
        this.orderNumber = null;
        this.processingStage = null;
    }

    public OrderProcessingException(String message, Throwable cause) {
        super(message, cause);
        this.orderNumber = null;
        this.processingStage = null;
    }

    public OrderProcessingException(String message, String orderNumber, String processingStage) {
        super(message);
        this.orderNumber = orderNumber;
        this.processingStage = processingStage;
    }

    public OrderProcessingException(String message, String orderNumber, String processingStage, Throwable cause) {
        super(message, cause);
        this.orderNumber = orderNumber;
        this.processingStage = processingStage;
    }
    
    public String getOrderNumber() {
        return orderNumber;
    }

    public String getProcessingStage() {
        return processingStage;
    }
}