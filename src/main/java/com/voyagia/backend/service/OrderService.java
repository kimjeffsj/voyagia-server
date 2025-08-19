package com.voyagia.backend.service;

import com.voyagia.backend.dto.order.*;
import com.voyagia.backend.entity.Order;
import com.voyagia.backend.entity.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Service interface for order management operations
 */
public interface OrderService {

    // ===== Order Creation and Management =====

    /**
     * Create a new order from user's cart or custom items
     *
     * @param userId  user ID
     * @param request order creation request
     * @return created order response
     * @throws InvalidOrderException      if order data is invalid
     * @throws InsufficientStockException if products are out of stock
     * @throws UserNotFoundException      if user not found
     * @throws OrderProcessingException   if order creation fails
     */
    OrderResponse createOrder(Long userId, OrderCreateRequest request);

    /**
     * Create order from cart items
     *
     * @param userId  user ID
     * @param request order creation request (without custom items)
     * @return created order response
     */
    OrderResponse createOrderFromCart(Long userId, OrderCreateRequest request);

    /**
     * Update order information (non-status changes)
     *
     * @param orderId order ID
     * @param request update request
     * @return updated order response
     * @throws OrderNotFoundException if order not found
     * @throws InvalidOrderException  if update data is invalid
     */
    OrderResponse updateOrder(Long orderId, OrderUpdateRequest request);

    /**
     * Update order status
     *
     * @param orderId order ID
     * @param request status update request
     * @return updated order response
     * @throws OrderNotFoundException if order not found
     * @throws InvalidOrderException  if status transition is invalid
     */
    OrderResponse updateOrderStatus(Long orderId, OrderStatusUpdateRequest request);

    /**
     * Cancel an order
     *
     * @param orderId order ID
     * @param reason  cancellation reason
     * @throws OrderNotFoundException if order not found
     * @throws InvalidOrderException  if order cannot be cancelled
     */
    void cancelOrder(Long orderId, String reason);

    /**
     * Cancel an order by user
     *
     * @param userId  user ID
     * @param orderId order ID
     * @param reason  cancellation reason
     * @throws OrderNotFoundException if order not found
     * @throws InvalidOrderException  if order cannot be cancelled or user doesn't own the order
     */
    void cancelOrderByUser(Long userId, Long orderId, String reason);

    // ===== Order Retrieval =====

    /**
     * Get order by ID
     *
     * @param orderId order ID
     * @return order response
     * @throws OrderNotFoundException if order not found
     */
    OrderResponse getOrderById(Long orderId);

    /**
     * Get order by ID (return Optional)
     *
     * @param orderId order ID
     * @return order response optional
     */
    Optional<OrderResponse> getOrderByIdOptional(Long orderId);

    /**
     * Get order by order number
     *
     * @param orderNumber order number
     * @return order response
     * @throws OrderNotFoundException if order not found
     */
    OrderResponse getOrderByOrderNumber(String orderNumber);

    /**
     * Get orders by user ID
     *
     * @param userId   user ID
     * @param pageable pagination information
     * @return paginated order responses
     */
    Page<OrderResponse> getOrdersByUserId(Long userId, Pageable pageable);

    /**
     * Get orders by user ID and status
     *
     * @param userId   user ID
     * @param status   order status
     * @param pageable pagination information
     * @return paginated order responses
     */
    Page<OrderResponse> getOrdersByUserIdAndStatus(Long userId, OrderStatus status, Pageable pageable);

    /**
     * Search orders with criteria
     *
     * @param request search criteria
     * @return paginated search results
     */
    Page<OrderResponse> searchOrders(OrderSearchRequest request);

    /**
     * Get all orders (admin only)
     *
     * @param pageable pagination information
     * @return paginated order responses
     */
    Page<OrderResponse> getAllOrders(Pageable pageable);

    // ===== Order Processing =====

    /**
     * Process order (mark as confirmed and start fulfillment)
     *
     * @param orderId order ID
     * @return processed order response
     * @throws OrderNotFoundException   if order not found
     * @throws OrderProcessingException if order cannot be processed
     */
    OrderResponse processOrder(Long orderId);

    /**
     * Mark order as shipped
     *
     * @param orderId        order ID
     * @param trackingNumber shipping tracking number
     * @return updated order response
     * @throws OrderNotFoundException if order not found
     * @throws InvalidOrderException  if order cannot be shipped
     */
    OrderResponse shipOrder(Long orderId, String trackingNumber);

    /**
     * Mark order as delivered
     *
     * @param orderId order ID
     * @return updated order response
     * @throws OrderNotFoundException if order not found
     * @throws InvalidOrderException  if order cannot be marked as delivered
     */
    OrderResponse deliverOrder(Long orderId);

    // ===== Payment Management =====

    /**
     * Initialize payment for order
     *
     * @param orderId order ID
     * @return payment initialization result
     * @throws OrderNotFoundException if order not found
     * @throws PaymentFailedException if payment initialization fails
     */
    String initializePayment(Long orderId);

