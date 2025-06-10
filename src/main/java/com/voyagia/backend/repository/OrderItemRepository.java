package com.voyagia.backend.repository;

import com.voyagia.backend.entity.Order;
import com.voyagia.backend.entity.OrderItem;
import com.voyagia.backend.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
    // Query by Order
    List<OrderItem> findByOrder(Order order);

    List<OrderItem> findByOrderId(Long orderId);

    // Query by Product
    List<OrderItem> findByProduct(Product product);

    List<OrderItem> findByProductId(Long productId);

    // Query Stats - top-selling
    @Query("SELECT oi.product.id, oi.productName, SUM(oi.quantity) as totalSold " +
            "FROM OrderItem oi " +
            "WHERE oi.order.createdAt >= :startDate AND oi.order.createdAt <= :endDate " +
            "GROUP BY oi.product.id, oi.productName " +
            "ORDER BY totalSold DESC")
    List<Object[]> findTopSellingProducts(@Param("startDate") LocalDateTime startDate,
                                          @Param("endDate") LocalDateTime endDate);

    // Query total sold quantity
    @Query("SELECT SUM(oi.quantity) FROM OrderItem oi WHERE oi.product.id = :productId")
    Long getTotalSoldQuantity(@Param("productId") Long productId);
}
