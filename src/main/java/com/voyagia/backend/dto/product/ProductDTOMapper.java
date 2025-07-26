package com.voyagia.backend.dto.product;

import com.voyagia.backend.entity.Category;
import com.voyagia.backend.entity.Product;
import com.voyagia.backend.service.CategoryService;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Product DTO Mapper
 */
@Component
public class ProductDTOMapper {
    private final CategoryService categoryService;

    public ProductDTOMapper(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    /**
     * ProductCreateRequest to Product Entity
     *
     * @param request ProductCreateRequest DTO
     * @return Product Entity
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
     * Product Entity to ProductResponse
     *
     * @param product Product Entity
     * @return ProductResponse DTO
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

        if (product.getCategory() != null) {
            Category category = product.getCategory();
            ProductResponse.CategorySummary categorySummary = new ProductResponse.CategorySummary(
                    category.getId(),
                    category.getName(),
                    category.getSlug()
            );
            response.setCategory(categorySummary);
        }

        response.setInStock(product.isInStock());
        response.setLowStock(product.isLowStock());
        response.setHasDiscount(product.hasDiscount());
        response.setDiscountAmount(product.getDiscountAmount());
        response.setDiscountPercentage(product.getDiscountPercentage());

        return response;
    }

    /**
     * Product Entity List to Product Response
     *
     * @param products Product Entity List
     * @return ProductResponse DTO List
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
     * Update Product Entity with ProductUpdateRequest data
     *
     * @param product Product Entity
     * @param request update request DTO
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
     * ProductUpdateRequest to Product Entity(Create new object)
     *
     * @param request Update request DTO
     * @return Product Entity
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
     * Product Entity to simplified response
     *
     * @param product Product Entity
     * @return simplified ProductResponse
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

        if (product.getCategory() != null) {
            Category category = product.getCategory();
            ProductResponse.CategorySummary categorySummary = new ProductResponse.CategorySummary(
                    category.getId(),
                    category.getName(),
                    category.getSlug()
            );
            response.setCategory(categorySummary);
        }

        response.setInStock(product.isInStock());
        response.setLowStock(product.isLowStock());
        response.setHasDiscount(product.hasDiscount());
        response.setDiscountAmount(product.getDiscountAmount());
        response.setDiscountPercentage(product.getDiscountPercentage());

        return response;
    }

    /**
     * Product Entity list to simplified Product response list
     *
     * @param products Product Entity List
     * @return Simplified ProductResponse DTO list
     */
    public List<ProductResponse> toSummaryResponseList(List<Product> products) {
        if (products == null) {
            return null;
        }

        return products.stream()
                .map(this::toSummaryResponse)
                .collect(Collectors.toList());
    }
}
