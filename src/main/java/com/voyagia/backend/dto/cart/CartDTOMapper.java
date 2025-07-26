package com.voyagia.backend.dto.cart;

import com.voyagia.backend.entity.Cart;
import com.voyagia.backend.entity.Product;
import com.voyagia.backend.entity.User;
import com.voyagia.backend.service.ProductService;
import com.voyagia.backend.service.UserService;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Cart DTO Mapper
 */
public class CartDTOMapper {

    private final UserService userService;
    private final ProductService productService;

    public CartDTOMapper(UserService userService, ProductService productService) {
        this.userService = userService;
        this.productService = productService;
    }

    /**
     * Cart Entity to CartItemResponse
     */
    public CartItemResponse toCartItemResponse(Cart cart) {
        if (cart == null) {
            return null;
        }

        return new CartItemResponse(cart);
    }

    /**
     * Cart Entity List to CartItemResponse DTO list
     */
    public List<CartItemResponse> toCartItemResponseList(List<Cart> carts) {
        if (carts == null) {
            return null;
        }

        return carts.stream()
                .map(this::toCartItemResponse)
                .collect(Collectors.toList());
    }

    /**
     * Cart Entity List to CartResponse DTO
     */
    public CartResponse toCartResponse(List<Cart> carts, Long userId) {
        if (carts == null) {
            return null;
        }

        List<CartItemResponse> items = toCartItemResponseList(carts);
        CartResponse response = new CartResponse(userId, items);

        // Set user information if available
        if (userId != null) {
            userService.findByIdOptional(userId).ifPresent(user -> {
                response.setUserEmail(user.getEmail());
                response.setUserFullName(user.getFullName());
            });
        }

        return response;
    }

    /**
     * CartItemRequest DTO to Cart Entity
     */
    public Cart toCartEntity(CartItemRequest request, Long userId) {
        if (request == null || userId == null) {
            return null;
        }

        User user = userService.findById(userId);
        Product product = productService.findById(request.getProductId());

        Cart cart = new Cart();
        cart.setUser(user);
        cart.setProduct(product);
        cart.setQuantity(request.getQuantity());
        cart.setUnitPrice(product.getPrice()); // Set to current price

        return cart;
    }

    /**
     * Update Cart Entity with CartItemRequest
     */
    public void updateCartEntity(Cart cart, CartItemRequest request) {
        if (cart == null || request == null) {
            return;
        }

        // Update quantity
        if (request.getQuantity() != null) {
            cart.setQuantity(request.getQuantity());
        }

        // Price might be changed, use current price
        if (cart.getProduct() != null) {
            cart.updateFromProduct();
        }
    }

    /**
     * Cart summary
     */
    public CartResponse.CartSummary toCartSummary(List<Cart> carts) {
        if (carts == null || carts.isEmpty()) {
            return new CartResponse.CartSummary(0, 0, java.math.BigDecimal.ZERO);
        }

        int itemCount = carts.size();
        int totalQuantity = carts.stream()
                .mapToInt(Cart::getQuantity)
                .sum();
        java.math.BigDecimal totalAmount = carts.stream()
                .map(Cart::getTotalPrice)
                .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);

        return new CartResponse.CartSummary(itemCount, totalQuantity, totalAmount);
    }

    /**
     * CartItemResponse summary
     */
    public CartItemResponse toSimpleCartItemResponse(Cart cart) {
        if (cart == null) {
            return null;
        }

        CartItemResponse response = new CartItemResponse();
        response.setId(cart.getId());
        response.setQuantity(cart.getQuantity());
        response.setUnitPrice(cart.getUnitPrice());
        response.setTotalPrice(cart.getTotalPrice());

        if (cart.getProduct() != null) {
            Product product = cart.getProduct();
            response.setProductId(product.getId());
            response.setProductName(product.getName());
            response.setProductSlug(product.getSlug());
            response.setProductSku(product.getSku());
            response.setProductMainImageUrl(product.getMainImageUrl());
            response.setAvailableStock(product.getStockQuantity());
            response.setInStock(product.isInStock());
        }

        return response;
    }

    /**
     * CartItemResponse List summary
     */
    public List<CartItemResponse> toSimpleCartItemResponseList(List<Cart> carts) {
        if (carts == null) {
            return null;
        }

        return carts.stream()
                .map(this::toSimpleCartItemResponse)
                .collect(Collectors.toList());
    }
}
