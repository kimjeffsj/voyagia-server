package com.voyagia.backend.dto.cart;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

/**
 * Cart Update Request DTO
 */
public class CartUpdateRequest {
    @NotEmpty(message = "Items list cannot be empty")
    @Valid
    private List<CartItemRequest> items;

    private String operation; // "bulk_update", "clear", "sync"

    // Constructors
    public CartUpdateRequest() {
    }

    public CartUpdateRequest(List<CartItemRequest> items) {
        this.items = items;
    }

    public CartUpdateRequest(List<CartItemRequest> items, String operation) {
        this.items = items;
        this.operation = operation;
    }

    // Validation methods
    public boolean hasItems() {
        return items != null && !items.isEmpty();
    }

    public boolean isBulkUpdate() {
        return "bulk_update".equalsIgnoreCase(operation);
    }

    public boolean isClear() {
        return "clear".equalsIgnoreCase(operation);
    }

    public boolean isSync() {
        return "sync".equalsIgnoreCase(operation);
    }

    /**
     * Check if all items have valid quantities
     */
    public boolean allItemsValid() {
        if (!hasItems()) return false;

        return items.stream().allMatch(item ->
                item.getProductId() != null &&
                        item.getQuantity() != null &&
                        item.getQuantity() > 0);
    }

    /**
     * Get total number of items in request
     */
    public int getItemCount() {
        return hasItems() ? items.size() : 0;
    }

    /**
     * Get total quantity across all items
     */
    public int getTotalQuantity() {
        if (!hasItems()) return 0;

        return items.stream()
                .mapToInt(CartItemRequest::getQuantity)
                .sum();
    }

    // Getters and Setters
    public List<CartItemRequest> getItems() {
        return items;
    }

    public void setItems(List<CartItemRequest> items) {
        this.items = items;
    }

    public String getOperation() {
        return operation;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }

    @Override
    public String toString() {
        return "CartUpdateRequest{" +
                "items=" + (items != null ? items.size() : 0) + " items" +
                ", operation='" + operation + '\'' +
                '}';
    }
}
