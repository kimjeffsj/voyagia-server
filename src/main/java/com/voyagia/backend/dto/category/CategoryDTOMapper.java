package com.voyagia.backend.dto.category;

import com.voyagia.backend.entity.Category;
import com.voyagia.backend.repository.CategoryRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Category DTO Mapper
 * Converts between Category entities and DTOs
 */
@Component
public class CategoryDTOMapper {

    private final CategoryRepository categoryRepository;

    public CategoryDTOMapper(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    /**
     * Convert CategoryCreateRequest to Category entity
     */
    public Category toEntity(CategoryCreateRequest request) {
        Category category = new Category();
        category.setName(request.getName());
        category.setDescription(request.getDescription());
        category.setSlug(request.getSlug());
        category.setImageUrl(request.getImageUrl());
        category.setIsActive(request.getIsActive());
        category.setSortOrder(request.getSortOrder());

        // Set parent if provided
        if (request.getParentId() != null) {
            Category parent = categoryRepository.findById(request.getParentId())
                    .orElseThrow(() -> new RuntimeException("Parent category not found"));
            category.setParent(parent);
        }

        return category;
    }

    /**
     * Convert CategoryUpdateRequest to Category entity (for updates)
     */
    public Category toUpdateEntity(CategoryUpdateRequest request) {
        Category category = new Category();
        category.setName(request.getName());
        category.setDescription(request.getDescription());
        category.setSlug(request.getSlug());
        category.setImageUrl(request.getImageUrl());
        category.setIsActive(request.getIsActive());
        category.setSortOrder(request.getSortOrder());

        // Set parent if provided
        if (request.getParentId() != null) {
            Category parent = categoryRepository.findById(request.getParentId())
                    .orElseThrow(() -> new RuntimeException("Parent category not found"));
            category.setParent(parent);
        }

        return category;
    }

    /**
     * Convert Category entity to CategoryResponse
     */
    public CategoryResponse toResponse(Category category) {
        CategoryResponse response = new CategoryResponse();
        response.setId(category.getId());
        response.setName(category.getName());
        response.setDescription(category.getDescription());
        response.setSlug(category.getSlug());
        response.setImageUrl(category.getImageUrl());
        response.setIsActive(category.getIsActive());
        response.setSortOrder(category.getSortOrder());
        response.setCreatedAt(category.getCreatedAt());
        response.setUpdatedAt(category.getUpdatedAt());

        // Set parent info
        if (category.getParent() != null) {
            response.setParent(new CategoryResponse.CategorySummary(
                    category.getParent().getId(),
                    category.getParent().getName(),
                    category.getParent().getSlug(),
                    category.getParent().getImageUrl(),
                    category.getParent().getSortOrder()
            ));
        }

        // Set children summaries (safely handle lazy loading)
        try {
            if (category.getChildren() != null && !category.getChildren().isEmpty()) {
                List<CategoryResponse.CategorySummary> childrenSummary = category.getChildren().stream()
                        .map(child -> new CategoryResponse.CategorySummary(
                                child.getId(),
                                child.getName(),
                                child.getSlug(),
                                child.getImageUrl(),
                                child.getSortOrder()
                        ))
                        .collect(Collectors.toList());
                response.setChildren(childrenSummary);
            }
        } catch (Exception e) {
            // Ignore lazy loading exception for children
            response.setChildren(null);
        }

        // Calculate path and depth
        response.setPath(buildCategoryPathSafe(category));
        response.setDepth(calculateDepthSafe(category));
        
        // Safely handle product count (avoid lazy loading)
        try {
            response.setProductCount((long) (category.getProducts() != null ? category.getProducts().size() : 0));
        } catch (Exception e) {
            response.setProductCount(0L);
        }
        
        // Safely handle children count (avoid lazy loading)
        try {
            response.setChildrenCount((long) (category.getChildren() != null ? category.getChildren().size() : 0));
        } catch (Exception e) {
            response.setChildrenCount(0L);
        }

        return response;
    }

    /**
     * Convert Category entity to CategoryTreeResponse (for tree structure)
     */
    public CategoryTreeResponse toTreeResponse(Category category) {
        CategoryTreeResponse response = new CategoryTreeResponse();
        response.setId(category.getId());
        response.setName(category.getName());
        response.setSlug(category.getSlug());
        response.setImageUrl(category.getImageUrl());
        response.setIsActive(category.getIsActive());
        response.setSortOrder(category.getSortOrder());

        response.setDepth(calculateDepthSafe(category));
        
        // Safely handle product count
        try {
            response.setProductCount((long) (category.getProducts() != null ? category.getProducts().size() : 0));
        } catch (Exception e) {
            response.setProductCount(0L);
        }

        // Recursively convert children (safely)
        try {
            if (category.getChildren() != null && !category.getChildren().isEmpty()) {
                List<CategoryTreeResponse> childrenResponse = category.getChildren().stream()
                        .map(this::toTreeResponse)
                        .collect(Collectors.toList());
                response.setChildren(childrenResponse);
            }
        } catch (Exception e) {
            // Ignore lazy loading exception for children
            response.setChildren(null);
        }

        return response;
    }

    /**
     * Convert list of Category entities to list of CategoryResponse
     */
    public List<CategoryResponse> toResponseList(List<Category> categories) {
        return categories.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Convert list of Category entities to list of CategoryTreeResponse
     */
    public List<CategoryTreeResponse> toTreeResponseList(List<Category> categories) {
        return categories.stream()
                .map(this::toTreeResponse)
                .collect(Collectors.toList());
    }

    /**
     * Convert Category entity to simple CategorySummary
     */
    public CategoryResponse.CategorySummary toSummary(Category category) {
        return new CategoryResponse.CategorySummary(
                category.getId(),
                category.getName(),
                category.getSlug(),
                category.getImageUrl(),
                category.getSortOrder()
        );
    }

    /**
     * Convert Category entity to simplified CategoryResponse (for children lists)
     */
    public CategoryResponse toSimpleResponse(Category category) {
        CategoryResponse response = new CategoryResponse();
        response.setId(category.getId());
        response.setName(category.getName());
        response.setDescription(category.getDescription());
        response.setSlug(category.getSlug());
        response.setImageUrl(category.getImageUrl());
        response.setIsActive(category.getIsActive());
        response.setSortOrder(category.getSortOrder());
        response.setCreatedAt(category.getCreatedAt());
        response.setUpdatedAt(category.getUpdatedAt());

        // Set basic info without complex relationships
        response.setPath(category.getName()); // Simple path for now
        response.setDepth(0); // Will be calculated later if needed
        response.setProductCount(0L);
        response.setChildrenCount(0L);
        response.setChildren(null); // No nested children
        response.setParent(null); // No parent info to avoid lazy loading

        return response;
    }

    /**
     * Convert list of Category entities to list of CategorySummary
     */
    public List<CategoryResponse.CategorySummary> toSummaryList(List<Category> categories) {
        return categories.stream()
                .map(this::toSummary)
                .collect(Collectors.toList());
    }

    /**
     * Convert list of Category entities to list of simple CategoryResponse (for children)
     */
    public List<CategoryResponse> toSimpleResponseList(List<Category> categories) {
        return categories.stream()
                .map(this::toSimpleResponse)
                .collect(Collectors.toList());
    }

    // Helper methods
    private String buildCategoryPath(Category category) {
        if (category.getParent() == null) {
            return category.getName();
        }
        return buildCategoryPath(category.getParent()) + " > " + category.getName();
    }

    private String buildCategoryPathSafe(Category category) {
        try {
            if (category.getParent() == null) {
                return category.getName();
            }
            return buildCategoryPathSafe(category.getParent()) + " > " + category.getName();
        } catch (Exception e) {
            return category.getName();
        }
    }

    private int calculateDepth(Category category) {
        int depth = 0;
        Category current = category.getParent();
        while (current != null) {
            depth++;
            current = current.getParent();
        }
        return depth;
    }

    private int calculateDepthSafe(Category category) {
        try {
            int depth = 0;
            Category current = category.getParent();
            while (current != null) {
                depth++;
                current = current.getParent();
            }
            return depth;
        } catch (Exception e) {
            return 0;
        }
    }
}