package com.voyagia.backend.entity;

public enum PaymentStatus {
    PENDING("Pending"),
    PROCESSING("Processing"),
    PAID("Paid"),
    FAILED("Failed"),
    CANCELLED("Cancelled"),
    REFUNDED("Refunded"),
    PARTIALLY_REFUNDED("Partially Refunded");

    private final String displayName;

    PaymentStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public boolean isSuccessful() {
        return this == PAID;
    }

    public boolean isFinal() {
        return this == PAID || this == FAILED || this == CANCELLED || this == REFUNDED;
    }

    public boolean canBeRefunded() {
        return this == PAID || this == PARTIALLY_REFUNDED;
    }
}