package com.voyagia.backend.controller;

import com.voyagia.backend.dto.common.ApiResponse;
import com.voyagia.backend.dto.product.*;
import com.voyagia.backend.entity.Product;
import com.voyagia.backend.exception.*;
import com.voyagia.backend.service.ProductService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Product Controller
 * <p>
 * Product management REST API
 * CRUD, Search, Filter, inventory
 */
@RestController
@RequestMapping("/products")
@CrossOrigin(origins = "*", maxAge = 3600)
public class ProductController {

    private static final Logger logger = LoggerFactory.getLogger(ProductController.class);

    private final ProductService productService;
    private final ProductDTOMapper productDTOMapper;

    public ProductController(ProductService productService, ProductDTOMapper productDTOMapper) {
        this.productService = productService;
        this.productDTOMapper = productDTOMapper;
    }

    /**
     * Create new product
     *
     * @param request       create product request DTO
     * @param bindingResult validation result
     * @return created product info
     */
    @PostMapping
    public ResponseEntity<?> createProduct(
            @Valid @RequestBody ProductCreateRequest request,
            BindingResult bindingResult
    ) {
        logger.info("Product creation attempt: name={}, sku={}", request.getName(), request.getSku());

        // Validation
        if (bindingResult.hasErrors()) {
            Map<String, String> errors = new HashMap<>();
            bindingResult.getFieldErrors().forEach(error -> errors.put(error.getField(), error.getDefaultMessage()));
            return ResponseEntity.badRequest().body(ApiResponse.error("Validation failed", errors));
        }

        try {
            Product product = productDTOMapper.toEntity(request);
            Product savedProduct = productService.createProduct(product);
            ProductResponse response = productDTOMapper.toResponse(savedProduct);

            logger.info("Product created successfully: id={}, sku={}",
                    savedProduct.getId(), savedProduct.getSku());

            return ResponseEntity.status(HttpStatus.CREATED).body(
                    ApiResponse.success("Product created successfully", response)
            );
        } catch (Exception e) {
            logger.error("Product creation failed: {}", e.getMessage());
            return ResponseEntity.badRequest().body(
                    ApiResponse.error("Product creation failed: " + e.getMessage())
            );
        }
    }

