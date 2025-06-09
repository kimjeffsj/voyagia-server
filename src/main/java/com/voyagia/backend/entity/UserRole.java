package com.voyagia.backend.entity;

public enum UserRole {
    CUSTOMER("Customer"),
    ADMIN("Administrator"),
    MANAGER("Manager");

    private final String displayName;

    UserRole(String displayName) {
        this.displayName = displayName;
    }

    public boolean isAdmin() {
        return this == ADMIN;
    }

    public boolean isManager() {
        return this == MANAGER;
    }

    public boolean isCustomer() {
        return this == CUSTOMER;
    }

    public boolean hasAdminPrivileges() {
        return this == ADMIN || this == MANAGER;
    }
}
