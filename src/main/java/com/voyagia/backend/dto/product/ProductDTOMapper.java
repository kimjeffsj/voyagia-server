package com.voyagia.backend.dto.product;

import com.voyagia.backend.entity.Category;
import com.voyagia.backend.entity.Product;
import com.voyagia.backend.service.CategoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Product DTO Mapper - 안전한 지연 로딩 접근 패턴 적용
 */
@Component
public class ProductDTOMapper {

    private static final Logger logger = LoggerFactory.getLogger(ProductDTOMapper.class);

    private final CategoryService categoryService;

    public ProductDTOMapper(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    /**
     * ProductCreateRequest to Product Entity
     */
    public Product toEntity(ProductCreateRequest request) {
        if (request == null) {
            return null;
        }

        Product product = new Product();
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setShortDescription(request.getShortDescription());
        product.setSku(request.getSku());
        product.setSlug(request.getSlug());
        product.setPrice(request.getPrice());
        product.setComparePrice(request.getComparePrice());
        product.setStockQuantity(request.getStockQuantity());
        product.setLowStockThreshold(request.getLowStockThreshold());
        product.setWeight(request.getWeight());
        product.setDimensions(request.getDimensions());
        product.setMainImageUrl(request.getMainImageUrl());
        product.setImageUrls(request.getImageUrls());
        product.setTags(request.getTags());
        product.setIsFeatured(request.getIsFeatured());
        product.setIsActive(true); // Default value: active

        // Category settings
        if (request.getCategoryId() != null) {
            Category category = categoryService.findById(request.getCategoryId());
            product.setCategory(category);
        }

        return product;
    }

    /**
     * Product Entity to ProductResponse - 안전한 Category 접근
     * 
     * 주의: 이 메서드는 Category가 이미 로딩된 Product 엔티티에서만 사용해야 합니다.
     * (JOIN FETCH 또는 @EntityGraph를 통해 로딩된 경우)
     */
    public ProductResponse toResponse(Product product) {
        if (product == null) {
            return null;
        }

        ProductResponse response = new ProductResponse();
        response.setId(product.getId());
        response.setName(product.getName());
        response.setDescription(product.getDescription());
        response.setShortDescription(product.getShortDescription());
        response.setSku(product.getSku());
        response.setSlug(product.getSlug());
        response.setPrice(product.getPrice());
        response.setComparePrice(product.getComparePrice());
        response.setStockQuantity(product.getStockQuantity());
        response.setLowStockThreshold(product.getLowStockThreshold());
        response.setIsActive(product.getIsActive());
        response.setIsFeatured(product.getIsFeatured());
        response.setWeight(product.getWeight());
        response.setDimensions(product.getDimensions());
        response.setMainImageUrl(product.getMainImageUrl());
        response.setImageUrls(product.getImageUrls());
        response.setTags(product.getTags());
        response.setCreatedAt(product.getCreatedAt());
        response.setUpdatedAt(product.getUpdatedAt());

        // 안전한 Category 접근 - 이미 로딩된 경우에만 접근
        try {
            Category category = product.getCategory();
            if (category != null) {
                // Category 정보가 실제로 로딩되었는지 확인하기 위해 ID에 접근
                Long categoryId = category.getId();
                if (categoryId != null) {
                    ProductResponse.CategorySummary categorySummary = new ProductResponse.CategorySummary(
                            category.getId(),
                            category.getName(),
                            category.getSlug());
                    response.setCategory(categorySummary);
                } else {
                    logger.warn("Category ID is null for product: {}", product.getId());
                }
            }
        } catch (Exception e) {
            // LazyInitializationException이 발생한 경우
            logger.error("Failed to access category for product {}: {}. Category was not properly loaded.",
                    product.getId(), e.getMessage());
            // Category 정보 없이 응답 반환
            response.setCategory(null);
        }

        // 비즈니스 로직 메서드들 (엔티티에 의존하지 않음)
        response.setInStock(product.isInStock());
        response.setLowStock(product.isLowStock());
        response.setHasDiscount(product.hasDiscount());
        response.setDiscountAmount(product.getDiscountAmount());
        response.setDiscountPercentage(product.getDiscountPercentage());

        return response;
    }

    /**
     * Product Entity to simplified response - 안전한 Category 접근
     */
    public ProductResponse toSummaryResponse(Product product) {
        if (product == null) {
            return null;
        }

        ProductResponse response = new ProductResponse();
        response.setId(product.getId());
        response.setName(product.getName());
        response.setShortDescription(product.getShortDescription());
        response.setSku(product.getSku());
        response.setSlug(product.getSlug());
        response.setPrice(product.getPrice());
        response.setComparePrice(product.getComparePrice());
        response.setStockQuantity(product.getStockQuantity());
        response.setIsActive(product.getIsActive());
        response.setIsFeatured(product.getIsFeatured());
        response.setMainImageUrl(product.getMainImageUrl());

        // 안전한 Category 접근
        try {
            Category category = product.getCategory();
            if (category != null) {
                // Category 정보가 실제로 로딩되었는지 확인
                Long categoryId = category.getId();
                if (categoryId != null) {
                    ProductResponse.CategorySummary categorySummary = new ProductResponse.CategorySummary(
                            category.getId(),
                            category.getName(),
                            category.getSlug());
                    response.setCategory(categorySummary);
                }
            }
        } catch (Exception e) {
            // LazyInitializationException이 발생한 경우
            logger.warn("Category not loaded for product {} in summary response: {}",
                    product.getId(), e.getMessage());
            response.setCategory(null);
        }

        // 비즈니스 로직 메서드들
        response.setInStock(product.isInStock());
        response.setLowStock(product.isLowStock());
        response.setHasDiscount(product.hasDiscount());
        response.setDiscountAmount(product.getDiscountAmount());
        response.setDiscountPercentage(product.getDiscountPercentage());

        return response;
    }

    /**
     * Product Entity List to Product Response
     */
    public List<ProductResponse> toResponseList(List<Product> products) {
        if (products == null) {
            return null;
        }

        return products.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Product Entity list to simplified Product response list
     */
    public List<ProductResponse> toSummaryResponseList(List<Product> products) {
        if (products == null) {
            return null;
        }

        return products.stream()
                .map(this::toSummaryResponse)
                .collect(Collectors.toList());
    }

    /**
     * Update Product Entity with ProductUpdateRequest data
     */
    public void updateEntity(Product product, ProductUpdateRequest request) {
        if (product == null || request == null) {
            return;
        }

        // Update where not null
        if (request.getName() != null) {
            product.setName(request.getName());
        }
        if (request.getDescription() != null) {
            product.setDescription(request.getDescription());
        }
        if (request.getShortDescription() != null) {
            product.setShortDescription(request.getShortDescription());
        }
        if (request.getSku() != null) {
            product.setSku(request.getSku());
        }
        if (request.getSlug() != null) {
            product.setSlug(request.getSlug());
        }
        if (request.getPrice() != null) {
            product.setPrice(request.getPrice());
        }
        if (request.getComparePrice() != null) {
            product.setComparePrice(request.getComparePrice());
        }
        if (request.getStockQuantity() != null) {
            product.setStockQuantity(request.getStockQuantity());
        }
        if (request.getLowStockThreshold() != null) {
            product.setLowStockThreshold(request.getLowStockThreshold());
        }
        if (request.getWeight() != null) {
            product.setWeight(request.getWeight());
        }
        if (request.getDimensions() != null) {
            product.setDimensions(request.getDimensions());
        }
        if (request.getMainImageUrl() != null) {
            product.setMainImageUrl(request.getMainImageUrl());
        }
        if (request.getImageUrls() != null) {
            product.setImageUrls(request.getImageUrls());
        }
        if (request.getTags() != null) {
            product.setTags(request.getTags());
        }
        if (request.getIsActive() != null) {
            product.setIsActive(request.getIsActive());
        }
        if (request.getIsFeatured() != null) {
            product.setIsFeatured(request.getIsFeatured());
        }

        // Update Category
        if (request.getCategoryId() != null) {
            Category category = categoryService.findById(request.getCategoryId());
            product.setCategory(category);
        }
    }

    /**
     * ProductUpdateRequest to Product Entity (Create new object)
     */
    public Product toUpdateEntity(ProductUpdateRequest request) {
        if (request == null) {
            return null;
        }

        Product product = new Product();
        updateEntity(product, request);
        return product;
    }

    /**
     * 안전한 Category 로딩 확인 메서드
     * 
     * @param product Product 엔티티
     * @return Category가 로딩되었는지 여부
     */
    public boolean isCategoryLoaded(Product product) {
        if (product == null) {
            return false;
        }

        try {
            Category category = product.getCategory();
            if (category != null) {
                // Category의 ID에 접근해서 실제로 로딩되었는지 확인
                category.getId();
                return true;
            }
        } catch (Exception e) {
            logger.debug("Category not loaded for product {}: {}", product.getId(), e.getMessage());
        }

        return false;
    }
}