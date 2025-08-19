package com.voyagia.backend.dto.order;

import com.voyagia.backend.entity.Order;
import com.voyagia.backend.entity.OrderItem;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper for converting between Order entities and DTOs
 */
@Component
public class OrderDTOMapper {

    /**
     * Convert Order entity to OrderResponse DTO
     */
    public OrderResponse toOrderResponse(Order order) {
        if (order == null) {
            return null;
        }

        OrderResponse response = new OrderResponse();

        // Basic information
        response.setId(order.getId());
        response.setOrderNumber(order.getOrderNumber());
        response.setUserId(order.getUser().getId());
        response.setUserEmail(order.getUser().getEmail());
        response.setStatus(order.getStatus());
        response.setPaymentStatus(order.getPaymentStatus());
        response.setPaymentMethod(order.getPaymentMethod());

        // Pricing information
        response.setSubtotal(order.getSubtotal());
        response.setTaxAmount(order.getTaxAmount());
        response.setShippingAmount(order.getShippingAmount());
        response.setDiscountAmount(order.getDiscountAmount());
        response.setTotalAmount(order.getTotalAmount());

        // Shipping information
        response.setShippingFirstName(order.getShippingFirstName());
        response.setShippingLastName(order.getShippingLastName());
        response.setShippingEmail(order.getShippingEmail());
        response.setShippingPhone(order.getShippingPhone());
        response.setShippingAddress(order.getShippingAddress());
        response.setShippingCity(order.getShippingCity());
        response.setShippingState(order.getShippingState());
        response.setShippingPostalCode(order.getShippingPostalCode());
        response.setShippingCountry(order.getShippingCountry());

        // Payment and tracking information
        response.setPaymentTransactionId(order.getPaymentTransactionId());
        response.setTrackingNumber(order.getTrackingNumber());
        response.setNotes(order.getNotes());
        response.setCancelReason(order.getCancelReason());

        // Timestamps
        response.setCreatedAt(order.getCreatedAt());
        response.setUpdatedAt(order.getUpdatedAt());
        response.setPaidAt(order.getPaidAt());
        response.setShippedAt(order.getShippedAt());
        response.setDeliveredAt(order.getDeliveredAt());
        response.setCancelledAt(order.getCancelledAt());

        // Order items
        if (order.getOrderItems() != null) {
            List<OrderItemResponse> orderItemResponses = order.getOrderItems().stream()
                    .map(this::toOrderItemResponse)
                    .collect(Collectors.toList());
            response.setOrderItems(orderItemResponses);
        }

        // Calculated fields
        response.setTotalItems(order.getTotalItems());
        response.setShippingFullName(order.getShippingFullName());
        response.setShippingFullAddress(order.getShippingFullAddress());

        return response;
    }

    /**
     * Convert OrderItem entity to OrderItemResponse DTO
     */
    public OrderItemResponse toOrderItemResponse(OrderItem orderItem) {
        if (orderItem == null) {
            return null;
        }

        OrderItemResponse response = new OrderItemResponse();

        // Basic information
        response.setId(orderItem.getId());
        response.setOrderId(orderItem.getOrder().getId());
        response.setProductId(orderItem.getProduct().getId());
        response.setQuantity(orderItem.getQuantity());
        response.setUnitPrice(orderItem.getUnitPrice());
        response.setTotalPrice(orderItem.getTotalPrice());

        // Product snapshot information
        response.setProductName(orderItem.getProductName());
        response.setProductSku(orderItem.getProductSku());
        response.setProductImageUrl(orderItem.getProductImageUrl());

        // Discount information
        response.setDiscountAmount(orderItem.getDiscountAmount());
        response.setDiscountPercentage(orderItem.getDiscountPercentage());

        // Timestamps
        response.setCreatedAt(orderItem.getCreatedAt());
        response.setUpdatedAt(orderItem.getUpdatedAt());

        // Calculated fields
        response.setSubtotal(orderItem.getSubtotal());
        response.setFinalUnitPrice(orderItem.getFinalUnitPrice());
        response.setHasDiscount(orderItem.hasDiscount());

        return response;
    }

    /**
     * Convert list of Order entities to list of OrderResponse DTOs
     */
    public List<OrderResponse> toOrderResponseList(List<Order> orders) {
        if (orders == null) {
            return null;
        }

        return orders.stream()
                .map(this::toOrderResponse)
                .collect(Collectors.toList());
    }

