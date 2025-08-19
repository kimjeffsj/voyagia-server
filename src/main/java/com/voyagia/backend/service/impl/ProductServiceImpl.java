package com.voyagia.backend.service.impl;

import com.voyagia.backend.entity.Category;
import com.voyagia.backend.entity.Product;
import com.voyagia.backend.dto.product.ProductResponse; // DTO 변환을 위한 import 추가
import com.voyagia.backend.dto.product.ProductDTOMapper; // DTO 매퍼 import 추가
import com.voyagia.backend.dto.product.ProductUpdateRequest; // ProductUpdateRequest import 추가
import com.voyagia.backend.exception.InsufficientStockException;
import com.voyagia.backend.exception.InvalidProductDataException;
import com.voyagia.backend.exception.ProductAlreadyExistsException;
import com.voyagia.backend.exception.ProductNotFoundException;
import com.voyagia.backend.repository.CategoryRepository;
import com.voyagia.backend.repository.ProductRepository;
import com.voyagia.backend.service.ProductService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * ProductService implements
 */
@Service
@Transactional(readOnly = true)
public class ProductServiceImpl implements ProductService {

    private static final Logger logger = LoggerFactory.getLogger(ProductServiceImpl.class);

    // Final fields for dependency injection
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductDTOMapper productDTOMapper; // DTO 변환을 위한 매퍼 추가

