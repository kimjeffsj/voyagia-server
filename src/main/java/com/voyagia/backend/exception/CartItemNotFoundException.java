package com.voyagia.backend.exception;

public class CartItemNotFoundException extends RuntimeException {

    private final Long cartItemId;
    private final Long userId;

    public CartItemNotFoundException(Long cartItemId) {
        super("Cart item not found with id: " + cartItemId);
        this.cartItemId = cartItemId;
        this.userId = null;
    }

    public CartItemNotFoundException(Long userId, String message) {
        super("Cart item not found for user " + userId + ": " + message);
        this.cartItemId = null;
        this.userId = userId;
    }
    
    public CartItemNotFoundException(String message) {
        super(message);
        this.cartItemId = null;
        this.userId = null;
    }

    public Long getUserId() {
        return userId;
    }

    public Long getCartItemId() {
        return cartItemId;
    }
}
