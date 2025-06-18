package com.voyagia.backend.dto.user;

import com.voyagia.backend.entity.UserRole;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

/**
 * User search request DTO
 */
public class UserSearchRequest {

    @Size(max = 100, message = "Search keyword cannot exceet 100 characters")
    private String keyword;

    private UserRole role;

    private Boolean isActive;

    @Min(value = 0, message = "Page number must be non-negative")
    private Integer page = 0;

    @Min(value = 1, message = "Page size must be at least 1")
    private Integer size = 20;

    private String sortBy = "createdAt";

    private String sortDirection = "desc";

    public UserSearchRequest() {
    }

    // Search with keyword
    public UserSearchRequest(String keyword) {
        this.keyword = keyword;
    }

    // Search with keyword and pagination
    public UserSearchRequest(String keyword, Integer page, Integer size) {
        this.keyword = keyword;
        this.page = page;
        this.size = size;
    }

    // Getters/Setter

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public UserRole getRole() {
        return role;
    }

    public void setRole(UserRole role) {
        this.role = role;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
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

    // Validation method
    public boolean hasKeyword() {
        return keyword != null && !keyword.trim().isEmpty();
    }

    public boolean hasRoleFilter() {
        return role != null;
    }

    public boolean hasActiveFilter() {
        return isActive != null;
    }

    public boolean isDescending() {
        return "desc".equalsIgnoreCase(sortDirection);
    }

    @Override
    public String toString() {
        return "UserSearchRequest{" +
                "keyword='" + keyword + '\'' +
                ", role=" + role +
                ", isActive=" + isActive +
                ", page=" + page +
                ", size=" + size +
                ", sortBy='" + sortBy + '\'' +
                ", sortDirection='" + sortDirection + '\'' +
                '}';
    }
}