    /**
     * Dependency injection
     *
     * @param productRepository  access to product data
     * @param categoryRepository validate category
     * @param productDTOMapper   DTO 변환을 위한 매퍼 추가
     */
    public ProductServiceImpl(ProductRepository productRepository,
            CategoryRepository categoryRepository,
            ProductDTOMapper productDTOMapper) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.productDTOMapper = productDTOMapper; // 매퍼 초기화 추가
    }

    // CRUD

    @Override
    @Transactional
    public Product createProduct(Product product) {
        logger.info("Create product: name={}, sku={}", product.getName(), product.getSku());

        // Validate data
        validateProductForCreation(product);

        // Check duplicate
        if (existsBySku(product.getSku())) {
            logger.warn("SKU is already exists : {}", product.getSku());
            throw new ProductAlreadyExistsException("sku", product.getSku());
        }

        if (existsBySlug(product.getSlug())) {
            logger.warn("Slug is already exists: {}", product.getSlug());
            throw new ProductAlreadyExistsException("slug", product.getSlug());
        }

        // Validate category
        validateCategory(product.getCategory());

        // Default
        if (product.getIsActive() == null) {
            product.setIsActive(true);
        }
        if (product.getIsFeatured() == null) {
            product.setIsFeatured(false);
        }
        if (product.getStockQuantity() == null) {
            product.setStockQuantity(0);
        }
        if (product.getLowStockThreshold() == null) {
            product.setLowStockThreshold(10);
        }

        // Save product
        Product savedProduct = productRepository.save(product);
        logger.info("Product created successfully: id={}, sku={}", savedProduct.getId(), savedProduct.getSku());

        return savedProduct;
    }

    @Override
    public Product findById(Long id) {
        logger.debug("Find product by ID: {}", id);
        // JOIN FETCH를 사용하여 Category와 함께 로딩 (LazyInitializationException 방지)
        return productRepository.findByIdWithCategory(id)
                .orElseThrow(() -> new ProductNotFoundException(id));
    }

    @Override
    public Optional<Product> findByIdOptional(Long id) {
        logger.debug("Find product by ID (Optional): {}", id);
        // JOIN FETCH를 사용하여 Category와 함께 로딩 (LazyInitializationException 방지)
        return productRepository.findByIdWithCategory(id);
    }

    @Override
    public Product findBySku(String sku) {
        logger.debug("Find product by SKU: {}", sku);
        // JOIN FETCH를 사용하여 Category와 함께 로딩 (LazyInitializationException 방지)
        return productRepository.findBySkuWithCategory(sku)
                .orElseThrow(() -> new ProductNotFoundException(sku, "sku"));
    }

    @Override
    public Optional<Product> findBySkuOptional(String sku) {
        logger.debug("Find product by SKU (Optional): {}", sku);
        // JOIN FETCH를 사용하여 Category와 함께 로딩 (LazyInitializationException 방지)
        return productRepository.findBySkuWithCategory(sku);
    }

    @Override
    public Product findBySlug(String slug) {
        logger.debug("Find product by Slug: {}", slug);
        // JOIN FETCH를 사용하여 Category와 함께 로딩 (LazyInitializationException 방지)
        return productRepository.findBySlugWithCategory(slug)
                .orElseThrow(() -> new ProductNotFoundException(slug, "slug"));
    }

    @Override
    public Optional<Product> findBySlugOptional(String slug) {
        logger.debug("Find product by Slug (Optional): {}", slug);
        // JOIN FETCH를 사용하여 Category와 함께 로딩 (LazyInitializationException 방지)
        return productRepository.findBySlugWithCategory(slug);
    }

    @Override
    @Transactional
    public Product updateProduct(Long id, Product productDetails) {
        logger.info("Update product details: id={}", id);

        // Category와 함께 로딩된 Product 가져오기
        Product existingProduct = findById(id); // 이미 Category가 로딩된 상태

        // If SKU changes, check duplicate
        if (!existingProduct.getSku().equals(productDetails.getSku()) &&
                existsBySku(productDetails.getSku())) {
            throw new ProductAlreadyExistsException("sku", productDetails.getSku());
        }

        // If slug changes, check duplicate
        if (!existingProduct.getSlug().equals(productDetails.getSlug()) &&
                existsBySlug(productDetails.getSlug())) {
            throw new ProductAlreadyExistsException("slug", productDetails.getSlug());
        }

        // Update only the fields that can be modified
        updateProductFields(existingProduct, productDetails);

        Product updatedProduct = productRepository.save(existingProduct);
        logger.info("Product details updated successfully: id={}", updatedProduct.getId());

        return updatedProduct;
    }

    @Override
    @Transactional
    public void deleteProduct(Long id) {
        logger.info("Soft delete product: id={}", id);

        Product product = findById(id);
        product.setIsActive(false);
        productRepository.save(product);

        logger.info("Product soft-deleted successfully: id={}", id);
    }

    @Override
    @Transactional
    public void deleteProductPermanently(Long id) {
        logger.info("Delete product permanently: id={}", id);

        Product product = findById(id);

        // TODO: implement check order history ( After implementing OrderService)
        // if (hasOrderHistory(product)) {
        // throw new InvalidProductDataException("Unable to delete a product with order
        // history");
        // }

        productRepository.delete(product);
        logger.info("Product successfully deleted permanently: id={}", id);
    }

    // Query products
    @Override
    public Page<Product> findAllActiveProducts(Pageable pageable) {
        logger.debug("Find active products (paginated): page={}, size={}",
                pageable.getPageNumber(), pageable.getPageSize());
        // @EntityGraph를 사용하여 Category와 함께 로딩 (LazyInitializationException 방지)
        return productRepository.findAllActiveProductsWithCategory(pageable);
    }

    @Override
    public List<Product> findFeaturedProducts() {
        logger.debug("Find featured products");
        // @EntityGraph를 사용하여 Category와 함께 로딩 (LazyInitializationException 방지)
        return productRepository.findFeaturedProductsWithCategory();
    }

    @Override
    public List<Product> findLatestProducts(int limit) {
        logger.debug("Find latest products: limit={}", limit);
        Pageable pageable = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "createdAt"));
        // @EntityGraph를 사용하여 Category와 함께 로딩 (LazyInitializationException 방지)
        return productRepository.findLatestProductsWithCategory(pageable).getContent();
    }

    @Override
    public Page<Product> findByPriceRange(BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable) {
        logger.debug("Find products by price range: ${} ~ ${}", minPrice, maxPrice);

        validatePriceRange(minPrice, maxPrice);

        // JOIN FETCH를 사용하여 Category와 함께 로딩 (LazyInitializationException 방지)
        return productRepository.findByPriceRangeWithCategory(minPrice, maxPrice, pageable);
    }

    @Override
    public Page<Product> findByCategoryId(Long categoryId, Pageable pageable) {
        logger.debug("Find products by category: categoryId={}", categoryId);

        // Validate category
        if (!categoryRepository.existsById(categoryId)) {
            throw new InvalidProductDataException("Category does not exists: " + categoryId);
        }

        // Category는 이미 쿼리에서 함께 로딩됨 (LazyInitializationException 방지)
        return productRepository.findByCategoryIdAndIsActiveTrueWithCategory(categoryId, pageable);
    }

    // Search & filtering

    @Override
    public Page<Product> searchProducts(String keyword, Pageable pageable) {
        logger.debug("Search product: keyword={}", keyword);

        if (!StringUtils.hasText(keyword)) {
            return findAllActiveProducts(pageable);
        }

        // JOIN FETCH를 사용하여 Category와 함께 로딩 (LazyInitializationException 방지)
        return productRepository.searchProductsWithCategory(keyword, pageable);
    }

    @Override
    public Page<Product> findWithFilters(Long categoryId, BigDecimal minPrice, BigDecimal maxPrice,
            String keyword, String sortBy, Pageable pageable) {
        logger.debug("Filtered product search: categoryId={}, price={}~{}, keyword={}, sortBy={}",
                categoryId, minPrice, maxPrice, keyword, sortBy);

        // Validate price range
        if (minPrice != null && maxPrice != null) {
            validatePriceRange(minPrice, maxPrice);
        }

        // Validate category
        if (categoryId != null && !categoryRepository.existsById(categoryId)) {
            throw new InvalidProductDataException("Category does not exists: " + categoryId);
        }

        // JOIN FETCH를 사용하여 Category와 함께 로딩 (LazyInitializationException 방지)
        return productRepository.findWithFiltersAndCategory(categoryId, minPrice, maxPrice, keyword, sortBy, pageable);
    }

    // Inventory management

    @Override
    public Integer getStockQuantity(Long productId) {
        Product product = findById(productId);
        return product.getStockQuantity();
    }

    @Override
    public boolean hasEnoughStock(Long productId, Integer requiredQuantity) {
        if (requiredQuantity == null || requiredQuantity <= 0) {
            return false;
        }

        Integer currentStock = getStockQuantity(productId);
        return currentStock >= requiredQuantity;
    }

    @Override
    @Transactional
    public void increaseStock(Long productId, Integer quantity) {
        logger.info("Increase quantity of the product: productId={}, quantity={}", productId, quantity);

        validateQuantity(quantity);

        Product product = findById(productId);
        product.setStockQuantity(product.getStockQuantity() + quantity);
        productRepository.save(product);

        logger.info("Quantity increased successfully: productId={}, newStock={}", productId,
                product.getStockQuantity());
    }

    @Override
    @Transactional
    public void decreaseStock(Long productId, Integer quantity) {
        logger.info("Decrease quantity of the product: productId={}, quantity={}", productId, quantity);

        validateQuantity(quantity);

        Product product = findById(productId);

        if (!hasEnoughStock(productId, quantity)) {
            throw new InsufficientStockException(
                    productId, product.getName(), quantity, product.getStockQuantity());
        }

        product.setStockQuantity(product.getStockQuantity() - quantity);
        productRepository.save(product);

        logger.info("Quantity decreased successfully: productId={}, newStock={}", productId,
                product.getStockQuantity());
    }

    @Override
    public List<Product> findLowStockProducts() {
        logger.debug("Find low stock products");

        // TODO: Comparing each product's lowStockThreshHold logic
        return productRepository.findByStockQuantityLessThanEqualAndIsActiveTrue(10);
    }

    @Override
    public List<Product> findProductsWithStock(Integer minStock) {
        logger.debug("Find products with enough stocks: minStock={}", minStock);

        return productRepository.findByStockQuantityGreaterThanAndIsActiveTrue(minStock);
    }

    // Product status management
    @Override
    @Transactional
    public void activateProduct(Long id) {
        logger.info("Activate product: id={}", id);

        Product product = findById(id);
        product.setIsActive(true);
        productRepository.save(product);

        logger.info("Product activated successfully: id={}", id);
    }

    @Override
    @Transactional
    public void deactivateProduct(Long id) {
        logger.info("Deactivate product: id={}", id);

        Product product = findById(id);
        product.setIsActive(false);
        productRepository.save(product);

        logger.info("Product deactivated successfully: id={}", id);
    }

    @Override
    @Transactional
    public void setAsFeatured(Long id) {
        logger.info("Set product as features: id={}", id);

        Product product = findById(id);
        product.setIsFeatured(true);
        productRepository.save(product);

        logger.info("Product set as featured successfully: id={}", id);
    }

    @Override
    @Transactional
    public void unsetAsFeatured(Long id) {
        logger.info("Unset featured product: id={}", id);

        Product product = findById(id);
        product.setIsFeatured(false);
        productRepository.save(product);

        logger.info("Featured product unset successfully: id={}", id);
    }

    // Validate & Utilities

    @Override
    public boolean existsBySku(String sku) {
        return productRepository.existsBySku(sku);
    }

    @Override
    public boolean existsBySlug(String slug) {
        return productRepository.existsBySlug(slug);
    }

    @Override
    public long countAllProducts() {
        return productRepository.count();
    }

    @Override
    public long countActiveProducts() {
        return productRepository.findByIsActiveTrueOrderByCreatedAtDesc(PageRequest.of(0, 1)).getTotalElements();
    }

    @Override
    public long countByCategoryId(Long categoryId) {
        return productRepository.findByCategoryIdAndIsActiveTrue(categoryId, PageRequest.of(0, 1)).getTotalElements();
    }

    // Helper methods

    /**
     * Validate product data for creation
     */
    private void validateProductForCreation(Product product) {
        if (product == null) {
            throw new InvalidProductDataException("Product information is required.");
        }

        if (!StringUtils.hasText(product.getName())) {
            throw new InvalidProductDataException("Product name is required.");
        }

        if (!StringUtils.hasText(product.getSku())) {
            throw new InvalidProductDataException("SKU is required.");
        }

        if (!StringUtils.hasText(product.getSlug())) {
            throw new InvalidProductDataException("Slug is required.");
        }

        if (product.getPrice() == null || product.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidProductDataException("price", product.getPrice());
        }

        if (product.getCategory() == null) {
            throw new InvalidProductDataException("Category is required.");
        }
    }

    /**
     * Validate category
     */
    private void validateCategory(Category category) {
        if (category == null || category.getId() == null) {
            throw new InvalidProductDataException("Category is required.");
        }

        if (!categoryRepository.existsById(category.getId())) {
            throw new InvalidProductDataException("Category does not exist: " + category.getId());
        }
    }

    /**
     * Update product fields
     */
    private void updateProductFields(Product existingProduct, Product productDetails) {
        if (StringUtils.hasText(productDetails.getName())) {
            existingProduct.setName(productDetails.getName());
        }
        if (StringUtils.hasText(productDetails.getDescription())) {
            existingProduct.setDescription(productDetails.getDescription());
        }
        if (StringUtils.hasText(productDetails.getShortDescription())) {
            existingProduct.setShortDescription(productDetails.getShortDescription());
        }
        if (StringUtils.hasText(productDetails.getSku())) {
            existingProduct.setSku(productDetails.getSku());
        }
        if (StringUtils.hasText(productDetails.getSlug())) {
            existingProduct.setSlug(productDetails.getSlug());
        }
        if (productDetails.getPrice() != null && productDetails.getPrice().compareTo(BigDecimal.ZERO) > 0) {
            existingProduct.setPrice(productDetails.getPrice());
        }
        if (productDetails.getComparePrice() != null
                && productDetails.getComparePrice().compareTo(BigDecimal.ZERO) > 0) {
            existingProduct.setComparePrice(productDetails.getComparePrice());
        }
        if (productDetails.getStockQuantity() != null && productDetails.getStockQuantity() >= 0) {
            existingProduct.setStockQuantity(productDetails.getStockQuantity());
        }
        if (productDetails.getLowStockThreshold() != null && productDetails.getLowStockThreshold() >= 0) {
            existingProduct.setLowStockThreshold(productDetails.getLowStockThreshold());
        }
        if (productDetails.getWeight() != null) {
            existingProduct.setWeight(productDetails.getWeight());
        }
        if (StringUtils.hasText(productDetails.getDimensions())) {
            existingProduct.setDimensions(productDetails.getDimensions());
        }
        if (StringUtils.hasText(productDetails.getMainImageUrl())) {
            existingProduct.setMainImageUrl(productDetails.getMainImageUrl());
        }
        if (productDetails.getCategory() != null) {
            validateCategory(productDetails.getCategory());
            existingProduct.setCategory(productDetails.getCategory());
        }
    }

    /**
     * Validate price range
     */
    private void validatePriceRange(BigDecimal minPrice, BigDecimal maxPrice) {
        if (minPrice.compareTo(BigDecimal.ZERO) < 0) {
            throw new InvalidProductDataException("Minimum price must be greater than equal to 0.");
        }
        if (maxPrice.compareTo(BigDecimal.ZERO) < 0) {
            throw new InvalidProductDataException("Maximum price must be greater than equal to 0.");
        }
        if (minPrice.compareTo(maxPrice) > 0) {
            throw new InvalidProductDataException("Minimum price cannot be greater than maximum price.");
        }
    }

    /**
     * Validate quantity
     */
    private void validateQuantity(Integer quantity) {
        if (quantity == null || quantity <= 0) {
            throw new InvalidProductDataException("Quantity must be greater than 0.");
        }
    }

    // ========================================
    // DTO 변환 메서드들 (LazyInitializationException 방지)
    // ========================================

    /**
     * ======== 단일 조회 DTO 메서드들 ========
     */

    @Override
    public ProductResponse findByIdAsDto(Long id) {
        logger.debug("Find product by ID as DTO: {}", id);
        Product product = findById(id); // Category가 이미 로딩된 상태
        return productDTOMapper.toResponse(product); // DTO 변환 수행
    }

    @Override
    public ProductResponse findBySlugAsDto(String slug) {
        logger.debug("Find product by slug as DTO: {}", slug);
        Product product = findBySlug(slug); // Category가 이미 로딩된 상태
        return productDTOMapper.toResponse(product); // DTO 변환 수행
    }

    /**
     * ProductUpdateRequest로 상품 업데이트 후 DTO 반환
     */
    @Override
    @Transactional
    public ProductResponse updateProductFromRequest(Long id, ProductUpdateRequest request) {
        logger.info("Update product from request: id={}", id);

        // Category와 함께 로딩된 Product 가져오기
        Product existingProduct = findById(id); // 이미 Category가 로딩된 상태

        // If SKU changes, check duplicate
        if (request.getSku() != null &&
                !existingProduct.getSku().equals(request.getSku()) &&
                existsBySku(request.getSku())) {
            throw new ProductAlreadyExistsException("sku", request.getSku());
        }

        // If slug changes, check duplicate
        if (request.getSlug() != null &&
                !existingProduct.getSlug().equals(request.getSlug()) &&
                existsBySlug(request.getSlug())) {
            throw new ProductAlreadyExistsException("slug", request.getSlug());
        }

        // ProductDTOMapper를 사용해서 업데이트
        productDTOMapper.updateEntity(existingProduct, request);

        Product updatedProduct = productRepository.save(existingProduct);
        logger.info("Product updated successfully: id={}", updatedProduct.getId());

        // DTO로 변환해서 반환 (트랜잭션 범위 내에서)
        return productDTOMapper.toResponse(updatedProduct);
    }

    /**
     * ======== 목록 조회 DTO 메서드들 ========
     */

    @Override
    public Page<ProductResponse> findAllActiveProductsAsDto(Pageable pageable) {
        logger.debug("Find active products as DTO (paginated): page={}, size={}",
                pageable.getPageNumber(), pageable.getPageSize());
        Page<Product> productPage = findAllActiveProducts(pageable); // Category가 이미 로딩된 상태
        return productPage.map(productDTOMapper::toSummaryResponse); // DTO 변환 수행
    }

    @Override
    public List<ProductResponse> findFeaturedProductsAsDto() {
        logger.debug("Find featured products as DTO");
        List<Product> products = findFeaturedProducts(); // Category가 이미 로딩된 상태
        return productDTOMapper.toSummaryResponseList(products); // DTO 변환 수행
    }

    @Override
    public List<ProductResponse> findLatestProductsAsDto(int limit) {
        logger.debug("Find latest products as DTO: limit={}", limit);
        List<Product> products = findLatestProducts(limit); // Category가 이미 로딩된 상태
        return productDTOMapper.toSummaryResponseList(products); // DTO 변환 수행
    }

    /**
     * ======== 검색/필터 DTO 메서드들 ========
     */

    @Override
    public Page<ProductResponse> searchProductsAsDto(String keyword, Pageable pageable) {
        logger.debug("Search products as DTO: keyword={}", keyword);
        Page<Product> productPage = searchProducts(keyword, pageable); // Category가 이미 로딩된 상태
        return productPage.map(productDTOMapper::toSummaryResponse); // DTO 변환 수행
    }

    @Override
    public Page<ProductResponse> findByCategoryIdAsDto(Long categoryId, Pageable pageable) {
        logger.debug("Find products by category as DTO: categoryId={}", categoryId);
        Page<Product> productPage = findByCategoryId(categoryId, pageable); // Category가 이미 로딩된 상태
        return productPage.map(productDTOMapper::toSummaryResponse); // DTO 변환 수행
    }

    @Override
    public Page<ProductResponse> findWithFiltersAsDto(Long categoryId, BigDecimal minPrice, BigDecimal maxPrice,
            String keyword, String sortBy, Pageable pageable) {
        logger.debug("Find products with filters as DTO");
        Page<Product> productPage = findWithFilters(categoryId, minPrice, maxPrice, keyword, sortBy, pageable); // Category가
                                                                                                                // 이미
                                                                                                                // 로딩된
                                                                                                                // 상태
        return productPage.map(productDTOMapper::toSummaryResponse); // DTO 변환 수행
    }
}
