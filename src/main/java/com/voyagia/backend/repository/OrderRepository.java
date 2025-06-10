package com.voyagia.backend.repository;

import com.voyagia.backend.entity.Order;
import com.voyagia.backend.entity.OrderStatus;
import com.voyagia.backend.entity.PaymentStatus;
import com.voyagia.backend.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    // Basic Search
    Optional<Order> findByOrderNumber(String orderNumber);

    // Query by userId
    Page<Order> findByUserOrderByCreatedAtDesc(User user, Pageable pageable);

    Page<Order> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    // Query by status
    Page<Order> findByStatusOrderByCreatedAtDesc(OrderStatus status, Pageable pageable);

    Page<Order> findByPaymentStatusOrderByCreatedAtDesc(PaymentStatus paymentStatus, Pageable pageable);

    // Query by User + Status
    List<Order> findByUserAndStatus(User user, OrderStatus orderStatus);

    Page<Order> findByUserIdAndStatus(Long userId, OrderStatus status, Pageable pageable);

    // Query by Date range
    @Query("SELECT o FROM Order o WHERE " +
            "o.createdAt >= :startDate AND o.createdAt <= :endDate " +
            "ORDER BY o.createdAt DESC")
    Page<Order> findByDateRange(@Param("startDate") LocalDateTime startDate,
                                @Param("endDate") LocalDateTime endDate,
                                Pageable pageable);


    // Query Stats
    @Query("SELECT COUNT(o) FROM Order o WHERE " +
            "o.status = :status AND " +
            "o.createdAt >= :startDate AND o.createdAt <= :endDate")
    Long countByStatusAndDateRange(@Param("status") OrderStatus status,
                                   @Param("startDate") LocalDateTime startDate,
                                   @Param("endDate") LocalDateTime endDate);

    @Query("SELECT SUM(o.totalAmount) FROM Order o WHERE " +
            "o.paymentStatus = 'PAID' AND " +
            "o.createdAt >= :startDate AND o.createdAt <= :endDate")
    BigDecimal getTotalRevenueByDateRange(@Param("startDate") LocalDateTime startDate,
                                          @Param("endDate") LocalDateTime endDate);


    // Search
    @Query("SELECT o FROM Order o WHERE " +
            "LOWER(o.orderNumber) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(o.shippingEmail) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(o.shippingFirstName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(o.shippingLastName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
            "ORDER BY o.createdAt DESC")
    Page<Order> searchOrders(@Param("searchTerm") String searchTerm, Pageable pageable);
}
