package com.voyagia.backend.dto.product;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * Create Product request DTO
 */
public class ProductCreateRequest {

    @NotBlank(message = "Product name is required")
    @Size(min = 2, max = 200, message = "Product name must be between 2 and 200")
    private String name;

    @Size(max = 5000, message = "Product description must be under 5000 characters")
    private String description;

    @Size(max = 500, message = "Short description must be under 500 characters")
    private String shortDescription;

    @NotBlank(message = "SKU is required")
    @Pattern(regexp = "^[A-Z0-9-]+$", message = "SKU must contain only uppercase letters, numbers, and hyphens")
    private String sku;

    @NotBlank(message = "Product slug is required")
    @Pattern(regexp = "^[a-z0-9-]+$", message = "Slug must contain only lowercase letters, numbers, and hyphens")
    private String slug;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.01", message = "Price must be over 0.01")
    @Digits(integer = 8, fraction = 2, message = "Price must have at most 8 integer digits and 2 decimal places")
    private BigDecimal price;

    @DecimalMin(value = "0.01", message = "Compare price must be over 0.01")
    @Digits(integer = 8, fraction = 2, message = "Compare price must have at most 8 integer digits and 2 decimal places")
    private BigDecimal comparePrice;

    @NotNull(message = "Stock quantity is required")
    @Min(value = 0, message = "Stock quantity must be 0 or greater")
    private Integer stockQuantity;

    @Min(value = 0, message = "Low stock threshold must be 0 or greater")
    private Integer lowStockThreshold = 10;

    @DecimalMin(value = "0.0", message = "Weight must be 0 or greater")
    private BigDecimal weight;

    @Size(max = 100, message = "Dimensions must be 100 characters or less")
    private String dimensions;

    @NotNull(message = "Category is required")
    private Long categoryId;

    private String mainImageUrl;

    private List<String> imageUrls;

    private List<String> tags;

    private Boolean isFeatured = false;

    public ProductCreateRequest() {
    }

    // Getters and Setters
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

    public Boolean getIsFeatured() {
        return isFeatured;
    }

    public void setIsFeatured(Boolean isFeatured) {
        this.isFeatured = isFeatured;
    }

    // Utility methods
    public boolean hasComparePrice() {
        return comparePrice != null && comparePrice.compareTo(BigDecimal.ZERO) > 0;
    }

    public boolean hasImages() {
        return imageUrls != null && !imageUrls.isEmpty();
    }

    public boolean hasTags() {
        return tags != null && !tags.isEmpty();
    }

    @Override
    public String toString() {
        return "ProductCreateRequest{" +
                "name='" + name + '\'' +
                ", sku='" + sku + '\'' +
                ", slug='" + slug + '\'' +
                ", price=" + price +
                ", stockQuantity=" + stockQuantity +
                ", categoryId=" + categoryId +
                '}';
    }
}
