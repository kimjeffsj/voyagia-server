package com.voyagia.backend.repository;

import com.voyagia.backend.entity.Category;
import com.voyagia.backend.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {

        // ========== 기존 메서드들 (하위 호환성을 위해 유지) ==========

        // Basic Search
        Optional<Product> findBySlug(String slug);

        Optional<Product> findBySku(String sku);

        boolean existsBySlug(String slug);

        boolean existsBySku(String sku);

        // ========== JOIN FETCH를 사용한 새로운 메서드들 ==========

        /**
         * ID로 Product 조회 (Category 함께 로딩)
         */
        @Query("SELECT p FROM Product p JOIN FETCH p.category WHERE p.id = :id")
        Optional<Product> findByIdWithCategory(@Param("id") Long id);

        /**
         * Slug로 Product 조회 (Category 함께 로딩)
         */
        @Query("SELECT p FROM Product p JOIN FETCH p.category WHERE p.slug = :slug")
        Optional<Product> findBySlugWithCategory(@Param("slug") String slug);

        /**
         * SKU로 Product 조회 (Category 함께 로딩)
         */
        @Query("SELECT p FROM Product p JOIN FETCH p.category WHERE p.sku = :sku")
        Optional<Product> findBySkuWithCategory(@Param("sku") String sku);

        // ========== @EntityGraph를 사용한 메서드들 ==========

        /**
         * 활성 상품 목록 조회 (페이징) - Category 함께 로딩
         */
        @EntityGraph(attributePaths = { "category" })
        @Query("SELECT p FROM Product p WHERE p.isActive = true ORDER BY p.createdAt DESC")
        Page<Product> findAllActiveProductsWithCategory(Pageable pageable);

        /**
         * 추천 상품 목록 조회 - Category 함께 로딩
         */
        @EntityGraph(attributePaths = { "category" })
        @Query("SELECT p FROM Product p WHERE p.isActive = true AND p.isFeatured = true ORDER BY p.createdAt DESC")
        List<Product> findFeaturedProductsWithCategory();

        /**
         * 최신 상품 목록 조회 - Category 함께 로딩
         */
        @EntityGraph(attributePaths = { "category" })
        @Query("SELECT p FROM Product p WHERE p.isActive = true ORDER BY p.createdAt DESC")
        Page<Product> findLatestProductsWithCategory(Pageable pageable);

        // ========== 카테고리별 조회 (수정됨) ==========

        /**
         * 카테고리별 상품 조회 - Category 함께 로딩 (이미 로딩된 상태이므로 JOIN FETCH 불필요)
         */
        @Query("SELECT p FROM Product p WHERE p.category.id = :categoryId AND p.isActive = true ORDER BY p.createdAt DESC")
        Page<Product> findByCategoryIdAndIsActiveTrueWithCategory(@Param("categoryId") Long categoryId,
                        Pageable pageable);

        /**
         * 카테고리 객체로 상품 조회
         */
        Page<Product> findByCategoryAndIsActiveTrue(Category category, Pageable pageable);

        // ========== 가격 범위 조회 (수정됨) ==========

        /**
         * 가격 범위로 상품 조회 - Category 함께 로딩
         */
        @Query("SELECT p FROM Product p JOIN FETCH p.category WHERE " +
                        "p.isActive = true AND " +
                        "p.price BETWEEN :minPrice AND :maxPrice " +
                        "ORDER BY p.createdAt DESC")
        Page<Product> findByPriceRangeWithCategory(@Param("minPrice") BigDecimal minPrice,
                        @Param("maxPrice") BigDecimal maxPrice,
                        Pageable pageable);

        // ========== 재고 관리 (기존 유지) ==========

        List<Product> findByStockQuantityLessThanEqualAndIsActiveTrue(Integer threshold);

        List<Product> findByStockQuantityGreaterThanAndIsActiveTrue(Integer minStock);

        // ========== 검색 기능 (수정됨) ==========

        /**
         * 키워드 검색 - Category 함께 로딩
         */
        @Query("SELECT p FROM Product p JOIN FETCH p.category WHERE " +
                        "p.isActive = true AND (" +
                        "LOWER(p.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
                        "LOWER(p.description) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
                        "LOWER(p.shortDescription) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
                        "LOWER(p.sku) LIKE LOWER(CONCAT('%', :searchTerm, '%'))" +
                        ") ORDER BY p.createdAt DESC")
        Page<Product> searchProductsWithCategory(@Param("searchTerm") String searchTerm, Pageable pageable);

        /**
         * 고급 필터 검색 - Category 함께 로딩
         */
        @Query("SELECT p FROM Product p JOIN FETCH p.category WHERE " +
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
        Page<Product> findWithFiltersAndCategory(@Param("categoryId") Long categoryId,
                        @Param("minPrice") BigDecimal minPrice,
                        @Param("maxPrice") BigDecimal maxPrice,
                        @Param("searchTerm") String searchTerm,
                        @Param("sortBy") String sortBy,
                        Pageable pageable);

        // ========== 기존 메서드들 (하위 호환성) ==========

        Page<Product> findByIsActiveTrueOrderByCreatedAtDesc(Pageable pageable);

        List<Product> findByIsActiveTrueAndIsFeaturedTrue();

        Page<Product> findByCategoryIdAndIsActiveTrue(Long categoryId, Pageable pageable);

        @Query("SELECT p FROM Product p WHERE " +
                        "p.isActive = true AND " +
                        "p.price BETWEEN :minPrice AND :maxPrice " +
                        "ORDER BY p.createdAt DESC")
        Page<Product> findByPriceRange(@Param("minPrice") BigDecimal minPrice,
                        @Param("maxPrice") BigDecimal maxPrice,
                        Pageable pageable);

        @Query("SELECT p FROM Product p WHERE " +
                        "p.isActive = true AND (" +
                        "LOWER(p.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
                        "LOWER(p.description) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
                        "LOWER(p.shortDescription) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
                        "LOWER(p.sku) LIKE LOWER(CONCAT('%', :searchTerm, '%'))" +
                        ") ORDER BY p.createdAt DESC")
        Page<Product> searchProducts(@Param("searchTerm") String searchTerm, Pageable pageable);

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