package com.voyagia.backend.service;

import com.voyagia.backend.entity.Product;
import com.voyagia.backend.dto.product.ProductResponse; // DTO 변환 메서드를 위한 import 추가
import com.voyagia.backend.dto.product.ProductUpdateRequest; // ProductUpdateRequest import 추가
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Product interface
 * <p>
 * product CRUD, inventory management, query, and filtering.
 * DTO 변환 메서드들이 추가되어 LazyInitializationException을 방지합니다.
 */
public interface ProductService {
    // ========================================
    // CRUD (기존 엔티티 반환 메서드들 - 하위 호환성 유지)
    // ========================================

    /**
     * Create product
     *
     * @param product product data
     * @return created product
     * @throws ProductAlreadyExistsException SKU or slug is already exists
     * @throws InvalidProductDataException   Invalid product data
     */
    Product createProduct(Product product);

    /**
     * Find product by ID
     *
     * @param id product ID
     * @return product result
     * @throws ProductNotFoundException Product not found
     */
    Product findById(Long id);

    /**
     * Find product by ID ( return Optional)
     *
     * @param id product ID
     * @return product result (Optional)
     */
    Optional<Product> findByIdOptional(Long id);

    /**
     * Find product by SKU
     *
     * @param sku product SKU
     * @return product result
     * @throws ProductNotFoundException product not found
     */
    Product findBySku(String sku);

    /**
     * Find product by SKU (return Optional)
     *
     * @param sku product SKU
     * @return product result (Optional)
     */
    Optional<Product> findBySkuOptional(String sku);

    /**
     * Find product by slug
     *
     * @param slug product slug
     * @return product result
     * @throws ProductNotFoundException product not found
     */
    Product findBySlug(String slug);

    /**
     * Find product by slug (return Optional)
     *
     * @param slug product slug
     * @return product result (Optional)
     */
    Optional<Product> findBySlugOptional(String slug);

    /**
     * Update product details
     *
     * @param id             product ID
     * @param productDetails product details
     * @return updated product details
     * @throws ProductNotFoundException      product not found
     * @throws ProductAlreadyExistsException SKU/slug is already exists
     */
    Product updateProduct(Long id, Product productDetails);

    /**
     * Delete product (Soft delete - deactivated)
     *
     * @param id product ID
     * @throws ProductNotFoundException product not found
     */
    void deleteProduct(Long id);

    /**
     * Delete product (Hard delete - delete permanently)
     * caution: cannot delete product with order history
     *
     * @param id product ID
     * @throws ProductNotFoundException    product not found
     * @throws InvalidProductDataException invalid product data (cannot delete)
     */
    void deleteProductPermanently(Long id);

    // ========================================
    // Product Query (기존 엔티티 반환 메서드들 - 하위 호환성 유지)
    // ========================================

    /**
     * Query active products
     *
     * @param pageable paging information
     * @return Paginated active products
     */
    Page<Product> findAllActiveProducts(Pageable pageable);

    /**
     * Query featured products
     *
     * @return featured products list
     */
    List<Product> findFeaturedProducts();

    /**
     * Query Lastest products
     *
     * @param limit products number
     * @return latest products list
     */
    List<Product> findLatestProducts(int limit);

    /**
     * Query products by price range
     *
     * @param minPrice minimum price
     * @param maxPrice maximum price
     * @param pageable paging information
     * @return paginated products list
     */
    Page<Product> findByPriceRange(BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable);

    /**
     * Query products by category
     *
     * @param categoryId category ID
     * @param pageable   paging information
     * @return paginated categorized products list
     */
    Page<Product> findByCategoryId(Long categoryId, Pageable pageable);

    // ========================================
    // DTO 변환 메서드들 (LazyInitializationException 방지)
    // ========================================

    /**
     * ======== 단일 조회 DTO 메서드들 ========
     * Service 레이어에서 DTO 변환을 수행하여 LazyInitializationException 방지
     */

    /**
     * Find product by ID and convert to DTO
     *
     * @param id product ID
     * @return product response DTO
     * @throws ProductNotFoundException Product not found
     */
    ProductResponse findByIdAsDto(Long id);

    /**
     * Find product by slug and convert to DTO
     *
     * @param slug product slug
     * @return product response DTO
     * @throws ProductNotFoundException product not found
     */
    ProductResponse findBySlugAsDto(String slug);

    /**
     * Update product from ProductUpdateRequest and return DTO
     * Controller에서 직접 사용할 수 있는 메서드
     *
     * @param id      product ID
     * @param request update request DTO
     * @return updated product response DTO
     * @throws ProductNotFoundException      Product not found
     * @throws ProductAlreadyExistsException SKU/slug already exists
     * @throws InvalidProductDataException   Invalid request data
     */
    ProductResponse updateProductFromRequest(Long id, ProductUpdateRequest request);

    /**
     * ======== 목록 조회 DTO 메서드들 ========
     */

    /**
     * Query active products and convert to DTO
     *
     * @param pageable paging information
     * @return Paginated active products DTO
     */
    Page<ProductResponse> findAllActiveProductsAsDto(Pageable pageable);

