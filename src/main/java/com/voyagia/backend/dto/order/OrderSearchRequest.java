package com.voyagia.backend.dto.order;

import com.voyagia.backend.entity.OrderStatus;
import com.voyagia.backend.entity.PaymentStatus;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO for order search criteria
 */
public class OrderSearchRequest {

    // Basic search
    private String searchTerm; // Order number, customer email, name

    // User filter
    private Long userId;
    private String userEmail;

    // Status filters
    private List<OrderStatus> statuses;
    private List<PaymentStatus> paymentStatuses;

    // Date range filters
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime createdFrom;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime createdTo;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime paidFrom;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime paidTo;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime shippedFrom;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime shippedTo;

    // Amount filters
    private BigDecimal minAmount;
    private BigDecimal maxAmount;

    // Location filters
    private String shippingCity;
    private String shippingState;
    private String shippingCountry;

    // Product filters
    private Long productId;
    private String productSku;

    // Pagination and sorting
    private int page = 0;
    private int size = 20;
    private String sortBy = "createdAt";
    private String sortDirection = "DESC";

    // Include related data
    private boolean includeOrderItems = false;

    // Default constructor
    public OrderSearchRequest() {
    }

    // Getters and Setters
    public String getSearchTerm() {
        return searchTerm;
    }

    public void setSearchTerm(String searchTerm) {
        this.searchTerm = searchTerm;
    }

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

    public List<OrderStatus> getStatuses() {
        return statuses;
    }

    public void setStatuses(List<OrderStatus> statuses) {
        this.statuses = statuses;
    }

    public List<PaymentStatus> getPaymentStatuses() {
        return paymentStatuses;
    }

    public void setPaymentStatuses(List<PaymentStatus> paymentStatuses) {
        this.paymentStatuses = paymentStatuses;
    }

    public LocalDateTime getCreatedFrom() {
        return createdFrom;
    }

    public void setCreatedFrom(LocalDateTime createdFrom) {
        this.createdFrom = createdFrom;
    }

    public LocalDateTime getCreatedTo() {
        return createdTo;
    }

    public void setCreatedTo(LocalDateTime createdTo) {
        this.createdTo = createdTo;
    }

    public LocalDateTime getPaidFrom() {
        return paidFrom;
    }

    public void setPaidFrom(LocalDateTime paidFrom) {
        this.paidFrom = paidFrom;
    }

    public LocalDateTime getPaidTo() {
        return paidTo;
    }

    public void setPaidTo(LocalDateTime paidTo) {
        this.paidTo = paidTo;
    }

    public LocalDateTime getShippedFrom() {
        return shippedFrom;
    }

    public void setShippedFrom(LocalDateTime shippedFrom) {
        this.shippedFrom = shippedFrom;
    }

    public LocalDateTime getShippedTo() {
        return shippedTo;
    }

    public void setShippedTo(LocalDateTime shippedTo) {
        this.shippedTo = shippedTo;
    }

    public BigDecimal getMinAmount() {
        return minAmount;
    }

    public void setMinAmount(BigDecimal minAmount) {
        this.minAmount = minAmount;
    }

    public BigDecimal getMaxAmount() {
        return maxAmount;
    }

    public void setMaxAmount(BigDecimal maxAmount) {
        this.maxAmount = maxAmount;
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

    public String getShippingCountry() {
        return shippingCountry;
    }

    public void setShippingCountry(String shippingCountry) {
        this.shippingCountry = shippingCountry;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public String getProductSku() {
        return productSku;
    }

    public void setProductSku(String productSku) {
        this.productSku = productSku;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = Math.max(0, page);
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = Math.min(Math.max(1, size), 100); // Limit between 1 and 100
    }

    public String getSortBy() {
        return sortBy;
    }

    public void setSortBy(String sortBy) {
        this.sortBy = sortBy;
    }

    public String getSortDirection() {
        return sortDirection;
    }

    public void setSortDirection(String sortDirection) {
        this.sortDirection = sortDirection;
    }

    public boolean isIncludeOrderItems() {
        return includeOrderItems;
    }

    public void setIncludeOrderItems(boolean includeOrderItems) {
        this.includeOrderItems = includeOrderItems;
    }

    // Utility methods
    public boolean hasSearchTerm() {
        return searchTerm != null && !searchTerm.trim().isEmpty();
    }

    public boolean hasUserFilter() {
        return userId != null || (userEmail != null && !userEmail.trim().isEmpty());
    }

    public boolean hasStatusFilter() {
        return statuses != null && !statuses.isEmpty();
    }

    public boolean hasPaymentStatusFilter() {
        return paymentStatuses != null && !paymentStatuses.isEmpty();
    }

    public boolean hasDateFilter() {
        return createdFrom != null || createdTo != null ||
                paidFrom != null || paidTo != null ||
                shippedFrom != null || shippedTo != null;
    }

    public boolean hasAmountFilter() {
        return minAmount != null || maxAmount != null;
    }

    public boolean hasLocationFilter() {
        return (shippingCity != null && !shippingCity.trim().isEmpty()) ||
                (shippingState != null && !shippingState.trim().isEmpty()) ||
                (shippingCountry != null && !shippingCountry.trim().isEmpty());
    }

    public boolean hasProductFilter() {
        return productId != null || (productSku != null && !productSku.trim().isEmpty());
    }

    public boolean hasAnyFilter() {
        return hasSearchTerm() || hasUserFilter() || hasStatusFilter() ||
                hasPaymentStatusFilter() || hasDateFilter() || hasAmountFilter() ||
                hasLocationFilter() || hasProductFilter();
    }

    public boolean isDescending() {
        return "DESC".equalsIgnoreCase(sortDirection);
    }

    @Override
    public String toString() {
        return "OrderSearchRequest{" +
                "searchTerm='" + searchTerm + '\'' +
                ", userId=" + userId +
                ", hasStatusFilter=" + hasStatusFilter() +
                ", hasDateFilter=" + hasDateFilter() +
                ", hasAmountFilter=" + hasAmountFilter() +
                ", page=" + page +
                ", size=" + size +
                ", sortBy='" + sortBy + '\'' +
                ", sortDirection='" + sortDirection + '\'' +
                '}';
    }
}