package com.voyagia.backend.exception;

public class UserAlreadyExistsException extends RuntimeException {

    private final String field;
    private final String value;

    public UserAlreadyExistsException(String field, String value) {
        super("User already exists with " + field + ": " + value);
        this.field = field;
        this.value = value;
    }

    public UserAlreadyExistsException(String message) {
        super(message);
        this.field = null;
        this.value = null;
    }

    public UserAlreadyExistsException(String message, Throwable cause) {
        super(message, cause);
        this.field = null;
        this.value = null;
    }

    public String getField() {
        return field;
    }

    public String getValue() {
        return value;
    }
}
