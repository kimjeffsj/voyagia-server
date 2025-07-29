package com.voyagia.backend.controller;

import com.voyagia.backend.dto.cart.CartItemRequest;
import com.voyagia.backend.dto.cart.CartItemResponse;
import com.voyagia.backend.dto.cart.CartResponse;
import com.voyagia.backend.dto.cart.CartUpdateRequest;
import com.voyagia.backend.dto.common.ApiResponse;
import com.voyagia.backend.entity.User;
import com.voyagia.backend.exception.*;
import com.voyagia.backend.service.CartService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Cart Controller
 * <p>
 * Cart management REST API
 * Add, Update, Delete, Query cart
 */
@RestController
@RequestMapping("/api/cart")
@CrossOrigin(origins = "*", maxAge = 3600)
public class CartController {
    private static final Logger logger = LoggerFactory.getLogger(CartController.class);

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    /**
     * Get current user's cart
     *
     * @return cart
     */
    @GetMapping
    public ResponseEntity<?> getCart() {
        logger.debug("Get current user's cart");

        try {
            Long userId = getCurrentUserId();
            CartResponse cart = cartService.getCartByUserId(userId);

            return ResponseEntity.ok(
                    ApiResponse.success("Cart retrieved successfully", cart));
        } catch (UserNotFoundException e) {
            logger.warn("User not found: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    ApiResponse.error("Authentication required"));
        } catch (Exception e) {
            logger.error("Failed to get cart: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    ApiResponse.error("Failed to retrieve cart"));
        }
    }

    /**
     * Add to cart
     *
     * @param request       Add item to cart request
     * @param bindingResult Validation result
     * @return Cart with added item
     */
    @PostMapping("/items")
    public ResponseEntity<?> addItemToCart(
            @Valid @RequestBody CartItemRequest request,
            BindingResult bindingResult) {
        logger.info("Add item to cart: productId={}, quantity={}", request.getProductId(), request.getQuantity());

        // Validation
        if (bindingResult.hasErrors()) {
            Map<String, String> errors = new HashMap<>();
            bindingResult.getFieldErrors().forEach(error -> errors.put(error.getField(), error.getDefaultMessage()));
            return ResponseEntity.badRequest().body(
                    ApiResponse.error("Validation failed", errors));
        }

        try {
            Long userId = getCurrentUserId();
            CartItemResponse cartItem = cartService.addItemToCart(userId, request);

            logger.info("Item added to cart successfully: cartItemId={}", cartItem.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(
                    ApiResponse.success("Item added to cart successfully", cartItem));

        } catch (UserNotFoundException e) {
            logger.warn("User not found: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    ApiResponse.error("Authentication required"));
        } catch (ProductNotFoundException e) {
            logger.warn("Product not found: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    ApiResponse.error("Product not found"));
        } catch (InsufficientStockException e) {
            logger.warn("Insufficient stock: {}", e.getMessage());
            return ResponseEntity.badRequest().body(
                    ApiResponse.error("Insufficient stock: " + e.getMessage()));
        } catch (InvalidQuantityException e) {
            logger.warn("Invalid quantity: {}", e.getMessage());
            return ResponseEntity.badRequest().body(
                    ApiResponse.error("Invalid quantity: " + e.getMessage()));
        } catch (Exception e) {
            logger.error("Failed to add item to cart: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    ApiResponse.error("Failed to add item to cart"));
        }
    }

    /**
     * Update Cart
     *
     * @param itemId        cart item ID
     * @param request       quantity update request
     * @param bindingResult validation result
     * @return updated cart item
     */
    @PutMapping("/items/{itemId}")
    public ResponseEntity<?> updateCartItem(
            @PathVariable Long itemId,
            @Valid @RequestBody CartItemRequest request,
            BindingResult bindingResult) {

        logger.info("Update cart item: itemId={}, quantity={}", itemId, request.getQuantity());

        // Validation
        if (bindingResult.hasErrors()) {
            Map<String, String> errors = new HashMap<>();
            bindingResult.getFieldErrors().forEach(error -> errors.put(error.getField(), error.getDefaultMessage()));
            return ResponseEntity.badRequest().body(
                    ApiResponse.error("Validation failed", errors));
        }

        try {
            Long userId = getCurrentUserId();
            CartItemResponse cartItem = cartService.updateCartItemQuantity(userId, itemId, request.getQuantity());

            logger.info("Cart item updated successfully: itemId={}", itemId);
            return ResponseEntity.ok(
                    ApiResponse.success("Cart item updated successfully", cartItem));

        } catch (UserNotFoundException e) {
            logger.warn("User not found: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    ApiResponse.error("Authentication required"));
        } catch (CartItemNotFoundException e) {
            logger.warn("Cart item not found: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    ApiResponse.error("Cart item not found"));
        } catch (InsufficientStockException e) {
            logger.warn("Insufficient stock: {}", e.getMessage());
            return ResponseEntity.badRequest().body(
                    ApiResponse.error("Insufficient stock: " + e.getMessage()));
        } catch (InvalidQuantityException e) {
            logger.warn("Invalid quantity: {}", e.getMessage());
            return ResponseEntity.badRequest().body(
                    ApiResponse.error("Invalid quantity: " + e.getMessage()));
        } catch (Exception e) {
            logger.error("Failed to update cart item: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    ApiResponse.error("Failed to update cart item"));
        }
    }

    /**
     * Remove Item from cart
     *
     * @param itemId Cart item ID
     * @return result
     */
    @DeleteMapping("/items/{itemId}")
    public ResponseEntity<?> removeCartItem(@PathVariable Long itemId) {
        logger.info("Remove cart item: itemId={}", itemId);

        try {
            Long userId = getCurrentUserId();
            cartService.removeItemFromCart(userId, itemId);

            logger.info("Cart item removed successfully: itemId={}", itemId);
            return ResponseEntity.ok(
                    ApiResponse.success("Item removed from cart successfully"));

        } catch (UserNotFoundException e) {
            logger.warn("User not found: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    ApiResponse.error("Authentication required"));
        } catch (CartItemNotFoundException e) {
            logger.warn("Cart item not found: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    ApiResponse.error("Cart item not found"));
        } catch (Exception e) {
            logger.error("Failed to remove cart item: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    ApiResponse.error("Failed to remove cart item"));
        }
    }

    /**
     * Clear Cart
     *
     * @return result
     */
    @DeleteMapping
    public ResponseEntity<?> clearCart() {
        logger.info("Clear cart");

        try {
            Long userId = getCurrentUserId();
            cartService.clearCart(userId);

            logger.info("Cart cleared successfully");
            return ResponseEntity.ok(
                    ApiResponse.success("Cart cleared successfully"));

        } catch (UserNotFoundException e) {
            logger.warn("User not found: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    ApiResponse.error("Authentication required"));
        } catch (Exception e) {
            logger.error("Failed to clear cart: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    ApiResponse.error("Failed to clear cart"));
        }
    }

    /**
     * Update Cart
     *
     * @param request       update request
     * @param bindingResult validation result
     * @return updated cart
     */
    @PutMapping
    public ResponseEntity<?> updateCart(
            @Valid @RequestBody CartUpdateRequest request,
            BindingResult bindingResult) {

        logger.info("Update cart: operation={}, itemCount={}",
                request.getOperation(), request.getItemCount());

        // 유효성 검사
        if (bindingResult.hasErrors()) {
            Map<String, String> errors = new HashMap<>();
            bindingResult.getFieldErrors().forEach(error -> errors.put(error.getField(), error.getDefaultMessage()));
            return ResponseEntity.badRequest().body(
                    ApiResponse.error("Validation failed", errors));
        }

        try {
            Long userId = getCurrentUserId();
            CartResponse cart = cartService.updateCart(userId, request);

            logger.info("Cart updated successfully");
            return ResponseEntity.ok(
                    ApiResponse.success("Cart updated successfully", cart));

        } catch (UserNotFoundException e) {
            logger.warn("User not found: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    ApiResponse.error("Authentication required"));
        } catch (ProductNotFoundException e) {
            logger.warn("Product not found: {}", e.getMessage());
            return ResponseEntity.badRequest().body(
                    ApiResponse.error("Some products were not found"));
        } catch (InsufficientStockException e) {
            logger.warn("Insufficient stock: {}", e.getMessage());
            return ResponseEntity.badRequest().body(
                    ApiResponse.error("Insufficient stock for some items"));
        } catch (Exception e) {
            logger.error("Failed to update cart: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    ApiResponse.error("Failed to update cart"));
        }
    }

    /**
     * Get Cart items
     *
     * @return cart items list
     */
    @GetMapping("/items")
    public ResponseEntity<?> getCartItems() {
        logger.debug("Get cart items");

        try {
            Long userId = getCurrentUserId();
            List<CartItemResponse> items = cartService.getCartItems(userId);

            return ResponseEntity.ok(
                    ApiResponse.success("Cart items retrieved successfully", items));

        } catch (UserNotFoundException e) {
            logger.warn("User not found: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    ApiResponse.error("Authentication required"));
        } catch (Exception e) {
            logger.error("Failed to get cart items: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    ApiResponse.error("Failed to retrieve cart items"));
        }
    }

    /**
     * Cart summary
     *
     * @return summarized cart
     */
    @GetMapping("/summary")
    public ResponseEntity<?> getCartSummary() {
        logger.debug("Get cart summary");

        try {
            Long userId = getCurrentUserId();

            Map<String, Object> summary = new HashMap<>();
            summary.put("itemCount", cartService.getCartItemCount(userId));
            summary.put("totalQuantity", cartService.getTotalQuantityByUserId(userId));
            summary.put("totalAmount", cartService.getTotalAmountByUserId(userId));
            summary.put("isEmpty", cartService.isCartEmpty(userId));

            return ResponseEntity.ok(
                    ApiResponse.success("Cart summary retrieved successfully", summary));

        } catch (UserNotFoundException e) {
            logger.warn("User not found: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    ApiResponse.error("Authentication required"));
        } catch (Exception e) {
            logger.error("Failed to get cart summary: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    ApiResponse.error("Failed to retrieve cart summary"));
        }
    }

    /**
     * Sync cart(price update, quantity check, etc.)
     *
     * @return Synced cart
     */
    @PostMapping("/sync")
    public ResponseEntity<?> syncCart() {
        logger.info("Sync cart");

        try {
            Long userId = getCurrentUserId();
            CartResponse cart = cartService.syncCart(userId);

            logger.info("Cart synced successfully");
            return ResponseEntity.ok(
                    ApiResponse.success("Cart synced successfully", cart));

        } catch (UserNotFoundException e) {
            logger.warn("User not found: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    ApiResponse.error("Authentication required"));
        } catch (Exception e) {
            logger.error("Failed to sync cart: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    ApiResponse.error("Failed to sync cart"));
        }
    }

    /**
     * Check if product is in cart
     *
     * @param productId product ID
     * @return result, quantity
     */
    @GetMapping("/check/{productId}")
    public ResponseEntity<Map<String, Object>> checkProductInCart(@PathVariable Long productId) {
        logger.debug("Check product in cart: productId={}", productId);

        try {
            Long userId = getCurrentUserId();

            boolean hasProduct = cartService.hasProduct(userId, productId);
            int quantity = cartService.getProductQuantityInCart(userId, productId);

            Map<String, Object> response = new HashMap<>();
            response.put("hasProduct", hasProduct);
            response.put("quantity", quantity);
            response.put("productId", productId);

            return ResponseEntity.ok(response);

        } catch (UserNotFoundException e) {
            logger.warn("User not found: {}", e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("hasProduct", false);
            errorResponse.put("quantity", 0);
            errorResponse.put("error", "Authentication required");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
        } catch (Exception e) {
            logger.error("Failed to check product in cart: {}", e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("hasProduct", false);
            errorResponse.put("quantity", 0);
            errorResponse.put("error", "Internal server error");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ===== Private Helper Methods =====

    /**
     * Get current user's ID
     *
     * @return user ID
     * @throws UserNotFoundException if not authenticated
     */
    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated() ||
                "anonymousUser".equals(authentication.getPrincipal())) {
            throw new UserNotFoundException("User not authenticated");
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof User user) {
            return user.getId();
        }
        throw new UserNotFoundException("Invalid user authentication");
    }

    // ===== Exception Handlers =====

    /**
     * CartNotFoundException
     */
    @ExceptionHandler(CartNotFoundException.class)
    public ResponseEntity<ApiResponse<String>> handleCartNotFoundException(CartNotFoundException e) {
        logger.warn("Cart not found: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                ApiResponse.error("Cart not found"));
    }

    /**
     * CartItemNotFoundException
     */
    @ExceptionHandler(CartItemNotFoundException.class)
    public ResponseEntity<ApiResponse<String>> handleCartItemNotFoundException(CartItemNotFoundException e) {
        logger.warn("Cart item not found: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                ApiResponse.error("Cart item not found"));
    }

    /**
     * InsufficientStockException
     */
    @ExceptionHandler(InsufficientStockException.class)
    public ResponseEntity<ApiResponse<String>> handleInsufficientStockException(InsufficientStockException e) {
        logger.warn("Insufficient stock: {}", e.getMessage());
        return ResponseEntity.badRequest().body(
                ApiResponse.error("Insufficient stock: " + e.getMessage()));
    }

    /**
     * InvalidQuantityException
     */
    @ExceptionHandler(InvalidQuantityException.class)
    public ResponseEntity<ApiResponse<String>> handleInvalidQuantityException(InvalidQuantityException e) {
        logger.warn("Invalid quantity: {}", e.getMessage());
        return ResponseEntity.badRequest().body(
                ApiResponse.error("Invalid quantity: " + e.getMessage()));
    }

    /**
     * ProductNotFoundException
     */
    @ExceptionHandler(ProductNotFoundException.class)
    public ResponseEntity<ApiResponse<String>> handleProductNotFoundException(ProductNotFoundException e) {
        logger.warn("Product not found: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                ApiResponse.error("Product not found"));
    }

    /**
     * UserNotFoundException
     */
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ApiResponse<String>> handleUserNotFoundException(UserNotFoundException e) {
        logger.warn("User not found: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                ApiResponse.error("Authentication required"));
    }

    /**
     * General error handler
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<String>> handleGeneralException(Exception e) {
        logger.error("Unexpected error in CartController: {}", e.getMessage(), e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                ApiResponse.error("Internal server error occurred"));
    }
}
