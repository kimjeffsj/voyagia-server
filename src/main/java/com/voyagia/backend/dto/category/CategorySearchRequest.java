package com.voyagia.backend.dto.category;

import jakarta.validation.constraints.*;

/**
 * Category search request DTO
 */
public class CategorySearchRequest {

    private String keyword;

    private Long parentId;

    private Boolean isActive = true;

    private Integer depth;

    @Min(value = 0, message = "Page must be 0 or greater")
    private int page = 0;

    @Min(value = 1, message = "Size must be 1 or greater")
    @Max(value = 100, message = "Size must be 100 or less")
    private int size = 20;

    @Pattern(regexp = "^(name|slug|createdAt|updatedAt|sortOrder)$", 
             message = "Sort field must be one of: name, slug, createdAt, updatedAt, sortOrder")
    private String sortBy = "sortOrder";

    private boolean descending = false;

    public CategorySearchRequest() {
    }

    // Getters and Setters
    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public Integer getDepth() {
        return depth;
    }

    public void setDepth(Integer depth) {
        this.depth = depth;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public String getSortBy() {
        return sortBy;
    }

    public void setSortBy(String sortBy) {
        this.sortBy = sortBy;
    }

    public boolean isDescending() {
        return descending;
    }

    public void setDescending(boolean descending) {
        this.descending = descending;
    }

    // Utility methods
    public boolean hasKeyword() {
        return keyword != null && !keyword.trim().isEmpty();
    }

    public boolean hasParentFilter() {
        return parentId != null;
    }

    public boolean hasDepthFilter() {
        return depth != null && depth >= 0;
    }

    @Override
    public String toString() {
        return "CategorySearchRequest{" +
                "keyword='" + keyword + '\'' +
                ", parentId=" + parentId +
                ", isActive=" + isActive +
                ", depth=" + depth +
                ", page=" + page +
                ", size=" + size +
                ", sortBy='" + sortBy + '\'' +
                ", descending=" + descending +
                '}';
    }
}