package com.voyagia.backend.service;

import com.voyagia.backend.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Product interface
 * <p>
 * product CRUD, inventory management, query, and filtering.
 */
public interface ProductService {
    // CRUD

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


    // Product Query

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


    // Search and filtering

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


    // Inventory management

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


    // Product status management

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


    // Validate & utilities

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
