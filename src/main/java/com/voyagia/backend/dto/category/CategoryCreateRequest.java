package com.voyagia.backend.dto.category;

import jakarta.validation.constraints.*;

/**
 * Create Category request DTO
 */
public class CategoryCreateRequest {

    @NotBlank(message = "Category name is required")
    @Size(min = 2, max = 100, message = "Category name must be between 2 and 100 characters")
    private String name;

    @Size(max = 500, message = "Description must be 500 characters or less")
    private String description;

    @NotBlank(message = "Category slug is required")
    @Pattern(regexp = "^[a-z0-9-]+$", message = "Slug must contain only lowercase letters, numbers, and hyphens")
    @Size(min = 2, max = 100, message = "Slug must be between 2 and 100 characters")
    private String slug;

    @Size(max = 255, message = "Image URL must be 255 characters or less")
    private String imageUrl;

    private Long parentId;

    @Min(value = 0, message = "Sort order must be 0 or greater")
    private Integer sortOrder = 0;

    private Boolean isActive = true;

    public CategoryCreateRequest() {
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

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    // Utility methods
    public boolean hasParent() {
        return parentId != null;
    }

    public boolean hasImage() {
        return imageUrl != null && !imageUrl.trim().isEmpty();
    }

    @Override
    public String toString() {
        return "CategoryCreateRequest{" +
                "name='" + name + '\'' +
                ", slug='" + slug + '\'' +
                ", parentId=" + parentId +
                ", sortOrder=" + sortOrder +
                ", isActive=" + isActive +
                '}';
    }
}