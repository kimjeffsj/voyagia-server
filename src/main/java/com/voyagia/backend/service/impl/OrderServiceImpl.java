package com.voyagia.backend.service.impl;

import com.voyagia.backend.dto.order.*;
import com.voyagia.backend.entity.*;
import com.voyagia.backend.exception.*;
import com.voyagia.backend.repository.OrderRepository;
import com.voyagia.backend.service.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

/**
 * Implementation of OrderService for comprehensive order management
 */
@Service
@Transactional
public class OrderServiceImpl implements OrderService {

    private static final Logger logger = LoggerFactory.getLogger(OrderServiceImpl.class);

    // Business Constants
    private static final BigDecimal DEFAULT_TAX_RATE = new BigDecimal("0.10"); // 10% tax
    private static final BigDecimal DEFAULT_SHIPPING_RATE = new BigDecimal("15.00"); // $15 flat rate
    private static final BigDecimal FREE_SHIPPING_THRESHOLD = new BigDecimal("100.00"); // Free shipping over $100
    private static final int MAX_ORDER_ITEMS = 50;
    private static final int MAX_QUANTITY_PER_ITEM = 999;
    private static final BigDecimal MAX_ORDER_AMOUNT = new BigDecimal("50000.00"); // $50,000 limit

    // Dependencies
    private final OrderRepository orderRepository;
    private final UserService userService;
    private final ProductService productService;
    private final CartService cartService;
    private final OrderDTOMapper orderDTOMapper;

    public OrderServiceImpl(OrderRepository orderRepository,
            UserService userService,
            ProductService productService,
            CartService cartService,
            OrderDTOMapper orderDTOMapper) {
        this.orderRepository = orderRepository;
        this.userService = userService;
        this.productService = productService;
        this.cartService = cartService;
        this.orderDTOMapper = orderDTOMapper;
    }

    // ===== Order Creation and Management =====

    @Override
    @Transactional
    public OrderResponse createOrder(Long userId, OrderCreateRequest request) {
        logger.info("Creating order for user: {}", userId);

        try {
            // 1. Validate user
            User user = userService.findById(userId);

            // 2. Validate request
            validateOrderCreateRequest(request);

            // 3. Check user eligibility
            if (!checkUserEligibility(userId)) {
                throw new InvalidOrderException("User is not eligible to place orders");
            }

            // 4. Determine order items source
            List<OrderCreateRequest.OrderItemRequest> orderItems;
            if (request.hasCustomOrderItems()) {
                orderItems = request.getOrderItems();
                logger.debug("Using custom order items: {} items", orderItems.size());
            } else {
                // Convert cart items to order items
                orderItems = getOrderItemsFromCart(userId);
                logger.debug("Using cart items: {} items", orderItems.size());
            }

            // 5. Validate order items
            if (orderItems.isEmpty()) {
                throw new InvalidOrderException("Order must contain at least one item");
            }

            if (!validateOrderItems(orderItems)) {
                throw new InvalidOrderException("Order contains invalid items");
            }

            // 6. Enforce order limits
            if (!enforceOrderLimits(userId, request)) {
                throw new InvalidOrderException("Order exceeds allowed limits");
            }

            // 7. Create order entity
            Order order = createOrderEntity(user, request, orderItems);

            // 8. Save order
            Order savedOrder = orderRepository.save(order);

            // 9. Reserve inventory
            reserveInventory(savedOrder.getId());

            // 10. Clear cart if using cart items
            if (!request.hasCustomOrderItems()) {
                cartService.clearCart(userId);
                logger.debug("Cart cleared for user: {}", userId);
            }

            logger.info("Order created successfully: orderNumber={}, orderId={}",
                    savedOrder.getOrderNumber(), savedOrder.getId());

            return orderDTOMapper.toOrderResponse(savedOrder);

        } catch (Exception e) {
            logger.error("Failed to create order for user: {}, error: {}", userId, e.getMessage(), e);
            throw e;
        }
    }

    @Override
    @Transactional
    public OrderResponse createOrderFromCart(Long userId, OrderCreateRequest request) {
        logger.info("Creating order from cart for user: {}", userId);

        // Ensure request doesn't have custom items
        request.setOrderItems(null);

        return createOrder(userId, request);
    }