    /**
     * Process payment for order
     *
     * @param orderId     order ID
     * @param paymentData payment processing data
     * @return payment processing result
     * @throws OrderNotFoundException if order not found
     * @throws PaymentFailedException if payment processing fails
     */
    boolean processPayment(Long orderId, String paymentData);

    /**
     * Handle payment callback
     *
     * @param orderNumber   order number
     * @param transactionId transaction ID
     * @param status        payment status
     * @throws OrderNotFoundException if order not found
     * @throws PaymentFailedException if payment callback processing fails
     */
    void handlePaymentCallback(String orderNumber, String transactionId, String status);

    /**
     * Validate payment status
     *
     * @param orderId order ID
     * @return payment validation result
     * @throws OrderNotFoundException if order not found
     */
    boolean validatePaymentStatus(Long orderId);

    // ===== Inventory Management =====

    /**
     * Validate order items for availability and pricing
     *
     * @param orderItems list of order items to validate
     * @return validation result
     * @throws InsufficientStockException if any item is out of stock
     * @throws ProductNotFoundException   if any product is not found
     */
    boolean validateOrderItems(List<OrderCreateRequest.OrderItemRequest> orderItems);

    /**
     * Reserve inventory for order items
     *
     * @param orderId order ID
     * @throws OrderNotFoundException     if order not found
     * @throws InsufficientStockException if inventory cannot be reserved
     */
    void reserveInventory(Long orderId);

    /**
     * Release reserved inventory for order items
     *
     * @param orderId order ID
     * @throws OrderNotFoundException if order not found
     */
    void releaseInventory(Long orderId);

    /**
     * Check stock availability for product and quantity
     *
     * @param productId product ID
     * @param quantity  requested quantity
     * @return availability status
     */
    boolean checkStockAvailability(Long productId, Integer quantity);

    // ===== Price Calculations =====

    /**
     * Calculate order subtotal
     *
     * @param orderItems list of order items
     * @return subtotal amount
     */
    BigDecimal calculateOrderSubtotal(List<OrderCreateRequest.OrderItemRequest> orderItems);

    /**
     * Calculate tax amount
     *
     * @param subtotal        subtotal amount
     * @param shippingAddress shipping address for tax calculation
     * @return tax amount
     */
    BigDecimal calculateTax(BigDecimal subtotal, String shippingAddress);

    /**
     * Calculate shipping amount
     *
     * @param orderItems      list of order items
     * @param shippingAddress shipping address
     * @return shipping amount
     */
    BigDecimal calculateShipping(List<OrderCreateRequest.OrderItemRequest> orderItems, String shippingAddress);

    /**
     * Calculate order total
     *
     * @param subtotal       subtotal amount
     * @param taxAmount      tax amount
     * @param shippingAmount shipping amount
     * @param discountAmount discount amount
     * @return total amount
     */
    BigDecimal calculateOrderTotal(BigDecimal subtotal, BigDecimal taxAmount,
                                   BigDecimal shippingAmount, BigDecimal discountAmount);

    // ===== Business Rules and Validation =====

    /**
     * Check if user is eligible to place order
     *
     * @param userId user ID
     * @return eligibility status
     */
    boolean checkUserEligibility(Long userId);

    /**
     * Apply discounts to order
     *
     * @param orderId      order ID
     * @param discountCode discount code
     * @return discount amount applied
     * @throws OrderNotFoundException if order not found
     */
    BigDecimal applyDiscounts(Long orderId, String discountCode);

    /**
     * Enforce order limits (quantity, amount, etc.)
     *
     * @param userId  user ID
     * @param request order creation request
     * @return validation result
     */
    boolean enforceOrderLimits(Long userId, OrderCreateRequest request);

    // ===== Utility Methods =====

    /**
     * Generate unique order number
     *
     * @return unique order number
     */
    String generateOrderNumber();

    /**
     * Get order entity by ID (for internal use)
     *
     * @param orderId order ID
     * @return order entity
     * @throws OrderNotFoundException if order not found
     */
    Order findOrderById(Long orderId);

    /**
     * Get order entity by ID (return Optional)
     *
     * @param orderId order ID
     * @return order entity optional
     */
    Optional<Order> findOrderByIdOptional(Long orderId);

    /**
     * Check if user owns the order
     *
     * @param userId  user ID
     * @param orderId order ID
     * @return ownership status
     */
    boolean validateUserOwnership(Long userId, Long orderId);

    /**
     * Count orders by status
     *
     * @param status order status
     * @return count of orders
     */
    long countOrdersByStatus(OrderStatus status);

    /**
     * Get total revenue for date range
     *
     * @param startDate start date
     * @param endDate   end date
     * @return total revenue
     */
    BigDecimal getTotalRevenue(java.time.LocalDateTime startDate, java.time.LocalDateTime endDate);
}