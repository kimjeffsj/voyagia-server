package com.voyagia.backend.dto.category;

import java.util.List;

/**
 * Category tree response DTO for hierarchical category structure
 */
public class CategoryTreeResponse {

    private Long id;
    private String name;
    private String slug;
    private String imageUrl;
    private Boolean isActive;
    private Integer sortOrder;
    private Integer depth;
    private Long productCount;

    // Recursive children for tree structure
    private List<CategoryTreeResponse> children;

    public CategoryTreeResponse() {
    }

    public CategoryTreeResponse(Long id, String name, String slug) {
        this.id = id;
        this.name = name;
        this.slug = slug;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }

    public Integer getDepth() {
        return depth;
    }

    public void setDepth(Integer depth) {
        this.depth = depth;
    }

    public Long getProductCount() {
        return productCount;
    }

    public void setProductCount(Long productCount) {
        this.productCount = productCount;
    }

    public List<CategoryTreeResponse> getChildren() {
        return children;
    }

    public void setChildren(List<CategoryTreeResponse> children) {
        this.children = children;
    }

    // Utility methods
    public boolean hasChildren() {
        return children != null && !children.isEmpty();
    }

    public boolean isRootCategory() {
        return depth != null && depth == 0;
    }

    public boolean hasProducts() {
        return productCount != null && productCount > 0;
    }

    public boolean hasImage() {
        return imageUrl != null && !imageUrl.trim().isEmpty();
    }

    @Override
    public String toString() {
        return "CategoryTreeResponse{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", slug='" + slug + '\'' +
                ", depth=" + depth +
                ", productCount=" + productCount +
                ", childrenCount=" + (children != null ? children.size() : 0) +
                '}';
    }
}