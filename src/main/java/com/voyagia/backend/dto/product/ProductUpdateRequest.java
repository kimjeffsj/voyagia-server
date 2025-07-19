package com.voyagia.backend.dto.product;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * Product update request DTO
 */
public class ProductUpdateRequest {
    @Size(min = 2, max = 200, message = "Product name must be between 2 and 200 characters")
    private String name;

    @Size(max = 5000, message = "Product description must be under 5000 characters")
    private String description;

    @Size(max = 500, message = "Short description must be under 500 characters")
    private String shortDescription;

    @Pattern(regexp = "^[A-Z0-9-]+$", message = "SKU must contain only uppercase letters, numbers, and hyphens")
    private String sku;

    @Pattern(regexp = "^[a-z0-9-]+$", message = "Slug must contain only lowercase letters, numbers, and hyphens")
    private String slug;

    @DecimalMin(value = "0.01", message = "Price must be over 0.01")
    @Digits(integer = 8, fraction = 2, message = "Price must have at most 8 integer digits and 2 decimal places")
    private BigDecimal price;

    @DecimalMin(value = "0.01", message = "Compare price must be over 0.01")
    @Digits(integer = 8, fraction = 2, message = "Compare price must have at most 8 integer digits and 2 decimal places")
    private BigDecimal comparePrice;

    @Min(value = 0, message = "Stock quantity must be 0 or greater")
    private Integer stockQuantity;

    @Min(value = 0, message = "Low stock threshold must be 0 or greater")
    private Integer lowStockThreshold;

    @DecimalMin(value = "0.0", message = "Weight must be 0 or greater")
    private BigDecimal weight;

    @Size(max = 100, message = "Dimensions must be 100 characters or less")
    private String dimensions;

    private Long categoryId;

    private String mainImageUrl;

    private List<String> imageUrls;

    private List<String> tags;

    private Boolean isActive;

    private Boolean isFeatured;

    public ProductUpdateRequest() {
    }

    // Getters/Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getShortDescription() {
        return shortDescription;
    }

    public void setShortDescription(String shortDescription) {
        this.shortDescription = shortDescription;
    }

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public BigDecimal getComparePrice() {
        return comparePrice;
    }

    public void setComparePrice(BigDecimal comparePrice) {
        this.comparePrice = comparePrice;
    }

    public Integer getStockQuantity() {
        return stockQuantity;
    }

    public void setStockQuantity(Integer stockQuantity) {
        this.stockQuantity = stockQuantity;
    }

    public Integer getLowStockThreshold() {
        return lowStockThreshold;
    }

    public void setLowStockThreshold(Integer lowStockThreshold) {
        this.lowStockThreshold = lowStockThreshold;
    }

    public BigDecimal getWeight() {
        return weight;
    }

    public void setWeight(BigDecimal weight) {
        this.weight = weight;
    }

    public String getDimensions() {
        return dimensions;
    }

    public void setDimensions(String dimensions) {
        this.dimensions = dimensions;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public String getMainImageUrl() {
        return mainImageUrl;
    }

    public void setMainImageUrl(String mainImageUrl) {
        this.mainImageUrl = mainImageUrl;
    }

    public List<String> getImageUrls() {
        return imageUrls;
    }

    public void setImageUrls(List<String> imageUrls) {
        this.imageUrls = imageUrls;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
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

    // Utility Method
    public boolean hasAnyUpdateField() {
        return name != null || description != null || shortDescription != null ||
                sku != null || slug != null || price != null || comparePrice != null ||
                stockQuantity != null || lowStockThreshold != null || weight != null ||
                dimensions != null || categoryId != null || mainImageUrl != null ||
                imageUrls != null || tags != null || isActive != null || isFeatured != null;
    }

    // Check if there is any updates
    public boolean hasBasicInfoUpdate() {
        return name != null || description != null || shortDescription != null;
    }

    public boolean hasPriceUpdate() {
        return price != null || comparePrice != null;
    }

    public boolean hasInventoryUpdate() {
        return stockQuantity != null || lowStockThreshold != null;
    }

    public boolean hasStatusUpdate() {
        return isActive != null || isFeatured != null;
    }

    public boolean hasMediaUpdate() {
        return mainImageUrl != null || imageUrls != null;
    }

    @Override
    public String toString() {
        return "ProductUpdateRequest{" +
                "name='" + name + '\'' +
                ", sku='" + sku + '\'' +
                ", slug='" + slug + '\'' +
                ", price=" + price +
                ", stockQuantity=" + stockQuantity +
                ", isActive=" + isActive +
                '}';
    }
}