    /**
     * Convert list of OrderItem entities to list of OrderItemResponse DTOs
     */
    public List<OrderItemResponse> toOrderItemResponseList(List<OrderItem> orderItems) {
        if (orderItems == null) {
            return null;
        }

        return orderItems.stream()
                .map(this::toOrderItemResponse)
                .collect(Collectors.toList());
    }

    /**
     * Update Order entity from OrderUpdateRequest
     */
    public void updateOrderFromRequest(Order order, OrderUpdateRequest request) {
        if (order == null || request == null) {
            return;
        }

        // Update shipping information
        if (request.getShippingFirstName() != null) {
            order.setShippingFirstName(request.getShippingFirstName());
        }
        if (request.getShippingLastName() != null) {
            order.setShippingLastName(request.getShippingLastName());
        }
        if (request.getShippingPhone() != null) {
            order.setShippingPhone(request.getShippingPhone());
        }
        if (request.getShippingAddress() != null) {
            order.setShippingAddress(request.getShippingAddress());
        }
        if (request.getShippingCity() != null) {
            order.setShippingCity(request.getShippingCity());
        }
        if (request.getShippingState() != null) {
            order.setShippingState(request.getShippingState());
        }
        if (request.getShippingPostalCode() != null) {
            order.setShippingPostalCode(request.getShippingPostalCode());
        }
        if (request.getShippingCountry() != null) {
            order.setShippingCountry(request.getShippingCountry());
        }

        // Update tracking and administrative information
        if (request.getTrackingNumber() != null) {
            order.setTrackingNumber(request.getTrackingNumber());
        }
        if (request.getNotes() != null) {
            order.setNotes(request.getNotes());
        }
        if (request.getPaymentTransactionId() != null) {
            order.setPaymentTransactionId(request.getPaymentTransactionId());
        }
    }

    /**
     * Create Order entity from OrderCreateRequest (partial mapping)
     * Note: This only maps the basic fields. Business logic and relationships
     * should be handled in the service layer
     */
    public Order createOrderFromRequest(OrderCreateRequest request) {
        if (request == null) {
            return null;
        }

        Order order = new Order();

        // Shipping information
        order.setShippingFirstName(request.getShippingFirstName());
        order.setShippingLastName(request.getShippingLastName());
        order.setShippingEmail(request.getShippingEmail());
        order.setShippingPhone(request.getShippingPhone());
        order.setShippingAddress(request.getShippingAddress());
        order.setShippingCity(request.getShippingCity());
        order.setShippingState(request.getShippingState());
        order.setShippingPostalCode(request.getShippingPostalCode());
        order.setShippingCountry(request.getShippingCountry());

        // Payment information
        order.setPaymentMethod(request.getPaymentMethod());

        // Notes
        order.setNotes(request.getNotes());

        // Custom pricing (if provided)
        if (request.getTaxAmount() != null) {
            order.setTaxAmount(request.getTaxAmount());
        }
        if (request.getShippingAmount() != null) {
            order.setShippingAmount(request.getShippingAmount());
        }
        if (request.getDiscountAmount() != null) {
            order.setDiscountAmount(request.getDiscountAmount());
        }

        return order;
    }

    /**
     * Create OrderItem entity from OrderCreateRequest.OrderItemRequest
     */
    public OrderItem createOrderItemFromRequest(OrderCreateRequest.OrderItemRequest request) {
        if (request == null) {
            return null;
        }

        OrderItem orderItem = new OrderItem();
        orderItem.setQuantity(request.getQuantity());

        // Unit price will be set from Product entity in service layer
        // unless custom price is provided
        if (request.hasCustomPrice()) {
            orderItem.setUnitPrice(request.getUnitPrice());
        }

        return orderItem;
    }

    /**
     * Simple mapping for order summary (without order items)
     */
    public OrderResponse toOrderSummaryResponse(Order order) {
        OrderResponse response = toOrderResponse(order);
        if (response != null) {
            response.setOrderItems(null); // Remove order items for summary
        }
        return response;
    }

    /**
     * Convert list of orders to summary responses
     */
    public List<OrderResponse> toOrderSummaryResponseList(List<Order> orders) {
        if (orders == null) {
            return null;
        }

        return orders.stream()
                .map(this::toOrderSummaryResponse)
                .collect(Collectors.toList());
    }
}