package com.voyagia.backend.service.impl;

import com.voyagia.backend.dto.cart.*;
import com.voyagia.backend.entity.Cart;
import com.voyagia.backend.entity.Product;
import com.voyagia.backend.entity.User;
import com.voyagia.backend.exception.CartItemNotFoundException;
import com.voyagia.backend.exception.InsufficientStockException;
import com.voyagia.backend.exception.InvalidQuantityException;
import com.voyagia.backend.exception.ProductNotFoundException;
import com.voyagia.backend.repository.CartRepository;
import com.voyagia.backend.service.CartService;
import com.voyagia.backend.service.ProductService;
import com.voyagia.backend.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Cart Service Implementation
 */
@Service
@Transactional
public class CartServiceImpl implements CartService {

    private static final Logger logger = LoggerFactory.getLogger(CartServiceImpl.class);
    private static final int MAX_CART_ITEMS = 50;
    private static final int MAX_QUANTITY_PER_ITEM = 99;
    private static final BigDecimal DEFAULT_TAX_RATE = new BigDecimal("0.10"); // tax rate of 10%

    private final CartRepository cartRepository;
    private final UserService userService;
    private final ProductService productService;
    private final CartDTOMapper cartDTOMapper;

    public CartServiceImpl(CartRepository cartRepository,
                           UserService userService,
                           ProductService productService,
                           CartDTOMapper cartDTOMapper) {
        this.cartRepository = cartRepository;
        this.userService = userService;
        this.productService = productService;
        this.cartDTOMapper = cartDTOMapper;
    }

