package com.voyagia.backend.exception;

/**
 * Circular reference exception
 * <p>
 * - When trying to set a category as its own subcategory
 * - When trying to set a category as a child of its descendant category
 * - When creating circular structures like A -> B -> C -> A
 * <p>
 * Examples:
 * - Electronics -> Smartphones -> Electronics (circular reference)
 * - Setting Category A's parent to Category A itself (self-reference)
 * <p>
 * Will be mapped to HTTP 400 Bad Request by @ControllerAdvice in the future
 */
public class CircularReferenceException extends RuntimeException {

    private final Long categoryId;
    private final Long parentId;
    private final String categoryName;
    private final String parentName;

    public CircularReferenceException(Long categoryId, Long parentId) {
        super(String.format("Circular reference detected: Category %d cannot be a child of Category %d",
                categoryId, parentId));
        this.categoryId = categoryId;
        this.parentId = parentId;
        this.categoryName = null;
        this.parentName = null;
    }

    public CircularReferenceException(Long categoryId, Long parentId,
            String categoryName, String parentName) {
        super(String.format(
                "Circular reference detected: Category '%s' (ID: %d) cannot be a child of Category '%s' (ID: %d)",
                categoryName, categoryId, parentName, parentId));
        this.categoryId = categoryId;
        this.parentId = parentId;
        this.categoryName = categoryName;
        this.parentName = parentName;
    }

    public CircularReferenceException(String message) {
        super(message);
        this.categoryId = null;
        this.parentId = null;
        this.categoryName = null;
        this.parentName = null;
    }

    public CircularReferenceException(String message, Throwable cause) {
        super(message, cause);
        this.categoryId = null;
        this.parentId = null;
        this.categoryName = null;
        this.parentName = null;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public Long getParentId() {
        return parentId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public String getParentName() {
        return parentName;
    }
}
