package com.voyagia.backend.dto.cart;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Cart Response DTO
 */
public class CartResponse {

    private Long userId;
    private String userEmail;
    private String userFullName;
    private List<CartItemResponse> items;
    private Integer totalItems;
    private Integer totalQuantity;
    private BigDecimal subtotal;
    private BigDecimal tax;
    private BigDecimal taxRate;
    private BigDecimal total;
    private Boolean isEmpty;
    private LocalDateTime lastUpdated;

    // Cart summary
    public static class CartSummary {
        private Integer itemCount;
        private Integer totalQuantity;
        private BigDecimal totalAmount;

        public CartSummary() {
        }

        public CartSummary(Integer itemCount, Integer totalQuantity, BigDecimal totalAmount) {
            this.itemCount = itemCount;
            this.totalQuantity = totalQuantity;
            this.totalAmount = totalAmount;
        }

        public Integer getItemCount() {
            return itemCount;
        }

        public void setItemCount(Integer itemCount) {
            this.itemCount = itemCount;
        }

        public Integer getTotalQuantity() {
            return totalQuantity;
        }

        public void setTotalQuantity(Integer totalQuantity) {
            this.totalQuantity = totalQuantity;
        }

        public BigDecimal getTotalAmount() {
            return totalAmount;
        }

        public void setTotalAmount(BigDecimal totalAmount) {
            this.totalAmount = totalAmount;
        }
    }

    // Constructor
    public CartResponse() {
    }

    public CartResponse(Long userId, List<CartItemResponse> items) {
        this.userId = userId;
        this.items = items;
        calculateTotals();
    }

    /**
     * Calculate cart totals based on items
     */
    public void calculateTotals() {
        if (items == null || items.isEmpty()) {
            this.totalItems = 0;
            this.totalQuantity = 0;
            this.subtotal = BigDecimal.ZERO;
            this.tax = BigDecimal.ZERO;
            this.total = BigDecimal.ZERO;
            this.isEmpty = true;
            return;
        }

        this.totalItems = items.size();
        this.totalQuantity = items.stream()
                .mapToInt(CartItemResponse::getQuantity)
                .sum();

        this.subtotal = items.stream()
                .map(CartItemResponse::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        //Calculate tax(default 10%)
        this.taxRate = new BigDecimal("0.10");
        this.tax = subtotal.multiply(taxRate);
        this.total = subtotal.add(tax);
        this.isEmpty = false;

        // Set last updated to the most recent item update
        this.lastUpdated = items.stream()
                .map(CartItemResponse::getUpdatedAt)
                .max(LocalDateTime::compareTo)
                .orElse(LocalDateTime.now());
    }

    /**
     * Set custom tax rate and recalculate
     */
    public void setTaxRate(BigDecimal taxRate) {
        this.taxRate = taxRate;
        if (subtotal != null) {
            this.tax = subtotal.multiply(taxRate);
            this.total = subtotal.add(tax);
        }
    }

    /**
     * Check if cart has any items
     */
    public boolean hasItems() {
        return items != null && !items.isEmpty();
    }

    /**
     * Get Cart Summary
     */
    public CartSummary getSummary() {
        return new CartSummary(totalItems, totalQuantity, total);
    }

    // Getters/Setters
    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public String getUserFullName() {
        return userFullName;
    }

    public void setUserFullName(String userFullName) {
        this.userFullName = userFullName;
    }

    public List<CartItemResponse> getItems() {
        return items;
    }

    public void setItems(List<CartItemResponse> items) {
        this.items = items;
        calculateTotals();
    }

    public Integer getTotalItems() {
        return totalItems;
    }

    public void setTotalItems(Integer totalItems) {
        this.totalItems = totalItems;
    }

    public Integer getTotalQuantity() {
        return totalQuantity;
    }

    public void setTotalQuantity(Integer totalQuantity) {
        this.totalQuantity = totalQuantity;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal;
    }

    public BigDecimal getTax() {
        return tax;
    }

    public void setTax(BigDecimal tax) {
        this.tax = tax;
    }

    public BigDecimal getTaxRate() {
        return taxRate;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public Boolean getIsEmpty() {
        return isEmpty;
    }

    public void setIsEmpty(Boolean isEmpty) {
        this.isEmpty = isEmpty;
    }

    public LocalDateTime getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(LocalDateTime lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    @Override
    public String toString() {
        return "CartResponse{" +
                "userId=" + userId +
                ", userEmail='" + userEmail + '\'' +
                ", totalItems=" + totalItems +
                ", totalQuantity=" + totalQuantity +
                ", subtotal=" + subtotal +
                ", tax=" + tax +
                ", total=" + total +
                ", isEmpty=" + isEmpty +
                ", lastUpdated=" + lastUpdated +
                '}';
    }
}
