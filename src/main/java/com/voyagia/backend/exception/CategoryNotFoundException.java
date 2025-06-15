package com.voyagia.backend.exception;

/**
 * Category not found exception
 * <p>
 * - Find by ID
 * - Find by Slug
 * - Find by name
 * <p>
 * TODO: will be mapped with HTTP 404, in @ControllerAdvice
 */
public class CategoryNotFoundException extends RuntimeException {

    private final Long categoryId;
    private final String identifier;
    private final String field;

    public CategoryNotFoundException(Long categoryId) {
        super("Category not found with id: " + categoryId);
        this.categoryId = categoryId;
        this.identifier = null;
        this.field = "id";
    }

    public CategoryNotFoundException(String identifier, String field) {
        super("Category not found with " + field + ": " + identifier);
        this.categoryId = null;
        this.identifier = identifier;
        this.field = field;
    }

    public CategoryNotFoundException(String message) {
        super(message);
        this.categoryId = null;
        this.identifier = null;
        this.field = null;
    }

    public CategoryNotFoundException(String message, Throwable cause) {
        super(message, cause);
        this.categoryId = null;
        this.identifier = null;
        this.field = null;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public String getIdentifier() {
        return identifier;
    }

    public String getField() {
        return field;
    }
}
