package com.voyagia.backend.exception;

/**
 * Category already exists exception
 * <p>
 * - Name is already exists ( under same parent )
 * - slug is already exists
 * <p>
 * * TODO: will be mapped as HTTP 409 Conflict, in @ControllerAdvice
 */
public class CategoryAlreadyExistsException extends RuntimeException {

    private final String field; // "sulg" or "name"
    private final String value; // duplicated value
    private final Long parentId; // parent category ID ( when name is duplicated )

    public CategoryAlreadyExistsException(String field, String value) {
        super("Category already eixsts with " + field + ": " + value);
        this.field = field;
        this.value = value;
        this.parentId = null;
    }

    public CategoryAlreadyExistsException(String field, String value, Long parentId) {
        super("Category already exists with " + field + ": " + value +
                (parentId != null ? " under parent category: " + parentId : ""));
        this.field = field;
        this.value = value;
        this.parentId = parentId;
    }

    public CategoryAlreadyExistsException(String message) {
        super(message);
        this.field = null;
        this.value = null;
        this.parentId = null;
    }

    public CategoryAlreadyExistsException(String message, Throwable cause) {
        super(message, cause);
        this.field = null;
        this.value = null;
        this.parentId = null;
    }

    public String getField() {
        return field;
    }

    public String getValue() {
        return value;
    }

    public Long getParentId() {
        return parentId;
    }
}
