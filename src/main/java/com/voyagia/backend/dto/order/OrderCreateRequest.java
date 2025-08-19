package com.voyagia.backend.dto.order;

import com.voyagia.backend.entity.PaymentMethod;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.List;


/**
 * DTO for creating a new order
 */
public class OrderCreateRequest {

    // Shipping Information
    @NotBlank(message = "First name is required")
    @Size(max = 50, message = "First name must be less than 50 characters")
    private String shippingFirstName;

    @NotBlank(message = "Last name is required")
    @Size(max = 50, message = "Last name must be less than 50 characters")
    private String shippingLastName;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    @Size(max = 100, message = "Email must be less than 100 characters")
    private String shippingEmail;

    @Size(max = 20, message = "Phone must be less than 20 characters")
    @Pattern(regexp = "^[+]?[0-9\\s\\-()]{10,20}$", message = "Invalid phone number format")
    private String shippingPhone;

    @NotBlank(message = "Address is required")
    @Size(max = 500, message = "Address must be less than 500 characters")
    private String shippingAddress;

    @NotBlank(message = "City is required")
    @Size(max = 100, message = "City must be less than 100 characters")
    private String shippingCity;

    @Size(max = 100, message = "State must be less than 100 characters")
    private String shippingState;

    @NotBlank(message = "Postal code is required")
    @Size(max = 20, message = "Postal code must be less than 20 characters")
    @Pattern(regexp = "^[A-Za-z0-9\\s\\-]{3,20}$", message = "Invalid postal code format")
    private String shippingPostalCode;

    @NotBlank(message = "Country is required")
    @Size(max = 100, message = "Country must be less than 100 characters")
    private String shippingCountry = "Canada";

    // Payment Information
    @NotNull(message = "Payment method is required")
    private PaymentMethod paymentMethod;

    // Order Items (optional - if empty, use cart items)
    private List<OrderItemRequest> orderItems;

    // Special requests
    @Size(max = 1000, message = "Notes must be less than 1000 characters")
    private String notes;

    // Pricing overrides (for admin use)
    @DecimalMin(value = "0.0", inclusive = true, message = "Tax amount must be non-negative")
    private BigDecimal taxAmount;

    @DecimalMin(value = "0.0", inclusive = true, message = "Shipping amount must be non-negative")
    private BigDecimal shippingAmount;

    @DecimalMin(value = "0.0", inclusive = true, message = "Discount amount must be non-negative")
    private BigDecimal discountAmount;

    // Force order creation even with cart validation issues (admin only)
    private boolean forceCreate = false;

    // Default constructor
    public OrderCreateRequest() {
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

    public String getShippingEmail() {
        return shippingEmail;
    }

    public void setShippingEmail(String shippingEmail) {
        this.shippingEmail = shippingEmail;
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

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(PaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public List<OrderItemRequest> getOrderItems() {
        return orderItems;
    }

    public void setOrderItems(List<OrderItemRequest> orderItems) {
        this.orderItems = orderItems;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public BigDecimal getTaxAmount() {
        return taxAmount;
    }

    public void setTaxAmount(BigDecimal taxAmount) {
        this.taxAmount = taxAmount;
    }

    public BigDecimal getShippingAmount() {
        return shippingAmount;
    }

    public void setShippingAmount(BigDecimal shippingAmount) {
        this.shippingAmount = shippingAmount;
    }

    public BigDecimal getDiscountAmount() {
        return discountAmount;
    }

    public void setDiscountAmount(BigDecimal discountAmount) {
        this.discountAmount = discountAmount;
    }

    public boolean isForceCreate() {
        return forceCreate;
    }

    public void setForceCreate(boolean forceCreate) {
        this.forceCreate = forceCreate;
    }

    // Utility methods
    public boolean hasCustomOrderItems() {
        return orderItems != null && !orderItems.isEmpty();
    }

    public String getShippingFullName() {
        return shippingFirstName + " " + shippingLastName;
    }

    public String getShippingFullAddress() {
        return String.format("%s, %s, %s %s, %s",
                shippingAddress, shippingCity, shippingState, shippingPostalCode, shippingCountry);
    }

    public boolean hasCustomPricing() {
        return taxAmount != null || shippingAmount != null || discountAmount != null;
    }

    @Override
    public String toString() {
        return "OrderCreateRequest{" +
                "shippingFullName='" + getShippingFullName() + '\'' +
                ", shippingCity='" + shippingCity + '\'' +
                ", paymentMethod=" + paymentMethod +
                ", hasCustomItems=" + hasCustomOrderItems() +
                ", forceCreate=" + forceCreate +
                '}';
    }

    /**
     * Inner class for order item request
     */
    public static class OrderItemRequest {
        @NotNull(message = "Product ID is required")
        private Long productId;

        @NotNull(message = "Quantity is required")
        @Min(value = 1, message = "Quantity must be at least 1")
        @Max(value = 999, message = "Quantity must be less than 1000")
        private Integer quantity;

        @DecimalMin(value = "0.01", message = "Unit price must be greater than 0")
        private BigDecimal unitPrice; // Optional: for price override

        public OrderItemRequest() {
        }

        public OrderItemRequest(Long productId, Integer quantity) {
            this.productId = productId;
            this.quantity = quantity;
        }

        public Long getProductId() {
            return productId;
        }

        public void setProductId(Long productId) {
            this.productId = productId;
        }

        public Integer getQuantity() {
            return quantity;
        }

        public void setQuantity(Integer quantity) {
            this.quantity = quantity;
        }

        public BigDecimal getUnitPrice() {
            return unitPrice;
        }

        public void setUnitPrice(BigDecimal unitPrice) {
            this.unitPrice = unitPrice;
        }

        public boolean hasCustomPrice() {
            return unitPrice != null;
        }

        @Override
        public String toString() {
            return "OrderItemRequest{" +
                    "productId=" + productId +
                    ", quantity=" + quantity +
                    ", unitPrice=" + unitPrice +
                    '}';
        }
    }
}
