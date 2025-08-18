package com.voyagia.backend.exception;

/**
 * Exception thrown when payment processing fails
 */
public class PaymentFailedException extends RuntimeException {

    private final String transactionId;
    private final String paymentMethod;

    public PaymentFailedException(String message) {
        super(message);
        this.transactionId = null;
        this.paymentMethod = null;
    }

    public PaymentFailedException(String message, Throwable cause) {
        super(message, cause);
        this.transactionId = null;
        this.paymentMethod = null;
    }

    public PaymentFailedException(String message, String transactionId, String paymentMethod) {
        super(message);
        this.transactionId = transactionId;
        this.paymentMethod = paymentMethod;
    }

    public PaymentFailedException(String message, String transactionId, String paymentMethod, Throwable cause) {
        super(message, cause);
        this.transactionId = transactionId;
        this.paymentMethod = paymentMethod;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }
}
