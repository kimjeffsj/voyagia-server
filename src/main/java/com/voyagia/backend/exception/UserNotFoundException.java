package com.voyagia.backend.exception;

public class UserNotFoundException extends RuntimeException {

    private final Long userId;
    private final String identifier; // email or username

    public UserNotFoundException(Long userId) {
        super("User not found with id: " + userId);
        this.userId = userId;
        this.identifier = null;
    }

    public UserNotFoundException(String identifier, String field) {
        super("User not found with " + field + ": " + identifier);
        this.userId = null;
        this.identifier = identifier;
    }

    public UserNotFoundException(String message) {
        super(message);
        this.userId = null;
        this.identifier = null;
    }

    public Long getUserId() {
        return userId;
    }

    public String getIdentifier() {
        return identifier;
    }
}
