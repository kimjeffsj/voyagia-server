package com.voyagia.backend.service;

import com.voyagia.backend.dto.cart.CartItemRequest;
import com.voyagia.backend.dto.cart.CartItemResponse;
import com.voyagia.backend.dto.cart.CartResponse;
import com.voyagia.backend.dto.cart.CartUpdateRequest;
import com.voyagia.backend.entity.Cart;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Cart Service interface
 */
public interface CartService {

    // ===== Cart Management =====

    /**
     * Get cart by user id
     *
     * @param userId user ID
     * @return CartItemResponse DTO
     * @throws UserNotFoundException cannot find user
     */
    CartResponse getCartByUserId(Long userId);

    /**
     * Get cart items by user id
     *
     * @param userId user ID
     * @return Cart items list
     * @throws UserNotFoundException cannot find user
     */
    List<CartItemResponse> getCartItems(Long userId);

    /**
     * Clear Cart
     *
     * @param userId user ID
     * @throws UserNotFoundException cannot find user
     */
    void clearCart(Long userId);

    /**
     * Check if cart is empty
     *
     * @param userId user ID
     * @return true if empty or false
     */
    boolean isCartEmpty(Long userId);

    /**
     * Get Cart Items count
     *
     * @param userId user ID
     * @return cart items count
     */
    int getCartItemCount(Long userId);

    // ===== Cart Item Operations =====

    /**
     * Add product to cart
     *
     * @param userId  user ID
     * @param request Cart Item request DTO
     * @return Cart Item response DTO
     * @throws UserNotFoundException      cannot find user
     * @throws ProductNotFoundException   cannot find product
     * @throws InsufficientStockException insufficient stock
     * @throws InvalidQuantityException   invalid quantity
     */
    CartItemResponse addItemToCart(Long userId, CartItemRequest request);

    /**
     * Update cart item quantity
     *
     * @param userId     user ID
     * @param cartItemId cart item ID
     * @param quantity   new quantity
     * @return updated cart item response DTO
     * @throws UserNotFoundException      cannot find user
     * @throws CartItemNotFoundException  cannot find cart item
     * @throws InsufficientStockException insufficient stock
     * @throws InvalidQuantityException   invalid quantity
     */
    CartItemResponse updateCartItemQuantity(Long userId, Long cartItemId, Integer quantity);

    /**
     * Remove item from cart
     *
     * @param userId     user ID
     * @param cartItemId cart item ID
     * @throws UserNotFoundException     cannot find user
     * @throws CartItemNotFoundException cannot find cart item
     */
    void removeItemFromCart(Long userId, Long cartItemId);

    /**
     * Remove specific product from cart
     *
     * @param userId    user ID
     * @param productId product ID
     * @throws UserNotFoundException     cannot find user
     * @throws CartItemNotFoundException cannot find cart item
     */
    void removeProductFromCart(Long userId, Long productId);

    /**
     * Bulk update cart
     *
     * @param userId  user ID
     * @param request cart update request DTO
     * @return updated cart response DTO
     * @throws UserNotFoundException      cannot find user
     * @throws ProductNotFoundException   cannot find product
     * @throws InsufficientStockException insufficient stock
     */
    CartResponse updateCart(Long userId, CartUpdateRequest request);

    // ===== Cart Item Queries =====

    /**
     * Get specific cart item
     *
     * @param userId     user ID
     * @param cartItemId cart item ID
     * @return cart item response DTO
     * @throws UserNotFoundException     cannot find user
     * @throws CartItemNotFoundException cannot find cart item
     */
    CartItemResponse getCartItem(Long userId, Long cartItemId);

    /**
     * Check if user's cart contains specific product
     *
     * @param userId    user ID
     * @param productId product ID
     * @return true if exists, false otherwise
     */
    boolean hasProduct(Long userId, Long productId);

    /**
     * Get quantity of specific product in user's cart
     *
     * @param userId    user ID
     * @param productId product ID
     * @return quantity of product in cart (0 if not found)
     */
    int getProductQuantityInCart(Long userId, Long productId);

    // ===== Price Calculations =====

    /**
     * Calculate cart subtotal (excluding tax)
     *
     * @param userId user ID
     * @return cart subtotal
     */
    BigDecimal calculateCartSubtotal(Long userId);

    /**
     * Calculate cart tax
     *
     * @param userId  user ID
     * @param taxRate tax rate (e.g., 0.10 = 10%)
     * @return tax amount
     */
    BigDecimal calculateCartTax(Long userId, BigDecimal taxRate);

    /**
     * Calculate cart total (including tax)
     *
     * @param userId  user ID
     * @param taxRate tax rate
     * @return cart total
     */
    BigDecimal calculateCartTotal(Long userId, BigDecimal taxRate);

    /**
     * Calculate individual item subtotal
     *
     * @param cartItemId cart item ID
     * @return item subtotal
     * @throws CartItemNotFoundException cannot find cart item
     */
    BigDecimal calculateItemSubtotal(Long cartItemId);

    // ===== Validation Methods =====

    /**
     * Validate cart items
     *
     * @param userId user ID
     * @return list of invalid items
     */
    List<CartItemResponse> validateCartItems(Long userId);

    /**
     * Check product stock availability
     *
     * @param productId product ID
     * @param quantity  required quantity
     * @return true if stock is sufficient, false if insufficient
     * @throws ProductNotFoundException cannot find product
     */
    boolean checkProductAvailability(Long productId, Integer quantity);

    /**
     * Validate if user owns the cart item
     *
     * @param userId     user ID
     * @param cartItemId cart item ID
     * @return true if owner, false otherwise
     */
    boolean validateUserOwnership(Long userId, Long cartItemId);

    /**
     * Validate quantity
     *
     * @param quantity quantity
     * @return true if valid, false if invalid
     */
    boolean validateQuantity(Integer quantity);

    /**
     * Validate and synchronize entire cart
     * Resolves issues like price changes, insufficient stock, etc.
     *
     * @param userId user ID
     * @return synchronized cart response DTO
     */
    CartResponse syncCart(Long userId);

    // ===== Utility Methods =====

    /**
     * Find cart item entity by ID (internal use)
     *
     * @param cartItemId cart item ID
     * @return cart item entity
     * @throws CartItemNotFoundException cannot find cart item
     */
    Cart findCartItemById(Long cartItemId);

    /**
     * Find cart item entity by ID (Optional)
     *
     * @param cartItemId cart item ID
     * @return cart item entity (Optional)
     */
    Optional<Cart> findCartItemByIdOptional(Long cartItemId);

    /**
     * Find cart item by user and product
     *
     * @param userId    user ID
     * @param productId product ID
     * @return cart item entity (Optional)
     */
    Optional<Cart> findCartItemByUserAndProduct(Long userId, Long productId);

    /**
     * Get user's total cart quantity
     *
     * @param userId user ID
     * @return total quantity
     */
    Integer getTotalQuantityByUserId(Long userId);

    /**
     * Get user's total cart amount
     *
     * @param userId user ID
     * @return total amount
     */
    BigDecimal getTotalAmountByUserId(Long userId);
}
