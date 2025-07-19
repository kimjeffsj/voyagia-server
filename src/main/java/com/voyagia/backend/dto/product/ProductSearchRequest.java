package com.voyagia.backend.dto.product;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

/**
 * Product advanced search request DTO
 */
public class ProductSearchRequest {

    @Size(max = 200, message = "Search keyword must be 200 characters or less")
    private String keyword;

    private Long categoryId;

    @DecimalMin(value = "0.0", message = "Minimum price must be 0 or greater")
    private BigDecimal minPrice;

    @DecimalMin(value = "0.0", message = "Maximum price must be 0 or greater")
    private BigDecimal maxPrice;

    private Boolean isActive;
    private Boolean isFeatured;
    private Boolean inStock;
    private Boolean lowStock;

    // Sorting
    @Pattern(regexp = "^(name|price|createdAt|stockQuantity|category)$", message = "Sort criteria must be one of: name, price, createdAt, stockQuantity, category")
    private String sortBy = "createdAt";

    private Boolean descending = true;

    // Paging
    @Min(value = 0, message = "Page number must be 0 or greater")
    private Integer page = 0;

    @Min(value = 1, message = "Page size must be 1 or greater")
    @Max(value = 100, message = "Page size must be 100 or less")
    private Integer size = 20;

    public ProductSearchRequest() {
    }

    // Getters/Setters
    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public BigDecimal getMinPrice() {
        return minPrice;
    }

    public void setMinPrice(BigDecimal minPrice) {
        this.minPrice = minPrice;
    }

    public BigDecimal getMaxPrice() {
        return maxPrice;
    }

    public void setMaxPrice(BigDecimal maxPrice) {
        this.maxPrice = maxPrice;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public Boolean getIsFeatured() {
        return isFeatured;
    }

    public void setIsFeatured(Boolean isFeatured) {
        this.isFeatured = isFeatured;
    }

    public Boolean getInStock() {
        return inStock;
    }

    public void setInStock(Boolean inStock) {
        this.inStock = inStock;
    }

    public Boolean getLowStock() {
        return lowStock;
    }

    public void setLowStock(Boolean lowStock) {
        this.lowStock = lowStock;
    }

    public String getSortBy() {
        return sortBy;
    }

    public void setSortBy(String sortBy) {
        this.sortBy = sortBy;
    }

    public Boolean getDescending() {
        return descending;
    }

    public void setDescending(Boolean descending) {
        this.descending = descending;
    }

    public Boolean isDescending() {
        return descending != null && descending;
    }

    public Integer getPage() {
        return page;
    }

    public void setPage(Integer page) {
        this.page = page;
    }

    public Integer getSize() {
        return size;
    }

    public void setSize(Integer size) {
        this.size = size;
    }

    // Utility methods
    public boolean hasKeyword() {
        return keyword != null && !keyword.trim().isEmpty();
    }

    public boolean hasCategoryFilter() {
        return categoryId != null;
    }

    public boolean hasPriceFilter() {
        return minPrice != null || maxPrice != null;
    }

    public boolean hasStatusFilter() {
        return isActive != null || isFeatured != null || inStock != null || lowStock != null;
    }

    public boolean hasAnyFilter() {
        return hasKeyword() || hasCategoryFilter() || hasPriceFilter() || hasStatusFilter();
    }

    public boolean isPriceRangeValid() {
        if (minPrice != null && maxPrice != null) {
            return minPrice.compareTo(maxPrice) <= 0;
        }
        return true;
    }

    @Override
    public String toString() {
        return "ProductSearchRequest{" +
                "keyword='" + keyword + '\'' +
                ", categoryId=" + categoryId +
                ", minPrice=" + minPrice +
                ", maxPrice=" + maxPrice +
                ", isActive=" + isActive +
                ", isFeatured=" + isFeatured +
                ", sortBy='" + sortBy + '\'' +
                ", descending=" + descending +
                ", page=" + page +
                ", size=" + size +
                '}';
    }
}
