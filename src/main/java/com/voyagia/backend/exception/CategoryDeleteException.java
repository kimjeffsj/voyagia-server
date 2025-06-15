package com.voyagia.backend.exception;


/**
 * Category Delete Exception
 * <p>
 * - Category has product
 * - Category has sub-category
 * - Category marked as important
 * - No permission
 * <p>
 * TODO: Will be mapped as HTTP 409 Conflict, in @ControllerAdvice
 */
public class CategoryDeleteException extends RuntimeException {
    private final Long categoryId;
    private final String categoryName;
    private final String reason;
    private final Integer productCount; // number of products
    private final Integer childrenCount; // number of sub categories

    public CategoryDeleteException(Long categoryId, String categoryName, String reason) {
        super(String.format("Cannot delete category '%s' (ID: %d): %s",
                categoryName, categoryId, reason));
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.reason = reason;
        this.productCount = null;
        this.childrenCount = null;
    }

    public CategoryDeleteException(Long categoryId, String categoryName, Integer productCount, Integer childrenCount) {
        super(String.format("Cannot delete category '%s' (ID: %d): " +
                        "Category has %d product(s) and %d child categor%s",
                categoryName, categoryId, productCount, childrenCount, childrenCount == 1 ? "y" : "ies"));
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.reason = "Has associated products or child categories";
        this.productCount = productCount;
        this.childrenCount = childrenCount;
    }

    public CategoryDeleteException(String message) {
        super(message);
        this.categoryId = null;
        this.categoryName = null;
        this.reason = null;
        this.productCount = null;
        this.childrenCount = null;
    }

    public CategoryDeleteException(String message, Throwable cause) {
        super(message, cause);
        this.categoryId = null;
        this.categoryName = null;
        this.reason = null;
        this.productCount = null;
        this.childrenCount = null;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public String getReason() {
        return reason;
    }

    public Integer getProductCount() {
        return productCount;
    }

    public Integer getChildrenCount() {
        return childrenCount;
    }
}