    /**
     * Get All Product (Paginated)
     *
     * @param page      page number
     * @param size      page size
     * @param sort      sort by
     * @param direction sort order
     * @param active    get only active
     * @return product list
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sort,
            @RequestParam(defaultValue = "desc") String direction,
            @RequestParam(defaultValue = "true") boolean active
    ) {
        logger.debug("Get products: page={}, size={}, sort={}, direction={}, active={}",
                page, size, sort, direction, active);

        try {
            Sort.Direction sortDirection = direction.equalsIgnoreCase("desc") ?
                    Sort.Direction.DESC : Sort.Direction.ASC;
            Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sort));

            Page<Product> productPage;
            if (active) {
                productPage = productService.findAllActiveProducts(pageable);
            } else {
                productPage = productService.findAllActiveProducts(pageable);
            }

            List<ProductResponse> products = productDTOMapper.toSummaryResponseList(productPage.getContent());

            Map<String, Object> response = new HashMap<>();
            response.put("products", products);
            response.put("currentPage", productPage.getNumber());
            response.put("totalItems", productPage.getTotalElements());
            response.put("totalPages", productPage.getTotalPages());
            response.put("pageSize", productPage.getSize());
            response.put("hasNext", productPage.hasNext());
            response.put("hasPrevious", productPage.hasPrevious());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Failed to get products: {}", e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("message", "Failed to retrieve products");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Get product by id
     *
     * @param id product Id
     * @return Product detail
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getProductById(@PathVariable Long id) {
        logger.debug("Get product by ID: {}", id);

        try {
            Product product = productService.findById(id);
            ProductResponse response = productDTOMapper.toResponse(product);

            return ResponseEntity.ok(
                    ApiResponse.success("Product retrieved successfully", response)
            );

        } catch (ProductNotFoundException e) {
            logger.warn("Product not found: id={}", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    ApiResponse.error("Product not found")
            );
        } catch (Exception e) {
            logger.error("Failed to get product: id={}, error={}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    ApiResponse.error("Failed to retrieve product")
            );
        }
    }

    /**
     * Get Product by slug
     *
     * @param slug product slug
     * @return product detail
     */
    @GetMapping("/slug/{slug}")
    public ResponseEntity<?> getProductBySlug(@PathVariable String slug) {
        logger.debug("Get product by slug: {}", slug);

        try {
            Product product = productService.findBySlug(slug);
            ProductResponse response = productDTOMapper.toResponse(product);

            return ResponseEntity.ok(
                    ApiResponse.success("Product retrieved successfully", response)
            );

        } catch (ProductNotFoundException e) {
            logger.warn("Product not found: slug={}", slug);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    ApiResponse.error("Product not found")
            );
        } catch (Exception e) {
            logger.error("Failed to get product: slug={}, error={}", slug, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    ApiResponse.error("Failed to retrieve product")
            );
        }
    }

    /**
     * Update product
     *
     * @param id            product id
     * @param request       update request DTO
     * @param bindingResult validation result
     * @return updated product detail
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateProduct(
            @PathVariable Long id,
            @Valid @RequestBody ProductUpdateRequest request,
            BindingResult bindingResult) {

        logger.info("Update product: id={}", id);

        // 검증 오류 처리
        if (bindingResult.hasErrors()) {
            Map<String, String> errors = new HashMap<>();
            bindingResult.getFieldErrors().forEach(error ->
                    errors.put(error.getField(), error.getDefaultMessage()));
            return ResponseEntity.badRequest().body(
                    ApiResponse.error("Validation failed", errors)
            );
        }

        // Check updated fields
        if (!request.hasAnyUpdateField()) {
            return ResponseEntity.badRequest().body(
                    ApiResponse.error("No fields to update")
            );
        }

        try {
            Product updateData = productDTOMapper.toUpdateEntity(request);
            Product updatedProduct = productService.updateProduct(id, updateData);
            ProductResponse response = productDTOMapper.toResponse(updatedProduct);

            logger.info("Product updated successfully: id={}", id);
            return ResponseEntity.ok(
                    ApiResponse.success("Product updated successfully", response)
            );

        } catch (ProductNotFoundException e) {
            logger.warn("Product not found for update: id={}", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    ApiResponse.error("Product not found")
            );
        } catch (Exception e) {
            logger.error("Product update failed: id={}, error={}", id, e.getMessage());
            return ResponseEntity.badRequest().body(
                    ApiResponse.error("Product update failed: " + e.getMessage())
            );
        }
    }

    /**
     * Delete product (soft - deactivate)
     *
     * @param id product id
     * @return result
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteProduct(@PathVariable Long id) {
        logger.info("Delete product: id={}", id);

        try {
            productService.deleteProduct(id);

            logger.info("Product deleted successfully: id={}", id);
            return ResponseEntity.ok(
                    ApiResponse.success("Product deleted successfully")
            );

        } catch (ProductNotFoundException e) {
            logger.warn("Product not found for deletion: id={}", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    ApiResponse.error("Product not found")
            );
        } catch (Exception e) {
            logger.error("Product deletion failed: id={}, error={}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    ApiResponse.error("Product deletion failed")
            );
        }
    }

    /**
     * Search product
     *
     * @param keyword   search keyword
     * @param page      page number
     * @param size      page size
     * @param sort      sort by
     * @param direction sort order
     * @return products list
     */
    @GetMapping("/search")
    public ResponseEntity<Map<String, Object>> searchProducts(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sort,
            @RequestParam(defaultValue = "desc") String direction) {

        logger.debug("Search products: keyword={}, page={}, size={}", keyword, page, size);

        try {
            Sort.Direction sortDirection = direction.equalsIgnoreCase("desc") ?
                    Sort.Direction.DESC : Sort.Direction.ASC;
            Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sort));

            Page<Product> productPage = productService.searchProducts(keyword, pageable);
            List<ProductResponse> products = productDTOMapper.toSummaryResponseList(productPage.getContent());

            Map<String, Object> response = new HashMap<>();
            response.put("products", products);
            response.put("keyword", keyword);
            response.put("currentPage", productPage.getNumber());
            response.put("totalItems", productPage.getTotalElements());
            response.put("totalPages", productPage.getTotalPages());
            response.put("pageSize", productPage.getSize());
            response.put("hasNext", productPage.hasNext());
            response.put("hasPrevious", productPage.hasPrevious());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Product search failed: keyword={}, error={}", keyword, e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("message", "Product search failed");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Advanced search
     *
     * @param searchRequest search request DTO
     * @param bindingResult Validation
     * @return filtered list
     */
    @PostMapping("/search/advanced")
    public ResponseEntity<?> advancedSearchProducts(
            @Valid @RequestBody ProductSearchRequest searchRequest,
            BindingResult bindingResult) {

        logger.debug("Advanced search products: {}", searchRequest);

        // Validation
        if (bindingResult.hasErrors()) {
            Map<String, String> errors = new HashMap<>();
            bindingResult.getFieldErrors().forEach(error ->
                    errors.put(error.getField(), error.getDefaultMessage()));
            return ResponseEntity.badRequest().body(
                    ApiResponse.error("Validation failed", errors)
            );
        }

        // price range
        if (!searchRequest.isPriceRangeValid()) {
            return ResponseEntity.badRequest().body(
                    ApiResponse.error("Invalid price range: minimum price cannot be greater than maximum price")
            );
        }

        try {
            Sort.Direction sortDirection = searchRequest.isDescending() ?
                    Sort.Direction.DESC : Sort.Direction.ASC;
            Pageable pageable = PageRequest.of(
                    searchRequest.getPage(),
                    searchRequest.getSize(),
                    Sort.by(sortDirection, searchRequest.getSortBy())
            );

            Page<Product> productPage = productService.findWithFilters(
                    searchRequest.getCategoryId(),
                    searchRequest.getMinPrice(),
                    searchRequest.getMaxPrice(),
                    searchRequest.getKeyword(),
                    searchRequest.getSortBy(),
                    pageable
            );

            List<ProductResponse> products = productDTOMapper.toSummaryResponseList(productPage.getContent());

            Map<String, Object> response = new HashMap<>();
            response.put("products", products);
            response.put("searchCriteria", searchRequest);
            response.put("currentPage", productPage.getNumber());
            response.put("totalItems", productPage.getTotalElements());
            response.put("totalPages", productPage.getTotalPages());
            response.put("pageSize", productPage.getSize());
            response.put("hasNext", productPage.hasNext());
            response.put("hasPrevious", productPage.hasPrevious());

            return ResponseEntity.ok(
                    ApiResponse.success("Advanced search completed", response)
            );

        } catch (Exception e) {
            logger.error("Advanced search failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    ApiResponse.error("Advanced search failed")
            );
        }
    }

    /**
     * Get Product by category
     *
     * @param categoryId category id
     * @param page       page number
     * @param size       page size
     * @param sort       sort by
     * @param direction  sort order
     * @return product list by category
     */
    @GetMapping("/category/{categoryId}")
    public ResponseEntity<Map<String, Object>> getProductsByCategory(
            @PathVariable Long categoryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sort,
            @RequestParam(defaultValue = "desc") String direction) {

        logger.debug("Get products by category: categoryId={}, page={}, size={}",
                categoryId, page, size);

        try {
            Sort.Direction sortDirection = direction.equalsIgnoreCase("desc") ?
                    Sort.Direction.DESC : Sort.Direction.ASC;
            Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sort));

            Page<Product> productPage = productService.findByCategoryId(categoryId, pageable);
            List<ProductResponse> products = productDTOMapper.toSummaryResponseList(productPage.getContent());

            Map<String, Object> response = new HashMap<>();
            response.put("products", products);
            response.put("categoryId", categoryId);
            response.put("currentPage", productPage.getNumber());
            response.put("totalItems", productPage.getTotalElements());
            response.put("totalPages", productPage.getTotalPages());
            response.put("pageSize", productPage.getSize());
            response.put("hasNext", productPage.hasNext());
            response.put("hasPrevious", productPage.hasPrevious());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Failed to get products by category: categoryId={}, error={}",
                    categoryId, e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("message", "Failed to retrieve products by category");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Get featured products
     *
     * @return featured product list
     */
    @GetMapping("/featured")
    public ResponseEntity<?> getFeaturedProducts() {
        logger.debug("Get featured products");

        try {
            List<Product> featuredProducts = productService.findFeaturedProducts();
            List<ProductResponse> products = productDTOMapper.toSummaryResponseList(featuredProducts);

            return ResponseEntity.ok(
                    ApiResponse.success("Featured products retrieved successfully", products)
            );

        } catch (Exception e) {
            logger.error("Failed to get featured products: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    ApiResponse.error("Failed to retrieve featured products")
            );
        }
    }

    /**
     * Latest product
     *
     * @param limit number of product
     * @return latest products list
     */
    @GetMapping("/latest")
    public ResponseEntity<?> getLatestProducts(
            @RequestParam(defaultValue = "10") int limit) {

        logger.debug("Get latest products: limit={}", limit);

        try {
            List<Product> latestProducts = productService.findLatestProducts(limit);
            List<ProductResponse> products = productDTOMapper.toSummaryResponseList(latestProducts);

            return ResponseEntity.ok(
                    ApiResponse.success("Latest products retrieved successfully", products)
            );

        } catch (Exception e) {
            logger.error("Failed to get latest products: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    ApiResponse.error("Failed to retrieve latest products")
            );
        }
    }

    /**
     * Low inventory products
     *
     * @return low inventory products list
     */
    @GetMapping("/low-stock")
    public ResponseEntity<?> getLowStockProducts() {
        logger.debug("Get low stock products");

        try {
            List<Product> lowStockProducts = productService.findLowStockProducts();
            List<ProductResponse> products = productDTOMapper.toSummaryResponseList(lowStockProducts);

            return ResponseEntity.ok(
                    ApiResponse.success("Low stock products retrieved successfully", products)
            );

        } catch (Exception e) {
            logger.error("Failed to get low stock products: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    ApiResponse.error("Failed to retrieve low stock products")
            );
        }
    }

    /**
     * Get Product stock
     *
     * @param id product id
     * @return product stock
     */
    @GetMapping("/{id}/stock")
    public ResponseEntity<?> getProductStock(@PathVariable Long id) {
        logger.debug("Get product stock: id={}", id);

        try {
            Integer stockQuantity = productService.getStockQuantity(id);

            Map<String, Object> stockInfo = new HashMap<>();
            stockInfo.put("productId", id);
            stockInfo.put("stockQuantity", stockQuantity);
            stockInfo.put("inStock", stockQuantity > 0);

            return ResponseEntity.ok(
                    ApiResponse.success("Stock information retrieved successfully", stockInfo)
            );

        } catch (ProductNotFoundException e) {
            logger.warn("Product not found for stock check: id={}", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    ApiResponse.error("Product not found")
            );
        } catch (Exception e) {
            logger.error("Failed to get product stock: id={}, error={}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    ApiResponse.error("Failed to retrieve stock information")
            );
        }
    }

    /**
     * Activate product
     *
     * @param id product id
     * @return result
     */
    @PutMapping("/{id}/activate")
    public ResponseEntity<?> activateProduct(@PathVariable Long id) {
        logger.info("Activate product: id={}", id);

        try {
            productService.activateProduct(id);

            logger.info("Product activated successfully: id={}", id);
            return ResponseEntity.ok(
                    ApiResponse.success("Product activated successfully")
            );

        } catch (ProductNotFoundException e) {
            logger.warn("Product not found for activation: id={}", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    ApiResponse.error("Product not found")
            );
        } catch (Exception e) {
            logger.error("Product activation failed: id={}, error={}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    ApiResponse.error("Product activation failed")
            );
        }
    }

    /**
     * 상품 비활성화
     *
     * @param id product id
     * @return result
     */
    @PutMapping("/{id}/deactivate")
    public ResponseEntity<?> deactivateProduct(@PathVariable Long id) {
        logger.info("Deactivate product: id={}", id);

        try {
            productService.deactivateProduct(id);

            logger.info("Product deactivated successfully: id={}", id);
            return ResponseEntity.ok(
                    ApiResponse.success("Product deactivated successfully")
            );

        } catch (ProductNotFoundException e) {
            logger.warn("Product not found for deactivation: id={}", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    ApiResponse.error("Product not found")
            );
        } catch (Exception e) {
            logger.error("Product deactivation failed: id={}, error={}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    ApiResponse.error("Product deactivation failed")
            );
        }
    }

    /**
     * Get product stats
     *
     * @return product stats
     */
    @GetMapping("/stats")
    public ResponseEntity<?> getProductStats() {
        logger.debug("Get product statistics");

        try {
            long totalProducts = productService.countAllProducts();
            long activeProducts = productService.countActiveProducts();
            long inactiveProducts = totalProducts - activeProducts;

            Map<String, Object> stats = new HashMap<>();
            stats.put("totalProducts", totalProducts);
            stats.put("activeProducts", activeProducts);
            stats.put("inactiveProducts", inactiveProducts);
            stats.put("activePercentage", totalProducts > 0 ?
                    (double) activeProducts / totalProducts * 100 : 0);

            return ResponseEntity.ok(
                    ApiResponse.success("Product statistics retrieved successfully", stats)
            );

        } catch (Exception e) {
            logger.error("Failed to get product statistics: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    ApiResponse.error("Failed to retrieve product statistics")
            );
        }
    }

    /**
     * SKU availability
     *
     * @param sku SKU
     * @return availability
     */
    @GetMapping("/check-sku")
    public ResponseEntity<Map<String, Object>> checkSkuAvailability(
            @RequestParam String sku) {
        logger.debug("Check SKU availability: {}", sku);

        boolean exists = productService.existsBySku(sku);

        Map<String, Object> response = new HashMap<>();
        response.put("exists", exists);
        response.put("available", !exists);
        response.put("sku", sku);

        return ResponseEntity.ok(response);
    }

    /**
     * Slug availability
     *
     * @param slug slug
     * @return availability
     */
    @GetMapping("/check-slug")
    public ResponseEntity<Map<String, Object>> checkSlugAvailability(
            @RequestParam String slug) {
        logger.debug("Check slug availability: {}", slug);

        boolean exists = productService.existsBySlug(slug);

        Map<String, Object> response = new HashMap<>();
        response.put("exists", exists);
        response.put("available", !exists);
        response.put("slug", slug);

        return ResponseEntity.ok(response);
    }

    // Exceptions

    /**
     * ProductNotFoundException
     */
    @ExceptionHandler(ProductNotFoundException.class)
    public ResponseEntity<ApiResponse<String>> handleProductNotFoundException(
            ProductNotFoundException e) {
        logger.warn("Product not found: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                ApiResponse.error("Product not found")
        );
    }

    /**
     * ProductAlreadyExistsException
     */
    @ExceptionHandler(ProductAlreadyExistsException.class)
    public ResponseEntity<ApiResponse<String>> handleProductAlreadyExistsException(
            ProductAlreadyExistsException e) {
        logger.warn("Product already exists: {}", e.getMessage());
        return ResponseEntity.badRequest().body(
                ApiResponse.error("Product already exists: " + e.getMessage())
        );
    }

    /**
     * InvalidProductDataException
     */
    @ExceptionHandler(InvalidProductDataException.class)
    public ResponseEntity<ApiResponse<String>> handleInvalidProductDataException(
            InvalidProductDataException e) {
        logger.warn("Invalid product data: {}", e.getMessage());
        return ResponseEntity.badRequest().body(
                ApiResponse.error("Invalid product data: " + e.getMessage())
        );
    }

    /**
     * CategoryNotFoundException
     */
    @ExceptionHandler(CategoryNotFoundException.class)
    public ResponseEntity<ApiResponse<String>> handleCategoryNotFoundException(
            CategoryNotFoundException e) {
        logger.warn("Category not found: {}", e.getMessage());
        return ResponseEntity.badRequest().body(
                ApiResponse.error("Category not found")
        );
    }

    /**
     * InsufficientStockException
     */
    @ExceptionHandler(InsufficientStockException.class)
    public ResponseEntity<ApiResponse<String>> handleInsufficientStockException(
            InsufficientStockException e) {
        logger.warn("Insufficient stock: {}", e.getMessage());
        return ResponseEntity.badRequest().body(
                ApiResponse.error("Insufficient stock: " + e.getMessage())
        );
    }

    /**
     * General exception
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<String>> handleGeneralException(Exception e) {
        logger.error("Unexpected error in ProductController: {}", e.getMessage(), e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                ApiResponse.error("Internal server error occurred")
        );
    }
}
