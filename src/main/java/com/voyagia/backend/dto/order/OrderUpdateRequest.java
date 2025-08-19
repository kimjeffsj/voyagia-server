package com.voyagia.backend.dto.order;

import jakarta.validation.constraints.Size;

/**
 * DTO for updating order information (non-status changes)
 */
public class OrderUpdateRequest {

    // Shipping information updates
    @Size(max = 50, message = "First name must be less than 50 characters")
    private String shippingFirstName;

    @Size(max = 50, message = "Last name must be less than 50 characters")
    private String shippingLastName;

    @Size(max = 20, message = "Phone must be less than 20 characters")
    private String shippingPhone;

    @Size(max = 500, message = "Address must be less than 500 characters")
    private String shippingAddress;

    @Size(max = 100, message = "City must be less than 100 characters")
    private String shippingCity;

    @Size(max = 100, message = "State must be less than 100 characters")
    private String shippingState;

    @Size(max = 20, message = "Postal code must be less than 20 characters")
    private String shippingPostalCode;

    @Size(max = 100, message = "Country must be less than 100 characters")
    private String shippingCountry;

    // Tracking and administrative information
    @Size(max = 100, message = "Tracking number must be less than 100 characters")
    private String trackingNumber;

    @Size(max = 1000, message = "Notes must be less than 1000 characters")
    private String notes;

    // Administrative fields
    @Size(max = 255, message = "Payment transaction ID must be less than 255 characters")
    private String paymentTransactionId;

    // Default constructor
    public OrderUpdateRequest() {
    }

    // Getters and Setters
    public String getShippingFirstName() {
        return shippingFirstName;
    }

    public void setShippingFirstName(String shippingFirstName) {
        this.shippingFirstName = shippingFirstName;
    }

    public String getShippingLastName() {
        return shippingLastName;
    }

    public void setShippingLastName(String shippingLastName) {
        this.shippingLastName = shippingLastName;
    }

    public String getShippingPhone() {
        return shippingPhone;
    }

    public void setShippingPhone(String shippingPhone) {
        this.shippingPhone = shippingPhone;
    }

    public String getShippingAddress() {
        return shippingAddress;
    }

    public void setShippingAddress(String shippingAddress) {
        this.shippingAddress = shippingAddress;
    }

    public String getShippingCity() {
        return shippingCity;
    }

    public void setShippingCity(String shippingCity) {
        this.shippingCity = shippingCity;
    }

    public String getShippingState() {
        return shippingState;
    }

    public void setShippingState(String shippingState) {
        this.shippingState = shippingState;
    }

    public String getShippingPostalCode() {
        return shippingPostalCode;
    }

    public void setShippingPostalCode(String shippingPostalCode) {
        this.shippingPostalCode = shippingPostalCode;
    }

    public String getShippingCountry() {
        return shippingCountry;
    }

    public void setShippingCountry(String shippingCountry) {
        this.shippingCountry = shippingCountry;
    }

    public String getTrackingNumber() {
        return trackingNumber;
    }

    public void setTrackingNumber(String trackingNumber) {
        this.trackingNumber = trackingNumber;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getPaymentTransactionId() {
        return paymentTransactionId;
    }

    public void setPaymentTransactionId(String paymentTransactionId) {
        this.paymentTransactionId = paymentTransactionId;
    }

    // Utility methods
    public boolean hasShippingUpdates() {
        return shippingFirstName != null || shippingLastName != null ||
                shippingPhone != null || shippingAddress != null ||
                shippingCity != null || shippingState != null ||
                shippingPostalCode != null || shippingCountry != null;
    }

    public boolean hasTrackingUpdate() {
        return trackingNumber != null;
    }

    public boolean hasNotesUpdate() {
        return notes != null;
    }

    public boolean hasPaymentUpdate() {
        return paymentTransactionId != null;
    }

    public boolean hasAnyUpdates() {
        return hasShippingUpdates() || hasTrackingUpdate() ||
                hasNotesUpdate() || hasPaymentUpdate();
    }

    @Override
    public String toString() {
        return "OrderUpdateRequest{" +
                "hasShippingUpdates=" + hasShippingUpdates() +
                ", hasTrackingUpdate=" + hasTrackingUpdate() +
                ", hasNotesUpdate=" + hasNotesUpdate() +
                ", hasPaymentUpdate=" + hasPaymentUpdate() +
                '}';
    }
}