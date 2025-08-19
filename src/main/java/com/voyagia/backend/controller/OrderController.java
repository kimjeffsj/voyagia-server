package com.voyagia.backend.controller;

import com.voyagia.backend.dto.common.ApiResponse;
import com.voyagia.backend.dto.order.*;
import com.voyagia.backend.entity.OrderStatus;
import com.voyagia.backend.service.OrderService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * REST Controller for order management
 */
@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private static final Logger logger = LoggerFactory.getLogger(OrderController.class);

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    // ===== Order Creation =====

    /**
     * Create a new order
     * POST /api/orders
     */
    @PostMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<OrderResponse>> createOrder(
            @Valid @RequestBody OrderCreateRequest request) {

        logger.info("Create order request received");

        try {
            Long userId = getCurrentUserId();
            OrderResponse orderResponse = orderService.createOrder(userId, request);

            ApiResponse<OrderResponse> response = ApiResponse.success(
                    "Order created successfully", orderResponse);

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (Exception e) {
            logger.error("Failed to create order: {}", e.getMessage());
            ApiResponse<OrderResponse> response = ApiResponse.error(
                    "Failed to create order: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    /**
     * Create order from cart
     * POST /api/orders/from-cart
     */
    @PostMapping("/from-cart")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<OrderResponse>> createOrderFromCart(
            @Valid @RequestBody OrderCreateRequest request) {

        logger.info("Create order from cart request received");

        try {
            Long userId = getCurrentUserId();
            OrderResponse orderResponse = orderService.createOrderFromCart(userId, request);

            ApiResponse<OrderResponse> response = ApiResponse.success(
                    "Order created successfully from cart", orderResponse);

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (Exception e) {
            logger.error("Failed to create order from cart: {}", e.getMessage());
            ApiResponse<OrderResponse> response = ApiResponse.error(
                    "Failed to create order from cart: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    // ===== Order Retrieval =====

    /**
     * Get order by ID
     * GET /api/orders/{orderId}
     */
    @GetMapping("/{orderId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<OrderResponse>> getOrderById(@PathVariable Long orderId) {
        logger.debug("Get order by ID: {}", orderId);

        try {
            Long userId = getCurrentUserId();

            // Check if user owns the order (for non-admin users)
            if (!isAdmin() && !orderService.validateUserOwnership(userId, orderId)) {
                ApiResponse<OrderResponse> response = ApiResponse.error("Order not found or access denied");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

            OrderResponse orderResponse = orderService.getOrderById(orderId);
            ApiResponse<OrderResponse> response = ApiResponse.success("Order retrieved successfully", orderResponse);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Failed to get order {}: {}", orderId, e.getMessage());
            ApiResponse<OrderResponse> response = ApiResponse.error("Failed to retrieve order: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }

    /**
     * Get order by order number
     * GET /api/orders/number/{orderNumber}
     */
    @GetMapping("/number/{orderNumber}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<OrderResponse>> getOrderByOrderNumber(@PathVariable String orderNumber) {
        logger.debug("Get order by number: {}", orderNumber);

        try {
            OrderResponse orderResponse = orderService.getOrderByOrderNumber(orderNumber);

            // Check ownership for non-admin users
            if (!isAdmin() && !orderResponse.getUserId().equals(getCurrentUserId())) {
                ApiResponse<OrderResponse> response = ApiResponse.error("Order not found or access denied");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

            ApiResponse<OrderResponse> response = ApiResponse.success("Order retrieved successfully", orderResponse);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Failed to get order by number {}: {}", orderNumber, e.getMessage());
            ApiResponse<OrderResponse> response = ApiResponse.error("Failed to retrieve order: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }

    /**
     * Get user's orders
     * GET /api/orders/my-orders
     */
    @GetMapping("/my-orders")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<Page<OrderResponse>>> getMyOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection,
            @RequestParam(required = false) OrderStatus status) {

        logger.debug("Get my orders: page={}, size={}, sort={}", page, size, sortBy);

        try {
            Long userId = getCurrentUserId();
            Pageable pageable = createPageable(page, size, sortBy, sortDirection);

            Page<OrderResponse> orders;
            if (status != null) {
                orders = orderService.getOrdersByUserIdAndStatus(userId, status, pageable);
            } else {
                orders = orderService.getOrdersByUserId(userId, pageable);
            }

            ApiResponse<Page<OrderResponse>> response = ApiResponse.success(
                    "Orders retrieved successfully", orders);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Failed to get user orders: {}", e.getMessage());
            ApiResponse<Page<OrderResponse>> response = ApiResponse.error(
                    "Failed to retrieve orders: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    /**
     * Search orders (Admin only)
     * POST /api/orders/search
     */
    @PostMapping("/search")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Page<OrderResponse>>> searchOrders(
            @Valid @RequestBody OrderSearchRequest request) {

        logger.debug("Search orders request: {}", request);

        try {
            Page<OrderResponse> orders = orderService.searchOrders(request);

            ApiResponse<Page<OrderResponse>> response = ApiResponse.success(
                    "Orders searched successfully", orders);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Failed to search orders: {}", e.getMessage());
            ApiResponse<Page<OrderResponse>> response = ApiResponse.error(
                    "Failed to search orders: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    /**
     * Get all orders (Admin only)
     * GET /api/orders/admin/all
     */
    @GetMapping("/admin/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Page<OrderResponse>>> getAllOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection) {

        logger.debug("Get all orders: page={}, size={}", page, size);

        try {
            Pageable pageable = createPageable(page, size, sortBy, sortDirection);
            Page<OrderResponse> orders = orderService.getAllOrders(pageable);

            ApiResponse<Page<OrderResponse>> response = ApiResponse.success(
                    "All orders retrieved successfully", orders);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Failed to get all orders: {}", e.getMessage());
            ApiResponse<Page<OrderResponse>> response = ApiResponse.error(
                    "Failed to retrieve orders: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    // ===== Order Updates =====

    /**
     * Update order
     * PUT /api/orders/{orderId}
     */
    @PutMapping("/{orderId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<OrderResponse>> updateOrder(
            @PathVariable Long orderId,
            @Valid @RequestBody OrderUpdateRequest request) {

        logger.info("Update order request: orderId={}", orderId);

        try {
            OrderResponse orderResponse = orderService.updateOrder(orderId, request);

            ApiResponse<OrderResponse> response = ApiResponse.success(
                    "Order updated successfully", orderResponse);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Failed to update order {}: {}", orderId, e.getMessage());
            ApiResponse<OrderResponse> response = ApiResponse.error(
                    "Failed to update order: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    /**
     * Update order status
     * PUT /api/orders/{orderId}/status
     */
    @PutMapping("/{orderId}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<OrderResponse>> updateOrderStatus(
            @PathVariable Long orderId,
            @Valid @RequestBody OrderStatusUpdateRequest request) {

        logger.info("Update order status: orderId={}, status={}", orderId, request.getStatus());

        try {
            OrderResponse orderResponse = orderService.updateOrderStatus(orderId, request);

            ApiResponse<OrderResponse> response = ApiResponse.success(
                    "Order status updated successfully", orderResponse);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Failed to update order status {}: {}", orderId, e.getMessage());
            ApiResponse<OrderResponse> response = ApiResponse.error(
                    "Failed to update order status: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    /**
     * Cancel order
     * POST /api/orders/{orderId}/cancel
     */
    @PostMapping("/{orderId}/cancel")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<Void>> cancelOrder(
            @PathVariable Long orderId,
            @RequestParam(required = false) String reason) {

        logger.info("Cancel order request: orderId={}", orderId);

        try {
            Long userId = getCurrentUserId();

            if (isAdmin()) {
                orderService.cancelOrder(orderId, reason);
            } else {
                orderService.cancelOrderByUser(userId, orderId, reason);
            }

            ApiResponse<Void> response = ApiResponse.success("Order cancelled successfully");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Failed to cancel order {}: {}", orderId, e.getMessage());
            ApiResponse<Void> response = ApiResponse.error("Failed to cancel order: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    // ===== Order Processing =====

    /**
     * Process order
     * POST /api/orders/{orderId}/process
     */
    @PostMapping("/{orderId}/process")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<OrderResponse>> processOrder(@PathVariable Long orderId) {
        logger.info("Process order request: orderId={}", orderId);

        try {
            OrderResponse orderResponse = orderService.processOrder(orderId);

            ApiResponse<OrderResponse> response = ApiResponse.success(
                    "Order processed successfully", orderResponse);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Failed to process order {}: {}", orderId, e.getMessage());
            ApiResponse<OrderResponse> response = ApiResponse.error(
                    "Failed to process order: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    /**
     * Ship order
     * POST /api/orders/{orderId}/ship
     */
    @PostMapping("/{orderId}/ship")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<OrderResponse>> shipOrder(
            @PathVariable Long orderId,
            @RequestParam String trackingNumber) {

        logger.info("Ship order request: orderId={}, trackingNumber={}", orderId, trackingNumber);

        try {
            OrderResponse orderResponse = orderService.shipOrder(orderId, trackingNumber);

            ApiResponse<OrderResponse> response = ApiResponse.success(
                    "Order shipped successfully", orderResponse);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Failed to ship order {}: {}", orderId, e.getMessage());
            ApiResponse<OrderResponse> response = ApiResponse.error(
                    "Failed to ship order: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    /**
     * Mark order as delivered
     * POST /api/orders/{orderId}/deliver
     */
    @PostMapping("/{orderId}/deliver")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<OrderResponse>> deliverOrder(@PathVariable Long orderId) {
        logger.info("Deliver order request: orderId={}", orderId);

        try {
            OrderResponse orderResponse = orderService.deliverOrder(orderId);

            ApiResponse<OrderResponse> response = ApiResponse.success(
                    "Order marked as delivered successfully", orderResponse);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Failed to deliver order {}: {}", orderId, e.getMessage());
            ApiResponse<OrderResponse> response = ApiResponse.error(
                    "Failed to deliver order: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    // ===== Payment Management =====

    /**
     * Initialize payment
     * POST /api/orders/{orderId}/payment/initialize
     */
    @PostMapping("/{orderId}/payment/initialize")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<String>> initializePayment(@PathVariable Long orderId) {
        logger.info("Initialize payment request: orderId={}", orderId);

        try {
            Long userId = getCurrentUserId();

            // Check ownership for non-admin users
            if (!isAdmin() && !orderService.validateUserOwnership(userId, orderId)) {
                ApiResponse<String> response = ApiResponse.error("Order not found or access denied");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

            String paymentToken = orderService.initializePayment(orderId);

            ApiResponse<String> response = ApiResponse.success(
                    "Payment initialized successfully", paymentToken);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Failed to initialize payment for order {}: {}", orderId, e.getMessage());
            ApiResponse<String> response = ApiResponse.error(
                    "Failed to initialize payment: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    /**
     * Process payment
     * POST /api/orders/{orderId}/payment/process
     */
    @PostMapping("/{orderId}/payment/process")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<Boolean>> processPayment(
            @PathVariable Long orderId,
            @RequestBody String paymentData) {

        logger.info("Process payment request: orderId={}", orderId);

        try {
            Long userId = getCurrentUserId();

            // Check ownership for non-admin users
            if (!isAdmin() && !orderService.validateUserOwnership(userId, orderId)) {
                ApiResponse<Boolean> response = ApiResponse.error("Order not found or access denied");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

            boolean paymentResult = orderService.processPayment(orderId, paymentData);

            ApiResponse<Boolean> response = ApiResponse.success(
                    "Payment processed successfully", paymentResult);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Failed to process payment for order {}: {}", orderId, e.getMessage());
            ApiResponse<Boolean> response = ApiResponse.error(
                    "Failed to process payment: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    /**
     * Payment callback (webhook)
     * POST /api/orders/payment/callback
     */
    @PostMapping("/payment/callback")
    public ResponseEntity<ApiResponse<Void>> paymentCallback(
            @RequestParam String orderNumber,
            @RequestParam String transactionId,
            @RequestParam String status) {

        logger.info("Payment callback: orderNumber={}, transactionId={}, status={}",
                orderNumber, transactionId, status);

        try {
            orderService.handlePaymentCallback(orderNumber, transactionId, status);

            ApiResponse<Void> response = ApiResponse.success("Payment callback processed successfully");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Failed to process payment callback: {}", e.getMessage());
            ApiResponse<Void> response = ApiResponse.error(
                    "Failed to process payment callback: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    // ===== Statistics and Reports (Admin only) =====

    /**
     * Get order statistics
     * GET /api/orders/admin/stats
     */
    @GetMapping("/admin/stats")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<OrderStatsResponse>> getOrderStats(
            @RequestParam(required = false) LocalDateTime startDate,
            @RequestParam(required = false) LocalDateTime endDate) {

        logger.debug("Get order stats request");

        try {
            // Default to last 30 days if no dates provided
            if (startDate == null) {
                startDate = LocalDateTime.now().minusDays(30);
            }
            if (endDate == null) {
                endDate = LocalDateTime.now();
            }

            // Create stats response
            OrderStatsResponse stats = new OrderStatsResponse();
            stats.setTotalRevenue(orderService.getTotalRevenue(startDate, endDate));
            stats.setPendingOrders(orderService.countOrdersByStatus(OrderStatus.PENDING));
            stats.setProcessingOrders(orderService.countOrdersByStatus(OrderStatus.PROCESSING));
            stats.setShippedOrders(orderService.countOrdersByStatus(OrderStatus.SHIPPED));
            stats.setDeliveredOrders(orderService.countOrdersByStatus(OrderStatus.DELIVERED));
            stats.setCancelledOrders(orderService.countOrdersByStatus(OrderStatus.CANCELLED));

            ApiResponse<OrderStatsResponse> response = ApiResponse.success(
                    "Order statistics retrieved successfully", stats);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Failed to get order stats: {}", e.getMessage());
            ApiResponse<OrderStatsResponse> response = ApiResponse.error(
                    "Failed to retrieve order statistics: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    // ===== Utility Methods =====

    /**
     * Get current authenticated user ID
     */
    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.isAuthenticated()) {
            // Assuming the authentication principal contains the user ID
            // This would depend on how your JWT authentication is set up
            return Long.parseLong(authentication.getName());
        }

        throw new RuntimeException("User not authenticated");
    }

    /**
     * Check if current user is admin
     */
    private boolean isAdmin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null &&
                authentication.getAuthorities().stream()
                        .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"));
    }

    /**
     * Create Pageable from parameters
     */
    private Pageable createPageable(int page, int size, String sortBy, String sortDirection) {
        Sort.Direction direction = "DESC".equalsIgnoreCase(sortDirection) ?
                Sort.Direction.DESC : Sort.Direction.ASC;

        return PageRequest.of(Math.max(0, page), Math.min(Math.max(1, size), 100),
                Sort.by(direction, sortBy));
    }

    /**
     * Inner class for order statistics response
     */
    public static class OrderStatsResponse {
        private BigDecimal totalRevenue;
        private long pendingOrders;
        private long processingOrders;
        private long shippedOrders;
        private long deliveredOrders;
        private long cancelledOrders;

        // Getters and Setters
        public BigDecimal getTotalRevenue() {
            return totalRevenue;
        }

        public void setTotalRevenue(BigDecimal totalRevenue) {
            this.totalRevenue = totalRevenue;
        }

        public long getPendingOrders() {
            return pendingOrders;
        }

        public void setPendingOrders(long pendingOrders) {
            this.pendingOrders = pendingOrders;
        }

        public long getProcessingOrders() {
            return processingOrders;
        }

        public void setProcessingOrders(long processingOrders) {
            this.processingOrders = processingOrders;
        }

        public long getShippedOrders() {
            return shippedOrders;
        }

        public void setShippedOrders(long shippedOrders) {
            this.shippedOrders = shippedOrders;
        }

        public long getDeliveredOrders() {
            return deliveredOrders;
        }

        public void setDeliveredOrders(long deliveredOrders) {
            this.deliveredOrders = deliveredOrders;
        }

        public long getCancelledOrders() {
            return cancelledOrders;
        }

        public void setCancelledOrders(long cancelledOrders) {
            this.cancelledOrders = cancelledOrders;
        }
    }
}