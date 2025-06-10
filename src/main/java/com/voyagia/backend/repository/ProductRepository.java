package com.voyagia.backend.repository;

import com.voyagia.backend.entity.Category;
import com.voyagia.backend.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {

    // Basic Search
    Optional<Product> findBySlug(String slug);

    Optional<Product> findBySku(String sku);

    boolean existsBySlug(String slug);

    boolean existsBySku(String sku);

    // Active products
    Page<Product> findByIsActiveTrueOrderByCreatedAtDesc(Pageable pageable);

    List<Product> findByIsActiveTrueAndIsFeaturedTrue();

    // 카테고리별 조회
    Page<Product> findByCategoryAndIsActiveTrue(Category category, Pageable pageable);

    Page<Product> findByCategoryIdAndIsActiveTrue(Long categoryId, Pageable pageable);

    // Price range query
    @Query("SELECT p FROM Product p WHERE " +
            "p.isActive = true AND " +
            "p.price BETWEEN :minPrice AND :maxPrice " +
            "ORDER BY p.createdAt DESC")
    Page<Product> findByPriceRange(@Param("minPrice") BigDecimal minPrice,
                                   @Param("maxPrice") BigDecimal maxPrice,
                                   Pageable pageable);

    // Inventory
    List<Product> findByStockQuantityLessThanEqualAndIsActiveTrue(Integer threshold);

    List<Product> findByStockQuantityGreaterThanAndIsActiveTrue(Integer minStock);

    // Combined Search(Name, Description, SKU)
    @Query("SELECT p FROM Product p WHERE " +
            "p.isActive = true AND (" +
            "LOWER(p.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(p.description) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(p.shortDescription) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(p.sku) LIKE LOWER(CONCAT('%', :searchTerm, '%'))" +
            ") ORDER BY p.createdAt DESC")
    Page<Product> searchProducts(@Param("searchTerm") String searchTerm, Pageable pageable);

    // Filtered Query
    @Query("SELECT p FROM Product p WHERE " +
            "p.isActive = true AND " +
            "(:categoryId IS NULL OR p.category.id = :categoryId) AND " +
            "(:minPrice IS NULL OR p.price >= :minPrice) AND " +
            "(:maxPrice IS NULL OR p.price <= :maxPrice) AND " +
            "(:searchTerm IS NULL OR " +
            " LOWER(p.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            " LOWER(p.description) LIKE LOWER(CONCAT('%', :searchTerm, '%'))" +
            ") ORDER BY " +
            "CASE WHEN :sortBy = 'name' THEN p.name END ASC, " +
            "CASE WHEN :sortBy = 'price_asc' THEN p.price END ASC, " +
            "CASE WHEN :sortBy = 'price_desc' THEN p.price END DESC, " +
            "p.createdAt DESC")
    Page<Product> findWithFilters(@Param("categoryId") Long categoryId,
                                  @Param("minPrice") BigDecimal minPrice,
                                  @Param("maxPrice") BigDecimal maxPrice,
                                  @Param("searchTerm") String searchTerm,
                                  @Param("sortBy") String sortBy,
                                  Pageable pageable);
}