    @Override
    @Transactional
    public OrderResponse updateOrder(Long orderId, OrderUpdateRequest request) {
        logger.info("Updating order: {}", orderId);

        Order order = findOrderById(orderId);

        // Check if order can be updated
        if (order.getStatus().isFinal()) {
            throw new InvalidOrderException("Cannot update order in final status: " + order.getStatus());
        }

        // Update order fields
        orderDTOMapper.updateOrderFromRequest(order, request);

        Order savedOrder = orderRepository.save(order);
        logger.info("Order updated successfully: {}", orderId);

        return orderDTOMapper.toOrderResponse(savedOrder);
    }

    @Override
    @Transactional
    public OrderResponse updateOrderStatus(Long orderId, OrderStatusUpdateRequest request) {
        logger.info("Updating order status: orderId={}, newStatus={}", orderId, request.getStatus());

        Order order = findOrderById(orderId);
        OrderStatus currentStatus = order.getStatus();
        OrderStatus newStatus = request.getStatus();

        // Validate status transition
        if (!isValidStatusTransition(currentStatus, newStatus)) {
            throw new InvalidOrderException(
                    String.format("Invalid status transition from %s to %s", currentStatus, newStatus));
        }

        // Check if reason is required
        if (request.requiresReason() && !request.hasReason()) {
            throw new InvalidOrderException("Reason is required for this status change");
        }

        // Update status and related fields
        updateOrderStatusAndTimestamps(order, request);

        // Handle payment status update
        if (request.hasPaymentUpdate()) {
            updatePaymentInformation(order, request);
        }

        // Handle tracking information
        if (request.hasTrackingUpdate()) {
            order.setTrackingNumber(request.getTrackingNumber());
        }

        Order savedOrder = orderRepository.save(order);
        logger.info("Order status updated successfully: orderId={}, status={}", orderId, newStatus);

        return orderDTOMapper.toOrderResponse(savedOrder);
    }

    @Override
    @Transactional
    public void cancelOrder(Long orderId, String reason) {
        logger.info("Cancelling order: orderId={}, reason={}", orderId, reason);

        Order order = findOrderById(orderId);

        if (!order.getStatus().canBeCancelled()) {
            throw new InvalidOrderException("Order cannot be cancelled in current status: " + order.getStatus());
        }

        // Release inventory
        releaseInventory(orderId);

        // Update order status
        order.setStatus(OrderStatus.CANCELLED);
        order.setCancelReason(reason);
        order.setCancelledAt(LocalDateTime.now());

        orderRepository.save(order);
        logger.info("Order cancelled successfully: {}", orderId);
    }

    @Override
    @Transactional
    public void cancelOrderByUser(Long userId, Long orderId, String reason) {
        logger.info("User {} cancelling order: {}", userId, orderId);

        if (!validateUserOwnership(userId, orderId)) {
            throw new OrderNotFoundException("Order not found or access denied");
        }

        cancelOrder(orderId, reason);
    }

    // ===== Order Retrieval =====

