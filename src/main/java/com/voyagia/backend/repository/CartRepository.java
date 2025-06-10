package com.voyagia.backend.repository;

import com.voyagia.backend.entity.Cart;
import com.voyagia.backend.entity.Product;
import com.voyagia.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {

    // Query Cart by user
    List<Cart> findByUserOrderByCreatedAtDesc(User user);

    List<Cart> findByUserIdOrderByCreatedAtDesc(Long userId);

    // Query cart with userId, productId
    Optional<Cart> findByUserAndProduct(User user, Product product);

    Optional<Cart> findByUserIdAndProductId(Long userId, Long productId);

    // Check if cart exists
    boolean existsByUserAndProduct(User user, Product product);

    boolean existsByUserIdAndProductId(Long userId, Long productId);

    // User's cart total quantities and price
    @Query("SELECT SUM(c.quantity) FROM Cart c WHERE c.user.id = :userId")
    Integer getTotalQuantityByUserId(@Param("userId") Long userId);

    @Query("SELECT SUM(c.quantity * c.unitPrice) FROM Cart c WHERE c.user.id = :userId")
    BigDecimal getTotalAmountByUserId(@Param("userId") Long userId);

    // Clear cart by userId
    @Modifying
    @Transactional
    @Query("DELETE FROM Cart c WHERE c.user.id = :userId")
    void deleteByUserId(@Param("userId") Long userId);

    // Delete product from cart
    @Modifying
    @Transactional
    void deleteByProduct(Product product);

    @Modifying
    @Transactional
    void deleteByProductId(Long productId);
}
