package com.voyagia.backend.exception;

public class CartNotFoundException extends RuntimeException {

    private final Long cartId;
    private final Long userId;
    private final Long productId;

    public CartNotFoundException(Long cartId) {
        super("Cart item not found with id: " + cartId);
        this.cartId = cartId;
        this.userId = null;
        this.productId = null;
    }

    public CartNotFoundException(Long userId, Long productId) {
        super("Cart item not found for user " + userId + " and product " + productId);
        this.cartId = null;
        this.userId = userId;
        this.productId = productId;
    }

    public CartNotFoundException(String message) {
        super(message);
        this.cartId = null;
        this.userId = null;
        this.productId = null;
    }

    public Long getCartId() {
        return cartId;
    }

    public Long getUserId() {
        return userId;
    }

    public Long getProductId() {
        return productId;
    }
}