    @Override
    @Transactional(readOnly = true)
    public OrderResponse getOrderById(Long orderId) {
        Order order = findOrderById(orderId);
        return orderDTOMapper.toOrderResponse(order);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<OrderResponse> getOrderByIdOptional(Long orderId) {
        Optional<Order> orderOpt = findOrderByIdOptional(orderId);
        return orderOpt.map(orderDTOMapper::toOrderResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse getOrderByOrderNumber(String orderNumber) {
        logger.debug("Finding order by number: {}", orderNumber);

        Order order = orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new OrderNotFoundException(orderNumber, true));

        return orderDTOMapper.toOrderResponse(order);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OrderResponse> getOrdersByUserId(Long userId, Pageable pageable) {
        logger.debug("Finding orders for user: {}", userId);

        // Validate user exists
        userService.findById(userId);

        Page<Order> orders = orderRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
        return orders.map(orderDTOMapper::toOrderSummaryResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OrderResponse> getOrdersByUserIdAndStatus(Long userId, OrderStatus status, Pageable pageable) {
        logger.debug("Finding orders for user: {} with status: {}", userId, status);

        // Validate user exists
        userService.findById(userId);

        Page<Order> orders = orderRepository.findByUserIdAndStatus(userId, status, pageable);
        return orders.map(orderDTOMapper::toOrderSummaryResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OrderResponse> getAllOrders(Pageable pageable) {
        logger.debug("Finding all orders with pagination");

        Page<Order> orders = orderRepository.findAll(pageable);
        return orders.map(orderDTOMapper::toOrderSummaryResponse);
    }

    // ===== Order Processing =====

    @Override
    @Transactional
    public OrderResponse processOrder(Long orderId) {
        logger.info("Processing order: {}", orderId);

        Order order = findOrderById(orderId);

        if (order.getStatus() != OrderStatus.PENDING) {
            throw new OrderProcessingException("Order cannot be processed in current status",
                    order.getOrderNumber(), "processing");
        }

        // Update status to CONFIRMED
        order.setStatus(OrderStatus.CONFIRMED);

        Order savedOrder = orderRepository.save(order);
        logger.info("Order processed successfully: {}", orderId);

        return orderDTOMapper.toOrderResponse(savedOrder);
    }

    @Override
    @Transactional
    public OrderResponse shipOrder(Long orderId, String trackingNumber) {
        logger.info("Shipping order: orderId={}, trackingNumber={}", orderId, trackingNumber);

        Order order = findOrderById(orderId);

        if (order.getStatus() != OrderStatus.PROCESSING && order.getStatus() != OrderStatus.CONFIRMED) {
            throw new InvalidOrderException("Order cannot be shipped in current status: " + order.getStatus());
        }

        // Update order
        order.setStatus(OrderStatus.SHIPPED);
        order.setTrackingNumber(trackingNumber);
        order.setShippedAt(LocalDateTime.now());

        Order savedOrder = orderRepository.save(order);
        logger.info("Order shipped successfully: {}", orderId);

        return orderDTOMapper.toOrderResponse(savedOrder);
    }

    @Override
    @Transactional
    public OrderResponse deliverOrder(Long orderId) {
        logger.info("Delivering order: {}", orderId);

        Order order = findOrderById(orderId);

        if (order.getStatus() != OrderStatus.SHIPPED) {
            throw new InvalidOrderException("Order cannot be delivered in current status: " + order.getStatus());
        }

        // Update order
        order.setStatus(OrderStatus.DELIVERED);
        order.setDeliveredAt(LocalDateTime.now());

        Order savedOrder = orderRepository.save(order);
        logger.info("Order delivered successfully: {}", orderId);

        return orderDTOMapper.toOrderResponse(savedOrder);
    }

    // ===== Utility Methods =====

    @Override
    @Transactional(readOnly = true)
    public Order findOrderById(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Order> findOrderByIdOptional(Long orderId) {
        return orderRepository.findById(orderId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean validateUserOwnership(Long userId, Long orderId) {
        Optional<Order> orderOpt = findOrderByIdOptional(orderId);
        return orderOpt.isPresent() && orderOpt.get().getUser().getId().equals(userId);
    }

    @Override
    public String generateOrderNumber() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        int random = ThreadLocalRandom.current().nextInt(1000, 9999);
        return "ORD-" + timestamp + "-" + random;
    }

    // ===== Private Helper Methods =====

    /**
     * Validate order creation request
     */
    private void validateOrderCreateRequest(OrderCreateRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Order request cannot be null");
        }

        // Additional business validations can be added here
        if (request.hasCustomOrderItems() && request.getOrderItems().size() > MAX_ORDER_ITEMS) {
            throw new InvalidOrderException("Order cannot contain more than " + MAX_ORDER_ITEMS + " items");
        }
    }

    /**
     * Convert cart items to order item requests
     */
    private List<OrderCreateRequest.OrderItemRequest> getOrderItemsFromCart(Long userId) {
        // Check if cart is empty
        if (cartService.isCartEmpty(userId)) {
            throw new InvalidOrderException("Cart is empty");
        }

        // Get cart items and convert to order items
        var cartItems = cartService.getCartItems(userId);

        return cartItems.stream()
                .map(cartItem -> {
                    var orderItem = new OrderCreateRequest.OrderItemRequest();
                    orderItem.setProductId(cartItem.getProductId());
                    orderItem.setQuantity(cartItem.getQuantity());
                    // Use current product price, not cart price
                    return orderItem;
                })
                .collect(Collectors.toList());
    }

    /**
     * Create order entity from request and items
     */
    private Order createOrderEntity(User user, OrderCreateRequest request,
            List<OrderCreateRequest.OrderItemRequest> orderItems) {

        // Create base order from request
        Order order = orderDTOMapper.createOrderFromRequest(request);

        // Set user and order number
        order.setUser(user);
        order.setOrderNumber(generateOrderNumber());
        order.setStatus(OrderStatus.PENDING);
        order.setPaymentStatus(PaymentStatus.PENDING);

        // Create order items
        List<OrderItem> entityOrderItems = new ArrayList<>();

        for (OrderCreateRequest.OrderItemRequest itemRequest : orderItems) {
            OrderItem orderItem = createOrderItemEntity(order, itemRequest);
            entityOrderItems.add(orderItem);
        }

        order.setOrderItems(entityOrderItems);

        // Calculate pricing
        calculateOrderPricing(order, request);

        return order;
    }

    /**
     * Create order item entity from request
     */
    private OrderItem createOrderItemEntity(Order order, OrderCreateRequest.OrderItemRequest itemRequest) {
        Product product = productService.findById(itemRequest.getProductId());

        // Use custom price if provided, otherwise use product price
        BigDecimal unitPrice = itemRequest.hasCustomPrice() ? itemRequest.getUnitPrice() : product.getPrice();

        OrderItem orderItem = new OrderItem(order, product, itemRequest.getQuantity(), unitPrice);

        return orderItem;
    }

    /**
     * Calculate order pricing
     */
    private void calculateOrderPricing(Order order, OrderCreateRequest request) {
        // Calculate subtotal
        BigDecimal subtotal = order.getOrderItems().stream()
                .map(OrderItem::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        order.setSubtotal(subtotal);

        // Calculate or use provided tax
        BigDecimal taxAmount = request.getTaxAmount() != null ? request.getTaxAmount()
                : calculateTax(subtotal, order.getShippingCountry());
        order.setTaxAmount(taxAmount);

        // Calculate or use provided shipping
        BigDecimal shippingAmount = request.getShippingAmount() != null ? request.getShippingAmount()
                : calculateShipping(null, order.getShippingCountry());
        order.setShippingAmount(shippingAmount);

        // Use provided discount or zero
        BigDecimal discountAmount = request.getDiscountAmount() != null ? request.getDiscountAmount() : BigDecimal.ZERO;
        order.setDiscountAmount(discountAmount);

        // Calculate total
        BigDecimal totalAmount = subtotal.add(taxAmount).add(shippingAmount).subtract(discountAmount);
        order.setTotalAmount(totalAmount);
    }

    /**
     * Validate status transition
     */
    private boolean isValidStatusTransition(OrderStatus from, OrderStatus to) {
        if (from == to)
            return true;

        return switch (from) {
            case PENDING -> to == OrderStatus.CONFIRMED || to == OrderStatus.CANCELLED;
            case CONFIRMED -> to == OrderStatus.PROCESSING || to == OrderStatus.CANCELLED;
            case PROCESSING -> to == OrderStatus.SHIPPED || to == OrderStatus.CANCELLED;
            case SHIPPED -> to == OrderStatus.DELIVERED;
            case DELIVERED -> false; // Final state
            case CANCELLED -> false; // Final state
            case REFUNDED -> false; // Final state
        };
    }

    /**
     * Update order status and related timestamps
     */
    private void updateOrderStatusAndTimestamps(Order order, OrderStatusUpdateRequest request) {
        OrderStatus newStatus = request.getStatus();
        LocalDateTime now = LocalDateTime.now();

        order.setStatus(newStatus);

        switch (newStatus) {
            case PENDING -> {
                // No special timestamp handling needed for PENDING
            }
            case CONFIRMED -> {
                // No special timestamp handling needed for CONFIRMED
            }
            case PROCESSING -> {
                // No special timestamp handling needed for PROCESSING
            }
            case SHIPPED -> {
                if (order.getShippedAt() == null) {
                    order.setShippedAt(now);
                }
            }
            case DELIVERED -> {
                if (order.getDeliveredAt() == null) {
                    order.setDeliveredAt(now);
                }
            }
            case CANCELLED -> {
                if (order.getCancelledAt() == null) {
                    order.setCancelledAt(now);
                }
                if (request.hasReason()) {
                    order.setCancelReason(request.getReason());
                }
            }
            case REFUNDED -> {
                // No special timestamp handling needed for REFUNDED
            }
        }
    }

    /**
     * Update payment information
     */
    private void updatePaymentInformation(Order order, OrderStatusUpdateRequest request) {
        if (request.getPaymentStatus() != null) {
            order.setPaymentStatus(request.getPaymentStatus());

            if (request.getPaymentStatus() == PaymentStatus.PAID && order.getPaidAt() == null) {
                order.setPaidAt(LocalDateTime.now());
            }
        }

        if (request.getPaymentTransactionId() != null) {
            order.setPaymentTransactionId(request.getPaymentTransactionId());
        }
    }

    // ===== Order Search Implementation =====

    @Override
    @Transactional(readOnly = true)
    public Page<OrderResponse> searchOrders(OrderSearchRequest request) {
        logger.debug("Searching orders with criteria: {}", request);

        try {
            // For now, implement basic search functionality
            // In a production environment, this would use Spring Data JPA Specifications
            // or a more sophisticated search engine

            if (request.hasSearchTerm()) {
                Page<Order> orders = orderRepository.searchOrders(request.getSearchTerm(),
                        createPageable(request));
                return orders.map(request.isIncludeOrderItems() ? orderDTOMapper::toOrderResponse
                        : orderDTOMapper::toOrderSummaryResponse);
            }

            if (request.hasUserFilter() && request.getUserId() != null) {
                Page<Order> orders = orderRepository.findByUserIdOrderByCreatedAtDesc(
                        request.getUserId(), createPageable(request));
                return orders.map(request.isIncludeOrderItems() ? orderDTOMapper::toOrderResponse
                        : orderDTOMapper::toOrderSummaryResponse);
            }

            if (request.hasStatusFilter() && !request.getStatuses().isEmpty()) {
                // For simplicity, search by first status
                Page<Order> orders = orderRepository.findByStatusOrderByCreatedAtDesc(
                        request.getStatuses().get(0), createPageable(request));
                return orders.map(request.isIncludeOrderItems() ? orderDTOMapper::toOrderResponse
                        : orderDTOMapper::toOrderSummaryResponse);
            }

            if (request.hasDateFilter()) {
                LocalDateTime startDate = request.getCreatedFrom() != null ? request.getCreatedFrom()
                        : LocalDateTime.now().minusYears(1);
                LocalDateTime endDate = request.getCreatedTo() != null ? request.getCreatedTo() : LocalDateTime.now();

                Page<Order> orders = orderRepository.findByDateRange(startDate, endDate, createPageable(request));
                return orders.map(request.isIncludeOrderItems() ? orderDTOMapper::toOrderResponse
                        : orderDTOMapper::toOrderSummaryResponse);
            }

            // Default: return all orders
            return getAllOrders(createPageable(request));

        } catch (Exception e) {
            logger.error("Error searching orders: {}", e.getMessage(), e);
            throw new OrderProcessingException("Failed to search orders", null, "search");
        }
    }

    // ===== Payment Management Implementation =====

    @Override
    @Transactional
    public String initializePayment(Long orderId) {
        logger.info("Initializing payment for order: {}", orderId);

        Order order = findOrderById(orderId);

        if (order.getPaymentStatus() != PaymentStatus.PENDING) {
            throw new PaymentFailedException("Payment already processed for order: " + orderId);
        }

        // Generate a mock payment token for demonstration
        // In production, this would integrate with a real payment processor
        String paymentToken = "PAY_" + order.getOrderNumber() + "_" +
                System.currentTimeMillis();

        logger.info("Payment initialized for order: {}, token: {}", orderId, paymentToken);
        return paymentToken;
    }

    @Override
    @Transactional
    public boolean processPayment(Long orderId, String paymentData) {
        logger.info("Processing payment for order: {}", orderId);

        Order order = findOrderById(orderId);

        try {
            // Mock payment processing
            // In production, this would call external payment service
            boolean paymentSuccess = mockPaymentProcessing(order, paymentData);

            if (paymentSuccess) {
                order.setPaymentStatus(PaymentStatus.PAID);
                order.setPaidAt(LocalDateTime.now());
                order.setPaymentTransactionId("TXN_" + System.currentTimeMillis());

                // Auto-advance order status if payment successful
                if (order.getStatus() == OrderStatus.PENDING) {
                    order.setStatus(OrderStatus.CONFIRMED);
                }

                orderRepository.save(order);
                logger.info("Payment processed successfully for order: {}", orderId);
                return true;
            } else {
                order.setPaymentStatus(PaymentStatus.FAILED);
                orderRepository.save(order);
                throw new PaymentFailedException("Payment processing failed for order: " + orderId);
            }

        } catch (Exception e) {
            logger.error("Payment processing failed for order: {}, error: {}", orderId, e.getMessage());
            order.setPaymentStatus(PaymentStatus.FAILED);
            orderRepository.save(order);
            throw new PaymentFailedException("Payment processing failed", e);
        }
    }

    @Override
    @Transactional
    public void handlePaymentCallback(String orderNumber, String transactionId, String status) {
        logger.info("Handling payment callback for order: {}, transaction: {}, status: {}",
                orderNumber, transactionId, status);

        try {
            Order order = orderRepository.findByOrderNumber(orderNumber)
                    .orElseThrow(() -> new OrderNotFoundException(orderNumber, true));

            // Update payment information based on callback
            order.setPaymentTransactionId(transactionId);

            switch (status.toUpperCase()) {
                case "SUCCESS", "COMPLETED", "PAID" -> {
                    order.setPaymentStatus(PaymentStatus.PAID);
                    order.setPaidAt(LocalDateTime.now());
                    if (order.getStatus() == OrderStatus.PENDING) {
                        order.setStatus(OrderStatus.CONFIRMED);
                    }
                }
                case "FAILED", "CANCELLED", "DECLINED" -> {
                    order.setPaymentStatus(PaymentStatus.FAILED);
                }
                case "PENDING", "PROCESSING" -> {
                    order.setPaymentStatus(PaymentStatus.PROCESSING);
                }
                default -> {
                    logger.warn("Unknown payment status received: {}", status);
                    return;
                }
            }

            orderRepository.save(order);
            logger.info("Payment callback processed successfully for order: {}", orderNumber);

        } catch (Exception e) {
            logger.error("Failed to process payment callback: {}", e.getMessage(), e);
            throw new PaymentFailedException("Payment callback processing failed", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public boolean validatePaymentStatus(Long orderId) {
        logger.debug("Validating payment status for order: {}", orderId);

        Order order = findOrderById(orderId);

        // Mock validation - in production, this would verify with payment processor
        return order.getPaymentStatus() == PaymentStatus.PAID &&
                order.getPaymentTransactionId() != null;
    }

    // ===== Inventory Management Implementation =====

    @Override
    @Transactional(readOnly = true)
    public boolean validateOrderItems(List<OrderCreateRequest.OrderItemRequest> orderItems) {
        logger.debug("Validating {} order items", orderItems.size());

        if (orderItems == null || orderItems.isEmpty()) {
            return false;
        }

        if (orderItems.size() > MAX_ORDER_ITEMS) {
            logger.warn("Order contains too many items: {} (max: {})", orderItems.size(), MAX_ORDER_ITEMS);
            return false;
        }

        for (OrderCreateRequest.OrderItemRequest item : orderItems) {
            // Validate item data
            if (item.getProductId() == null || item.getQuantity() == null) {
                logger.warn("Invalid order item: missing product ID or quantity");
                return false;
            }

            if (item.getQuantity() <= 0 || item.getQuantity() > MAX_QUANTITY_PER_ITEM) {
                logger.warn("Invalid quantity for product {}: {}", item.getProductId(), item.getQuantity());
                return false;
            }

            // Validate product exists and is active
            try {
                Product product = productService.findById(item.getProductId());
                if (!product.getIsActive()) {
                    logger.warn("Product is not active: {}", item.getProductId());
                    return false;
                }

                // Check stock availability
                if (!checkStockAvailability(item.getProductId(), item.getQuantity())) {
                    logger.warn("Insufficient stock for product {}: requested {}",
                            item.getProductId(), item.getQuantity());
                    return false;
                }

            } catch (ProductNotFoundException e) {
                logger.warn("Product not found: {}", item.getProductId());
                return false;
            }
        }

        return true;
    }

    @Override
    @Transactional
    public void reserveInventory(Long orderId) {
        logger.info("Reserving inventory for order: {}", orderId);

        Order order = findOrderById(orderId);

        try {
            for (OrderItem orderItem : order.getOrderItems()) {
                Product product = orderItem.getProduct();
                Integer requestedQuantity = orderItem.getQuantity();

                // Check current stock
                if (!productService.hasEnoughStock(product.getId(), requestedQuantity)) {
                    throw new InsufficientStockException(
                            String.format("Insufficient stock for product %s: requested %d",
                                    product.getName(), requestedQuantity));
                }

                // Reserve stock (in production, this would update a reserved_stock field)
                // For now, we'll just log the reservation
                logger.debug("Reserved {} units of product {} for order {}",
                        requestedQuantity, product.getId(), orderId);
            }

            logger.info("Inventory reserved successfully for order: {}", orderId);

        } catch (Exception e) {
            logger.error("Failed to reserve inventory for order: {}, error: {}", orderId, e.getMessage());
            throw new OrderProcessingException("Inventory reservation failed",
                    order.getOrderNumber(), "inventory_reservation");
        }
    }

    @Override
    @Transactional
    public void releaseInventory(Long orderId) {
        logger.info("Releasing inventory for order: {}", orderId);

        Order order = findOrderById(orderId);

        try {
            for (OrderItem orderItem : order.getOrderItems()) {
                Product product = orderItem.getProduct();
                Integer quantityToRelease = orderItem.getQuantity();

                // Release reserved stock (in production, this would update inventory)
                logger.debug("Released {} units of product {} for order {}",
                        quantityToRelease, product.getId(), orderId);
            }

            logger.info("Inventory released successfully for order: {}", orderId);

        } catch (Exception e) {
            logger.error("Failed to release inventory for order: {}, error: {}", orderId, e.getMessage());
            // Don't throw exception for inventory release failures
        }
    }

    @Override
    @Transactional(readOnly = true)
    public boolean checkStockAvailability(Long productId, Integer quantity) {
        try {
            return productService.hasEnoughStock(productId, quantity);
        } catch (Exception e) {
            logger.warn("Error checking stock availability for product {}: {}", productId, e.getMessage());
            return false;
        }
    }

    // ===== Price Calculation Implementation =====

    @Override
    @Transactional(readOnly = true)
    public BigDecimal calculateOrderSubtotal(List<OrderCreateRequest.OrderItemRequest> orderItems) {
        logger.debug("Calculating subtotal for {} items", orderItems.size());

        BigDecimal subtotal = BigDecimal.ZERO;

        for (OrderCreateRequest.OrderItemRequest item : orderItems) {
            try {
                Product product = productService.findById(item.getProductId());

                BigDecimal unitPrice = item.hasCustomPrice() ? item.getUnitPrice() : product.getPrice();

                BigDecimal itemTotal = unitPrice.multiply(BigDecimal.valueOf(item.getQuantity()));
                subtotal = subtotal.add(itemTotal);

            } catch (ProductNotFoundException e) {
                logger.warn("Product not found when calculating subtotal: {}", item.getProductId());
                throw new InvalidOrderException("Invalid product in order: " + item.getProductId());
            }
        }

        return subtotal.setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    public BigDecimal calculateTax(BigDecimal subtotal, String shippingAddress) {
        // Enhanced tax calculation based on location
        BigDecimal taxRate = DEFAULT_TAX_RATE;

        // In production, this would use a tax service or lookup table
        if (shippingAddress != null) {
            if (shippingAddress.toLowerCase().contains("alberta")) {
                taxRate = new BigDecimal("0.05"); // 5% GST
            } else if (shippingAddress.toLowerCase().contains("ontario")) {
                taxRate = new BigDecimal("0.13"); // 13% HST
            } else if (shippingAddress.toLowerCase().contains("quebec")) {
                taxRate = new BigDecimal("0.14975"); // 14.975% GST+QST
            }
            // Add more provinces as needed
        }

        return subtotal.multiply(taxRate).setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    public BigDecimal calculateShipping(List<OrderCreateRequest.OrderItemRequest> orderItems, String shippingAddress) {
        // Calculate shipping based on order value and destination
        BigDecimal subtotal = orderItems != null ? calculateOrderSubtotal(orderItems) : BigDecimal.ZERO;

        // Free shipping over threshold
        if (subtotal.compareTo(FREE_SHIPPING_THRESHOLD) >= 0) {
            return BigDecimal.ZERO;
        }

        // Basic shipping calculation
        BigDecimal shippingAmount = DEFAULT_SHIPPING_RATE;

        // In production, this would integrate with shipping carriers
        if (shippingAddress != null && shippingAddress.toLowerCase().contains("remote")) {
            shippingAmount = DEFAULT_SHIPPING_RATE.multiply(new BigDecimal("1.5")); // 50% surcharge
        }

        return shippingAmount.setScale(2, RoundingMode.HALF_UP);
    }

    // ===== Business Rules Implementation =====

    @Override
    @Transactional(readOnly = true)
    public boolean checkUserEligibility(Long userId) {
        logger.debug("Checking user eligibility: {}", userId);

        try {
            User user = userService.findById(userId);

            // Check if user is active
            if (!user.getIsActive()) {
                logger.warn("User is not active: {}", userId);
                return false;
            }

            // Additional eligibility checks can be added here
            // For example: account verification, payment method on file, etc.

            return true;

        } catch (UserNotFoundException e) {
            logger.warn("User not found for eligibility check: {}", userId);
            return false;
        } catch (Exception e) {
            logger.error("Error checking user eligibility: {}", e.getMessage());
            return false;
        }
    }

    @Override
    @Transactional
    public BigDecimal applyDiscounts(Long orderId, String discountCode) {
        logger.info("Applying discount to order: {}, code: {}", orderId, discountCode);

        Order order = findOrderById(orderId);

        // Mock discount system - in production, this would be more sophisticated
        BigDecimal discountAmount = BigDecimal.ZERO;

        if (discountCode != null && !discountCode.trim().isEmpty()) {
            switch (discountCode.toUpperCase()) {
                case "SAVE10" -> discountAmount = order.getSubtotal().multiply(new BigDecimal("0.10"));
                case "SAVE20" -> discountAmount = order.getSubtotal().multiply(new BigDecimal("0.20"));
                case "WELCOME15" -> discountAmount = order.getSubtotal().multiply(new BigDecimal("0.15"));
                case "FLAT25" -> discountAmount = new BigDecimal("25.00");
                default -> logger.warn("Invalid discount code: {}", discountCode);
            }
        }

        if (discountAmount.compareTo(BigDecimal.ZERO) > 0) {
            order.setDiscountAmount(discountAmount);
            order.calculateTotalAmount();
            orderRepository.save(order);

            logger.info("Discount applied: {} to order {}", discountAmount, orderId);
        }

        return discountAmount;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean enforceOrderLimits(Long userId, OrderCreateRequest request) {
        logger.debug("Enforcing order limits for user: {}", userId);

        try {
            // Check number of items
            int itemCount = request.hasCustomOrderItems() ? request.getOrderItems().size()
                    : cartService.getCartItemCount(userId);

            if (itemCount > MAX_ORDER_ITEMS) {
                logger.warn("Order exceeds item limit: {} items (max: {})", itemCount, MAX_ORDER_ITEMS);
                return false;
            }

            // Check individual item quantities
            List<OrderCreateRequest.OrderItemRequest> items = request.hasCustomOrderItems() ? request.getOrderItems()
                    : getOrderItemsFromCart(userId);

            for (OrderCreateRequest.OrderItemRequest item : items) {
                if (item.getQuantity() > MAX_QUANTITY_PER_ITEM) {
                    logger.warn("Item quantity exceeds limit: {} (max: {})",
                            item.getQuantity(), MAX_QUANTITY_PER_ITEM);
                    return false;
                }
            }

            // Check order total amount
            BigDecimal orderTotal = calculateOrderSubtotal(items);
            if (orderTotal.compareTo(MAX_ORDER_AMOUNT) > 0) {
                logger.warn("Order exceeds amount limit: {} (max: {})", orderTotal, MAX_ORDER_AMOUNT);
                return false;
            }

            return true;

        } catch (Exception e) {
            logger.error("Error enforcing order limits: {}", e.getMessage());
            return false;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public long countOrdersByStatus(OrderStatus status) {
        return orderRepository.countByStatusAndDateRange(status,
                LocalDateTime.now().minusYears(10), LocalDateTime.now());
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal getTotalRevenue(LocalDateTime startDate, LocalDateTime endDate) {
        BigDecimal revenue = orderRepository.getTotalRevenueByDateRange(startDate, endDate);
        return revenue != null ? revenue : BigDecimal.ZERO;
    }

    // ===== Private Utility Methods =====

    /**
     * Create Pageable from search request
     */
    private Pageable createPageable(OrderSearchRequest request) {
        return org.springframework.data.domain.PageRequest.of(
                request.getPage(),
                request.getSize(),
                request.isDescending() ? org.springframework.data.domain.Sort.by(request.getSortBy()).descending()
                        : org.springframework.data.domain.Sort.by(request.getSortBy()).ascending());
    }

    /**
     * Mock payment processing
     */
    private boolean mockPaymentProcessing(Order order, String paymentData) {
        // Simulate payment processing delay
        try {
            Thread.sleep(1000); // 1-second delay
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Mock success rate of 95%
        return ThreadLocalRandom.current().nextDouble() < 0.95;
    }

    @Override
    public BigDecimal calculateOrderTotal(BigDecimal subtotal, BigDecimal taxAmount,
            BigDecimal shippingAmount, BigDecimal discountAmount) {
        return subtotal.add(taxAmount).add(shippingAmount).subtract(discountAmount);
    }
}