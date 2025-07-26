package com.voyagia.backend.dto.cart;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * Cart Item Request DTO
 */
public class CartItemRequest {

    @NotNull(message = "Product ID is required")
    private Long productId;

    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    private Integer quantity;

    private String action;

    // Constructors
    public CartItemRequest() {
    }

    public CartItemRequest(Long productId, Integer quantity) {
        this.productId = productId;
        this.quantity = quantity;
    }

    public CartItemRequest(Long productId, Integer quantity, String action) {
        this.productId = productId;
        this.quantity = quantity;
        this.action = action;
    }

    // Validation methods
    public boolean isValidQuantity() {
        return quantity != null && quantity > 0;
    }

    public boolean isAddAction() {
        return "add".equalsIgnoreCase(action);
    }

    public boolean isUpdate() {
        return "update".equalsIgnoreCase(action);
    }

    public boolean isRemoveAction() {
        return "remove".equalsIgnoreCase(action);
    }

    // Getters/Setters
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

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    @Override
    public String toString() {
        return "CartItemRequest{" +
                "productId=" + productId +
                ", quantity=" + quantity +
                ", action='" + action + '\'' +
                '}';
    }
}