    /**
     * Query featured products and convert to DTO
     *
     * @return featured products DTO list
     */
    List<ProductResponse> findFeaturedProductsAsDto();

    /**
     * Query latest products and convert to DTO
     *
     * @param limit products number
     * @return latest products DTO list
     */
    List<ProductResponse> findLatestProductsAsDto(int limit);

    /**
     * ======== 검색/필터 DTO 메서드들 ========
     */

    /**
     * Find product by keyword and convert to DTO
     *
     * @param keyword  search keyword
     * @param pageable paging information
     * @return paginated product DTO list
     */
    Page<ProductResponse> searchProductsAsDto(String keyword, Pageable pageable);

    /**
     * Query products by category and convert to DTO
     *
     * @param categoryId category ID
     * @param pageable   paging information
     * @return paginated categorized products DTO list
     */
    Page<ProductResponse> findByCategoryIdAsDto(Long categoryId, Pageable pageable);

    /**
     * Filtering and convert to DTO
     *
     * @param categoryId category ID (nullable)
     * @param minPrice   minimum price (nullable)
     * @param maxPrice   maximum price (nullable)
     * @param keyword    search keyword (nullable)
     * @param sortBy     sort by ("name", "price_asc", "price_desc", "latest")
     * @param pageable   paging information
     * @return filtered product DTO list
     */
    Page<ProductResponse> findWithFiltersAsDto(Long categoryId, BigDecimal minPrice, BigDecimal maxPrice,
            String keyword, String sortBy, Pageable pageable);

    // ========================================
    // 기존 메서드들 (하위 호환성을 위해 유지)
    // ========================================

    // Search and filtering (기존 엔티티 반환 메서드들)

    /**
     * Find product by keyword
     * product name, description, SKU
     *
     * @param keyword  search keyword
     * @param pageable paging information
     * @return paginated product list
     */
    Page<Product> searchProducts(String keyword, Pageable pageable);

    /**
     * Filtering
     *
     * @param categoryId category ID (nullable)
     * @param minPrice   minimum price (nullable)
     * @param maxPrice   maximum price (nullable)
     * @param keyword    search keyword (nullable)
     * @param sortBy     sort by ("name", "price_asc", "price_desc", "latest")
     * @param pageable   paging information
     * @return filtered product list
     */
    Page<Product> findWithFilters(Long categoryId, BigDecimal minPrice, BigDecimal maxPrice,
            String keyword, String sortBy, Pageable pageable);

    // ========================================
    // Inventory management (기존 메서드들 유지)
    // ========================================

    /**
     * Check product's current stock
     *
     * @param productId product ID
     * @return current product stock
     * @throws ProductNotFoundException product not found
     */
    Integer getStockQuantity(Long productId);

    /**
     * Check if product has enough stock
     *
     * @param productId        product ID
     * @param requiredQuantity required quantity
     * @return true/false
     * @throws ProductNotFoundException product not found
     */
    boolean hasEnoughStock(Long productId, Integer requiredQuantity);

    /**
     * Increase stock number
     *
     * @param productId product ID
     * @param quantity  increasing quantity by number
     * @throws ProductNotFoundException    product not found
     * @throws InvalidProductDataException negative quantity
     */
    void increaseStock(Long productId, Integer quantity);

    /**
     * Decrease stock number
     *
     * @param productId product ID
     * @param quantity  decreasing quantity by number
     * @throws ProductNotFoundException    product not found
     * @throws InsufficientStockException  Not enough stock
     * @throws InvalidProductDataException negative quantity
     */
    void decreaseStock(Long productId, Integer quantity);

    /**
     * Find low stock products
     *
     * @return low stock products list
     */
    List<Product> findLowStockProducts();

    /**
     * Find products in stocks
     *
     * @param minStock minimum stock
     * @return product with stock list
     */
    List<Product> findProductsWithStock(Integer minStock);

    // ========================================
    // Product status management (기존 메서드들 유지)
    // ========================================

    /**
     * Activate product
     *
     * @param id product ID
     * @throws ProductNotFoundException product not found
     */
    void activateProduct(Long id);

    /**
     * Deactivate product
     *
     * @param id product ID
     * @throws ProductNotFoundException product not found
     */
    void deactivateProduct(Long id);

    /**
     * Set product as featured
     *
     * @param id product ID
     * @throws ProductNotFoundException product not found
     */
    void setAsFeatured(Long id);

    /**
     * Unset featured product
     *
     * @param id product ID
     * @throws ProductNotFoundException product not found
     */
    void unsetAsFeatured(Long id);

    // ========================================
    // Validate & utilities (기존 메서드들 유지)
    // ========================================

    /**
     * Check if SKU exists
     *
     * @param sku SKU
     * @return true/false
     */
    boolean existsBySku(String sku);

    /**
     * Check if slug exists
     *
     * @param slug slug
     * @return true/false
     */
    boolean existsBySlug(String slug);

    /**
     * Count all products
     *
     * @return number of total products
     */
    long countAllProducts();

    /**
     * Count active products
     *
     * @return number of active products
     */
    long countActiveProducts();

    /**
     * Count products by category
     *
     * @param categoryId category ID
     * @return number of the products in the category
     */
    long countByCategoryId(Long categoryId);
}