    // ===== Cart Management =====
    @Override
    @Transactional(readOnly = true)
    public CartResponse getCartByUserId(Long userId) {
        logger.debug("Get cart for user: {}", userId);

        userService.findById(userId);

        List<Cart> cartItems = cartRepository.findByUserIdOrderByCreatedAtDesc(userId);
        return cartDTOMapper.toCartResponse(cartItems, userId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CartItemResponse> getCartItems(Long userId) {
        logger.debug("Get cart items for user: {}", userId);

        userService.findById(userId);

        List<Cart> cartItems = cartRepository.findByUserIdOrderByCreatedAtDesc(userId);
        return cartDTOMapper.toCartItemResponseList(cartItems);
    }

    @Override
    public void clearCart(Long userId) {
        logger.info("Clear cart for user: {}", userId);

        userService.findById(userId);

        cartRepository.deleteByUserId(userId);
        logger.info("Cart cleared successfully for user: {}", userId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isCartEmpty(Long userId) {
        List<Cart> cartItems = cartRepository.findByUserIdOrderByCreatedAtDesc(userId);
        return cartItems.isEmpty();
    }

    @Override
    @Transactional(readOnly = true)
    public int getCartItemCount(Long userId) {
        List<Cart> cartItems = cartRepository.findByUserIdOrderByCreatedAtDesc(userId);
        return cartItems.size();
    }

    // ===== Cart Item Operations =====

    @Override
    public CartItemResponse addItemToCart(Long userId, CartItemRequest request) {
        logger.info("Add item to cart: userId={}, productId={}, quantity={}",
                userId, request.getProductId(), request.getQuantity());

        // Validation
        validateAddItemRequest(userId, request);

        User user = userService.findById(userId);
        Product product = productService.findById(request.getProductId());

        // Check availability
        if (!checkProductAvailability(request.getProductId(), request.getQuantity())) {
            throw new InsufficientStockException("Product is out of stock or insufficient quantity available");
        }

        // Check cart item count
        if (getCartItemCount(userId) >= MAX_CART_ITEMS) {
            throw new InvalidQuantityException("Maximum cart items limit exceeded");
        }

        // Check existing item
        Optional<Cart> existingItem = cartRepository.findByUserAndProduct(user, product);

        Cart cartItem;
        if (existingItem.isPresent()) {
            // Update exiting item quantity
            cartItem = existingItem.get();
            int newQuantity = cartItem.getQuantity() + request.getQuantity();

            // Check quantity limit
            if (newQuantity > MAX_QUANTITY_PER_ITEM) {
                throw new InvalidQuantityException("Maximum quantity per item exceeded");
            }

            // Check total quantity
            if (!checkProductAvailability(request.getProductId(), newQuantity)) {
                throw new InsufficientStockException("Not enough stock for the requested quantity");
            }

            cartItem.setQuantity(newQuantity);
            cartItem.updateFromProduct();
            logger.info("Updated existing cart item: id={}, newQuantity={}", cartItem.getId(), newQuantity);
        } else {
            // Add new item
            cartItem = cartDTOMapper.toCartEntity(request, userId);
            logger.info("Adding new cart item: productId={}, quantity={}", request.getProductId(), request.getQuantity());
        }

        Cart savedItem = cartRepository.save(cartItem);
        logger.info("Cart item saved successfully: id={}", savedItem.getId());

        return cartDTOMapper.toCartItemResponse(savedItem);
    }

    @Override
    public CartItemResponse updateCartItemQuantity(Long userId, Long cartItemId, Integer quantity) {
        logger.info("Update cart item quantity: userId={}, cartItemId={}, quantity={}",
                userId, cartItemId, quantity);

        // Validation
        if (!validateQuantity(quantity)) {
            throw new InvalidQuantityException("Invalid quantity: " + quantity);
        }

        if (quantity > MAX_QUANTITY_PER_ITEM) {
            throw new InvalidQuantityException("Maximum quantity per item exceeded");
        }

        Cart cartItem = findCartItemById(cartItemId);

        // Validate ownership
        if (!validateUserOwnership(userId, cartItemId)) {
            throw new CartItemNotFoundException("Cart item not found or access denied");
        }

        // Check stock
        if (!checkProductAvailability(cartItem.getProduct().getId(), quantity)) {
            throw new InsufficientStockException("Not enough stock for the requested quantity");
        }

        cartItem.setQuantity(quantity);
        cartItem.updateFromProduct();

        Cart savedItem = cartRepository.save(cartItem);
        logger.info("Cart item quantity updated successfully: id={}, newQuantity={}", savedItem.getId(), quantity);

        return cartDTOMapper.toCartItemResponse(savedItem);
    }

    @Override
    public void removeItemFromCart(Long userId, Long cartItemId) {
        logger.info("Remove item from cart: userId={}, cartItemId={}", userId, cartItemId);

        Cart cartItem = findCartItemById(cartItemId);

        // Validate ownership
        if (!validateUserOwnership(userId, cartItemId)) {
            throw new CartItemNotFoundException("Cart item not found or access denied");
        }

        cartRepository.delete(cartItem);
        logger.info("Cart item removed successfully: id={}", cartItemId);
    }

    @Override
    public void removeProductFromCart(Long userId, Long productId) {
        logger.info("Remove product from cart: userId={}, productId={}", userId, productId);

        User user = userService.findById(userId);
        Product product = productService.findById(productId);

        Optional<Cart> cartItem = cartRepository.findByUserAndProduct(user, product);
        if (cartItem.isPresent()) {
            cartRepository.delete(cartItem.get());
            logger.info("Product removed from cart successfully: productId={}", productId);
        } else {
            throw new CartItemNotFoundException("Product not found in cart");
        }
    }

    @Override
    public CartResponse updateCart(Long userId, CartUpdateRequest request) {
        logger.info("Update cart: userId={}, operation={}, itemCount={}",
                userId, request.getOperation(), request.getItemCount());

        userService.findById(userId);

        if (request.isClear()) {
            clearCart(userId);
            return getCartByUserId(userId);
        }

        if (request.isSync()) {
            return syncCart(userId);
        }

        if (request.hasItems()) {
            for (CartItemRequest itemRequest : request.getItems()) {
                try {
                    Optional<Cart> existingItem = findCartItemByUserAndProduct(userId, itemRequest.getProductId());

                    if (existingItem.isPresent()) {
                        // Update the item
                        updateCartItemQuantity(userId, existingItem.get().getId(), itemRequest.getQuantity());
                    } else {
                        // Add new item
                        addItemToCart(userId, itemRequest);
                    }
                } catch (Exception e) {
                    logger.warn("Failed to update cart item: productId={}, error={}",
                            itemRequest.getProductId(), e.getMessage());
                    // sing item fail will not stop the whole process
                }
            }
        }

        return getCartByUserId(userId);
    }

    // ===== Cart Item Queries =====

    @Override
    @Transactional(readOnly = true)
    public CartItemResponse getCartItem(Long userId, Long cartItemId) {
        logger.debug("Get cart item: userId={}, cartItemId={}", userId, cartItemId);

        Cart cartItem = findCartItemById(cartItemId);

        // Validate ownership
        if (!validateUserOwnership(userId, cartItemId)) {
            throw new CartItemNotFoundException("Cart item not found or access denied");
        }

        return cartDTOMapper.toCartItemResponse(cartItem);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasProduct(Long userId, Long productId) {
        return cartRepository.existsByUserIdAndProductId(userId, productId);
    }

    @Override
    @Transactional(readOnly = true)
    public int getProductQuantityInCart(Long userId, Long productId) {
        Optional<Cart> cartItem = cartRepository.findByUserIdAndProductId(userId, productId);
        return cartItem.map(Cart::getQuantity).orElse(0);
    }

    // ===== Price Calculations =====

    @Override
    @Transactional(readOnly = true)
    public BigDecimal calculateCartSubtotal(Long userId) {
        BigDecimal subtotal = cartRepository.getTotalAmountByUserId(userId);
        return subtotal != null ? subtotal : BigDecimal.ZERO;
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal calculateCartTax(Long userId, BigDecimal taxRate) {
        BigDecimal subtotal = calculateCartSubtotal(userId);
        return subtotal.multiply(taxRate != null ? taxRate : DEFAULT_TAX_RATE);
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal calculateCartTotal(Long userId, BigDecimal taxRate) {
        BigDecimal subtotal = calculateCartSubtotal(userId);
        BigDecimal tax = calculateCartTax(userId, taxRate);
        return subtotal.add(tax);
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal calculateItemSubtotal(Long cartItemId) {
        Cart cartItem = findCartItemById(cartItemId);
        return cartItem.getTotalPrice();
    }

    // ===== Validation Methods =====

    @Override
    @Transactional(readOnly = true)
    public List<CartItemResponse> validateCartItems(Long userId) {
        logger.debug("Validate cart items for user: {}", userId);

        List<Cart> cartItems = cartRepository.findByUserIdOrderByCreatedAtDesc(userId);
        List<CartItemResponse> invalidItems = new ArrayList<>();

        for (Cart item : cartItems) {
            try {
                Product product = item.getProduct();

                // Check active item
                if (!product.getIsActive()) {
                    invalidItems.add(cartDTOMapper.toCartItemResponse(item));
                    continue;
                }

                // Check stock
                if (!checkProductAvailability(product.getId(), item.getQuantity())) {
                    invalidItems.add(cartDTOMapper.toCartItemResponse(item));
                    continue;
                }

                // Check price changes
                if (!item.getUnitPrice().equals(product.getPrice())) {
                    invalidItems.add(cartDTOMapper.toCartItemResponse(item));
                }

            } catch (Exception e) {
                logger.warn("Error validating cart item: id={}, error={}", item.getId(), e.getMessage());
                invalidItems.add(cartDTOMapper.toCartItemResponse(item));
            }
        }

        return invalidItems;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean checkProductAvailability(Long productId, Integer quantity) {
        try {
            return productService.hasEnoughStock(productId, quantity);
        } catch (ProductNotFoundException e) {
            logger.warn("Product not found during availability check: {}", productId);
            return false;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public boolean validateUserOwnership(Long userId, Long cartItemId) {
        Optional<Cart> cartItem = cartRepository.findById(cartItemId);
        return cartItem.isPresent() && cartItem.get().getUser().getId().equals(userId);
    }

    @Override
    public boolean validateQuantity(Integer quantity) {
        return quantity != null && quantity > 0 && quantity <= MAX_QUANTITY_PER_ITEM;
    }

    @Override
    public CartResponse syncCart(Long userId) {
        logger.info("Sync cart for user: {}", userId);

        List<Cart> cartItems = cartRepository.findByUserIdOrderByCreatedAtDesc(userId);

        for (Cart item : cartItems) {
            try {
                Product product = item.getProduct();

                // Remove inactive product
                if (!product.getIsActive()) {
                    cartRepository.delete(item);
                    logger.info("Removed inactive product from cart: productId={}", product.getId());
                    continue;
                }

                if (!item.getUnitPrice().equals(product.getPrice())) {
                    item.updateFromProduct();
                    cartRepository.save(item);
                    logger.info("Updated price for cart item: id={}, newPrice={}", item.getId(), product.getPrice());
                }

                // Update quantity against stocks
                if (!checkProductAvailability(product.getId(), item.getQuantity())) {
                    int availableStock = productService.getStockQuantity(product.getId());
                    if (availableStock > 0) {
                        item.setQuantity(Math.min(availableStock, MAX_QUANTITY_PER_ITEM));
                        cartRepository.save(item);
                        logger.info("Adjusted quantity for cart item: id={}, newQuantity={}", item.getId(), item.getQuantity());
                    } else {
                        cartRepository.delete(item);
                        logger.info("Removed out of stock product from cart: productId={}", product.getId());
                    }
                }

            } catch (Exception e) {
                logger.error("Error syncing cart item: id={}, error={}", item.getId(), e.getMessage());
                // Remove errored item
                cartRepository.delete(item);
            }
        }

        return getCartByUserId(userId);
    }

    // ===== Utility Methods =====

    @Override
    @Transactional(readOnly = true)
    public Cart findCartItemById(Long cartItemId) {
        return cartRepository.findById(cartItemId)
                .orElseThrow(() -> new CartItemNotFoundException("Cart item not found: " + cartItemId));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Cart> findCartItemByIdOptional(Long cartItemId) {
        return cartRepository.findById(cartItemId);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Cart> findCartItemByUserAndProduct(Long userId, Long productId) {
        return cartRepository.findByUserIdAndProductId(userId, productId);
    }

    @Override
    @Transactional(readOnly = true)
    public Integer getTotalQuantityByUserId(Long userId) {
        Integer total = cartRepository.getTotalQuantityByUserId(userId);
        return total != null ? total : 0;
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal getTotalAmountByUserId(Long userId) {
        BigDecimal total = cartRepository.getTotalAmountByUserId(userId);
        return total != null ? total : BigDecimal.ZERO;
    }

    // ===== Private Helper Methods =====

    /**
     * Add item request validation
     */
    private void validateAddItemRequest(Long userId, CartItemRequest request) {
        if (userId == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }

        if (request == null) {
            throw new IllegalArgumentException("Cart item request cannot be null");
        }

        if (request.getProductId() == null) {
            throw new IllegalArgumentException("Product ID cannot be null");
        }

        if (!validateQuantity(request.getQuantity())) {
            throw new InvalidQuantityException("Invalid quantity: " + request.getQuantity());
        }
    }
}
