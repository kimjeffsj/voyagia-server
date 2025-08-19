package com.voyagia.backend.dto.order;

import com.voyagia.backend.entity.OrderStatus;
import com.voyagia.backend.entity.PaymentStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * DTO for updating order status
 */
public class OrderStatusUpdateRequest {

    @NotNull(message = "Order status is required")
    private OrderStatus status;

    private PaymentStatus paymentStatus;

    @Size(max = 100, message = "Tracking number must be less than 100 characters")
    private String trackingNumber;

    @Size(max = 500, message = "Reason must be less than 500 characters")
    private String reason; // For cancellation or other status changes

    @Size(max = 255, message = "Payment transaction ID must be less than 255 characters")
    private String paymentTransactionId;

    @Size(max = 1000, message = "Admin notes must be less than 1000 characters")
    private String adminNotes;

    // Force status change even if validation fails (admin only)
    private boolean forceUpdate = false;

    // Send notification to customer
    private boolean sendNotification = true;

    // Default constructor
    public OrderStatusUpdateRequest() {
    }

    public OrderStatusUpdateRequest(OrderStatus status) {
        this.status = status;
    }

    public OrderStatusUpdateRequest(OrderStatus status, String reason) {
        this.status = status;
        this.reason = reason;
    }

    // Getters and Setters
    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public PaymentStatus getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(PaymentStatus paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public String getTrackingNumber() {
        return trackingNumber;
    }

    public void setTrackingNumber(String trackingNumber) {
        this.trackingNumber = trackingNumber;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getPaymentTransactionId() {
        return paymentTransactionId;
    }

    public void setPaymentTransactionId(String paymentTransactionId) {
        this.paymentTransactionId = paymentTransactionId;
    }

    public String getAdminNotes() {
        return adminNotes;
    }

    public void setAdminNotes(String adminNotes) {
        this.adminNotes = adminNotes;
    }

    public boolean isForceUpdate() {
        return forceUpdate;
    }

    public void setForceUpdate(boolean forceUpdate) {
        this.forceUpdate = forceUpdate;
    }

    public boolean isSendNotification() {
        return sendNotification;
    }

    public void setSendNotification(boolean sendNotification) {
        this.sendNotification = sendNotification;
    }

    // Utility methods
    public boolean isCancellation() {
        return status == OrderStatus.CANCELLED;
    }

    public boolean isShipping() {
        return status == OrderStatus.SHIPPED;
    }

    public boolean isDelivery() {
        return status == OrderStatus.DELIVERED;
    }

    public boolean hasPaymentUpdate() {
        return paymentStatus != null || paymentTransactionId != null;
    }

    public boolean hasTrackingUpdate() {
        return trackingNumber != null && !trackingNumber.trim().isEmpty();
    }

    public boolean hasReason() {
        return reason != null && !reason.trim().isEmpty();
    }

    public boolean requiresReason() {
        return isCancellation(); // Cancellation should have a reason
    }

    public boolean isValidForShipping() {
        return isShipping() && hasTrackingUpdate();
    }

    @Override
    public String toString() {
        return "OrderStatusUpdateRequest{" +
                "status=" + status +
                ", paymentStatus=" + paymentStatus +
                ", hasTrackingNumber=" + hasTrackingUpdate() +
                ", hasReason=" + hasReason() +
                ", forceUpdate=" + forceUpdate +
                ", sendNotification=" + sendNotification +
                '}';
    }
}