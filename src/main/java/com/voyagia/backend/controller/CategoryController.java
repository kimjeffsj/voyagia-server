package com.voyagia.backend.controller;

import com.voyagia.backend.dto.category.*;
import com.voyagia.backend.dto.common.ApiResponse;
import com.voyagia.backend.entity.Category;
import com.voyagia.backend.exception.*;
import com.voyagia.backend.service.CategoryService;
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
 * Category Controller
 * <p>
 * Category management REST API
 * CRUD, Search, Tree structure, Hierarchy management
 */
@RestController
@RequestMapping("/categories")
@CrossOrigin(origins = "*", maxAge = 3600)
public class CategoryController {

    private static final Logger logger = LoggerFactory.getLogger(CategoryController.class);

    private final CategoryService categoryService;
    private final CategoryDTOMapper categoryDTOMapper;

    public CategoryController(CategoryService categoryService, CategoryDTOMapper categoryDTOMapper) {
        this.categoryService = categoryService;
        this.categoryDTOMapper = categoryDTOMapper;
    }

    /**
     * Create new category
     *
     * @param request       create category request DTO
     * @param bindingResult validation result
     * @return created category info
     */
    @PostMapping
    public ResponseEntity<?> createCategory(
            @Valid @RequestBody CategoryCreateRequest request,
            BindingResult bindingResult) {
        logger.info("Category creation attempt: name={}, slug={}", request.getName(), request.getSlug());

        // Validation
        if (bindingResult.hasErrors()) {
            Map<String, String> errors = new HashMap<>();
            bindingResult.getFieldErrors().forEach(error -> errors.put(error.getField(), error.getDefaultMessage()));
            return ResponseEntity.badRequest().body(ApiResponse.error("Validation failed", errors));
        }

        try {
            Category category = categoryDTOMapper.toEntity(request);
            Category savedCategory = categoryService.createCategory(category);
            CategoryResponse response = categoryDTOMapper.toResponse(savedCategory);

            logger.info("Category created successfully: id={}, name={}",
                    savedCategory.getId(), savedCategory.getName());

            return ResponseEntity.status(HttpStatus.CREATED).body(
                    ApiResponse.success("Category created successfully", response));
        } catch (Exception e) {
            logger.error("Category creation failed: {}", e.getMessage());
            return ResponseEntity.badRequest().body(
                    ApiResponse.error("Category creation failed: " + e.getMessage()));
        }
    }

    /**
     * Get all categories (paginated)
     *
     * @param page      page number
     * @param size      page size
     * @param sort      sort by
     * @param direction sort order
     * @param active    get only active categories
     * @return category list
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllCategories(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "sortOrder") String sort,
            @RequestParam(defaultValue = "asc") String direction,
            @RequestParam(defaultValue = "true") boolean active) {
        logger.debug("Get categories: page={}, size={}, sort={}, direction={}, active={}",
                page, size, sort, direction, active);

        try {
            Sort.Direction sortDirection = direction.equalsIgnoreCase("desc") ? Sort.Direction.DESC
                    : Sort.Direction.ASC;
            Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sort));

            Page<Category> categoryPage;
            if (active) {
                categoryPage = categoryService.findAllActiveCategoriesWithPagination(pageable);
            } else {
                categoryPage = categoryService.findAllCategoriesWithPagination(pageable);
            }

            List<CategoryResponse> categoryResponses = categoryDTOMapper.toResponseList(categoryPage.getContent());

            Map<String, Object> response = new HashMap<>();
            response.put("categories", categoryResponses);
            response.put("currentPage", categoryPage.getNumber());
            response.put("totalItems", categoryPage.getTotalElements());
            response.put("totalPages", categoryPage.getTotalPages());
            response.put("pageSize", categoryPage.getSize());
            response.put("hasNext", categoryPage.hasNext());
            response.put("hasPrevious", categoryPage.hasPrevious());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Failed to get categories: page={}, size={}, sort={}, direction={}, active={}, error={}", 
                    page, size, sort, direction, active, e.getMessage(), e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("message", "Failed to retrieve categories");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Get category by id
     *
     * @param id category Id
     * @return Category detail
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getCategoryById(@PathVariable Long id) {
        logger.debug("Get category by ID: {}", id);

        try {
            Category category = categoryService.findById(id);
            CategoryResponse response = categoryDTOMapper.toResponse(category);

            return ResponseEntity.ok(
                    ApiResponse.success("Category retrieved successfully", response));

        } catch (CategoryNotFoundException e) {
            logger.warn("Category not found: id={}", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    ApiResponse.error("Category not found"));
        } catch (Exception e) {
            logger.error("Failed to get category: id={}, error={}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    ApiResponse.error("Failed to retrieve category"));
        }
    }

    /**
     * Get category by slug
     *
     * @param slug category slug
     * @return category detail
     */
    @GetMapping("/slug/{slug}")
    public ResponseEntity<?> getCategoryBySlug(@PathVariable String slug) {
        logger.debug("Get category by slug: {}", slug);

        try {
            Category category = categoryService.findBySlug(slug);
            CategoryResponse response = categoryDTOMapper.toResponse(category);

            return ResponseEntity.ok(
                    ApiResponse.success("Category retrieved successfully", response));

        } catch (CategoryNotFoundException e) {
            logger.warn("Category not found: slug={}", slug);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    ApiResponse.error("Category not found"));
        } catch (Exception e) {
            logger.error("Failed to get category: slug={}, error={}", slug, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    ApiResponse.error("Failed to retrieve category"));
        }
    }

    /**
     * Update category
     *
     * @param id            category id
     * @param request       update request DTO
     * @param bindingResult validation result
     * @return updated category detail
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateCategory(
            @PathVariable Long id,
            @Valid @RequestBody CategoryUpdateRequest request,
            BindingResult bindingResult) {

        logger.info("Update category: id={}", id);

        // Validation errors
        if (bindingResult.hasErrors()) {
            Map<String, String> errors = new HashMap<>();
            bindingResult.getFieldErrors().forEach(error -> errors.put(error.getField(), error.getDefaultMessage()));
            return ResponseEntity.badRequest().body(
                    ApiResponse.error("Validation failed", errors));
        }

        // Check if there are fields to update
        if (!request.hasAnyUpdateField()) {
            return ResponseEntity.badRequest().body(
                    ApiResponse.error("No fields to update"));
        }

        try {
            Category updateData = categoryDTOMapper.toUpdateEntity(request);
            Category updatedCategory = categoryService.updateCategory(id, updateData);
            CategoryResponse response = categoryDTOMapper.toResponse(updatedCategory);

            logger.info("Category updated successfully: id={}", id);
            return ResponseEntity.ok(
                    ApiResponse.success("Category updated successfully", response));

        } catch (CategoryNotFoundException e) {
            logger.warn("Category not found for update: id={}", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    ApiResponse.error("Category not found"));
        } catch (Exception e) {
            logger.error("Category update failed: id={}, error={}", id, e.getMessage());
            return ResponseEntity.badRequest().body(
                    ApiResponse.error("Category update failed: " + e.getMessage()));
        }
    }

    /**
     * Delete category (soft delete - deactivate)
     *
     * @param id category id
     * @return result
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteCategory(@PathVariable Long id) {
        logger.info("Delete category: id={}", id);

        try {
            categoryService.deleteCategory(id);

            logger.info("Category deleted successfully: id={}", id);
            return ResponseEntity.ok(
                    ApiResponse.success("Category deleted successfully"));

        } catch (CategoryNotFoundException e) {
            logger.warn("Category not found for deletion: id={}", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    ApiResponse.error("Category not found"));
        } catch (Exception e) {
            logger.error("Category deletion failed: id={}, error={}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    ApiResponse.error("Category deletion failed"));
        }
    }

    /**
     * Delete category permanently
     *
     * @param id category id
     * @return result
     */
    @DeleteMapping("/{id}/permanent")
    public ResponseEntity<?> deleteCategoryPermanently(@PathVariable Long id) {
        logger.info("Delete category permanently: id={}", id);

        try {
            categoryService.deleteCategoryPermanently(id);

            logger.info("Category permanently deleted: id={}", id);
            return ResponseEntity.ok(
                    ApiResponse.success("Category permanently deleted"));

        } catch (CategoryNotFoundException e) {
            logger.warn("Category not found for permanent deletion: id={}", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    ApiResponse.error("Category not found"));
        } catch (CategoryDeleteException e) {
            logger.warn("Category cannot be deleted: id={}, reason={}", id, e.getMessage());
            return ResponseEntity.badRequest().body(
                    ApiResponse.error("Category cannot be deleted: " + e.getMessage()));
        } catch (Exception e) {
            logger.error("Category permanent deletion failed: id={}, error={}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    ApiResponse.error("Category permanent deletion failed"));
        }
    }

    /**
     * Search categories
     *
     * @param keyword   search keyword
     * @param page      page number
     * @param size      page size
     * @param sort      sort by
     * @param direction sort order
     * @return categories list
     */
    @GetMapping("/search")
    public ResponseEntity<Map<String, Object>> searchCategories(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "sortOrder") String sort,
            @RequestParam(defaultValue = "asc") String direction) {

        logger.debug("Search categories: keyword={}, page={}, size={}", keyword, page, size);

        try {
            // URL decoding (Korean keyword support)
            String decodedKeyword = java.net.URLDecoder.decode(keyword, "UTF-8");
            logger.debug("Decoded keyword: {}", decodedKeyword);
            keyword = decodedKeyword;
        } catch (Exception e) {
            logger.debug("Keyword decoding failed, using original: {}", keyword);
        }

        try {
            Sort.Direction sortDirection = direction.equalsIgnoreCase("desc") ? Sort.Direction.DESC
                    : Sort.Direction.ASC;
            Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sort));

            Page<Category> categoryPage = categoryService.searchCategories(keyword, pageable);
            List<CategoryResponse> categoryResponses = categoryDTOMapper.toResponseList(categoryPage.getContent());

            Map<String, Object> response = new HashMap<>();
            response.put("categories", categoryResponses);
            response.put("keyword", keyword);
            response.put("currentPage", categoryPage.getNumber());
            response.put("totalItems", categoryPage.getTotalElements());
            response.put("totalPages", categoryPage.getTotalPages());
            response.put("pageSize", categoryPage.getSize());
            response.put("hasNext", categoryPage.hasNext());
            response.put("hasPrevious", categoryPage.hasPrevious());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Category search failed: keyword={}, error={}", keyword, e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("message", "Category search failed");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Advanced search categories
     *
     * @param searchRequest search request DTO
     * @param bindingResult validation
     * @return filtered list
     */
    @PostMapping("/search/advanced")
    public ResponseEntity<?> advancedSearchCategories(
            @Valid @RequestBody CategorySearchRequest searchRequest,
            BindingResult bindingResult) {

        logger.debug("Advanced search categories: {}", searchRequest);

        // Validation
        if (bindingResult.hasErrors()) {
            Map<String, String> errors = new HashMap<>();
            bindingResult.getFieldErrors().forEach(error -> errors.put(error.getField(), error.getDefaultMessage()));
            return ResponseEntity.badRequest().body(
                    ApiResponse.error("Validation failed", errors));
        }

        try {
            Sort.Direction sortDirection = searchRequest.isDescending() ? Sort.Direction.DESC : Sort.Direction.ASC;
            Pageable pageable = PageRequest.of(
                    searchRequest.getPage(),
                    searchRequest.getSize(),
                    Sort.by(sortDirection, searchRequest.getSortBy()));

            Page<Category> categoryPage;
            if (searchRequest.hasKeyword()) {
                categoryPage = categoryService.searchCategoriesAdvanced(
                    searchRequest.getKeyword(), 
                    true, // activeOnly = true for advanced search
                    pageable);
            } else {
                categoryPage = categoryService.findAllActiveCategoriesWithPagination(pageable);
            }

            List<CategoryResponse> categoryResponses = categoryDTOMapper.toResponseList(categoryPage.getContent());

            Map<String, Object> response = new HashMap<>();
            response.put("categories", categoryResponses);
            response.put("searchCriteria", searchRequest);
            response.put("currentPage", categoryPage.getNumber());
            response.put("totalItems", categoryPage.getTotalElements());
            response.put("totalPages", categoryPage.getTotalPages());
            response.put("pageSize", categoryPage.getSize());
            response.put("hasNext", categoryPage.hasNext());
            response.put("hasPrevious", categoryPage.hasPrevious());

            return ResponseEntity.ok(
                    ApiResponse.success("Advanced search completed", response));

        } catch (Exception e) {
            logger.error("Advanced search failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    ApiResponse.error("Advanced search failed"));
        }
    }

    /**
     * Get category tree structure
     *
     * @return complete category tree
     */
    @GetMapping("/tree")
    public ResponseEntity<?> getCategoryTree() {
        logger.debug("Get category tree");

        try {
            List<Category> categoryTree = categoryService.getCategoryTree();
            List<CategoryTreeResponse> treeResponse = categoryDTOMapper.toTreeResponseList(categoryTree);

            return ResponseEntity.ok(
                    ApiResponse.success("Category tree retrieved successfully", treeResponse));

        } catch (Exception e) {
            logger.error("Failed to get category tree: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    ApiResponse.error("Failed to retrieve category tree"));
        }
    }

    /**
     * Get category subtree
     *
     * @param rootCategoryId root category ID
     * @return subtree structure
     */
    @GetMapping("/{rootCategoryId}/subtree")
    public ResponseEntity<?> getCategorySubTree(@PathVariable Long rootCategoryId) {
        logger.debug("Get category subtree: rootCategoryId={}", rootCategoryId);

        try {
            List<Category> subTree = categoryService.getCategorySubTree(rootCategoryId);
            List<CategoryTreeResponse> treeResponse = categoryDTOMapper.toTreeResponseList(subTree);

            return ResponseEntity.ok(
                    ApiResponse.success("Category subtree retrieved successfully", treeResponse));

        } catch (CategoryNotFoundException e) {
            logger.warn("Root category not found: id={}", rootCategoryId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    ApiResponse.error("Root category not found"));
        } catch (Exception e) {
            logger.error("Failed to get category subtree: rootCategoryId={}, error={}",
                    rootCategoryId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    ApiResponse.error("Failed to retrieve category subtree"));
        }
    }

    /**
     * Get root categories
     *
     * @return root categories list
     */
    @GetMapping("/root")
    public ResponseEntity<?> getRootCategories() {
        logger.debug("Get root categories");

        try {
            List<Category> rootCategories = categoryService.findRootCategories();
            List<CategoryResponse> categoryResponses = categoryDTOMapper.toResponseList(rootCategories);

            return ResponseEntity.ok(
                    ApiResponse.success("Root categories retrieved successfully", categoryResponses));

        } catch (Exception e) {
            logger.error("Failed to get root categories: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    ApiResponse.error("Failed to retrieve root categories"));
        }
    }

    /**
     * Get children categories by parent ID
     *
     * @param parentId parent category ID
     * @return children categories list
     */
    @GetMapping("/{parentId}/children")
    public ResponseEntity<?> getChildrenCategories(@PathVariable Long parentId) {
        logger.debug("Get children categories: parentId={}", parentId);

        try {
            List<Category> children = categoryService.findChildrenByParentId(parentId);
            List<CategoryResponse> categoryResponses = categoryDTOMapper.toSimpleResponseList(children);

            return ResponseEntity.ok(
                    ApiResponse.success("Children categories retrieved successfully", categoryResponses));

        } catch (CategoryNotFoundException e) {
            logger.warn("Parent category not found: id={}", parentId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    ApiResponse.error("Parent category not found"));
        } catch (Exception e) {
            logger.error("Failed to get children categories: parentId={}, error={}",
                    parentId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    ApiResponse.error("Failed to retrieve children categories"));
        }
    }

    /**
     * Move category to different parent
     *
     * @param categoryId   category ID to move
     * @param newParentId  new parent category ID (null for root)
     * @return updated category
     */
    @PutMapping("/{categoryId}/move")
    public ResponseEntity<?> moveCategory(
            @PathVariable Long categoryId,
            @RequestParam(required = false) Long newParentId) {
        logger.info("Move category: categoryId={}, newParentId={}", categoryId, newParentId);

        try {
            Category movedCategory = categoryService.moveCategory(categoryId, newParentId);
            CategoryResponse response = categoryDTOMapper.toResponse(movedCategory);

            logger.info("Category moved successfully: categoryId={}", categoryId);
            return ResponseEntity.ok(
                    ApiResponse.success("Category moved successfully", response));

        } catch (CategoryNotFoundException e) {
            logger.warn("Category not found for move: categoryId={}", categoryId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    ApiResponse.error("Category not found"));
        } catch (CircularReferenceException e) {
            logger.warn("Circular reference in category move: {}", e.getMessage());
            return ResponseEntity.badRequest().body(
                    ApiResponse.error("Circular reference: " + e.getMessage()));
        } catch (Exception e) {
            logger.error("Category move failed: categoryId={}, error={}", categoryId, e.getMessage());
            return ResponseEntity.badRequest().body(
                    ApiResponse.error("Category move failed: " + e.getMessage()));
        }
    }

    /**
     * Reorder categories
     *
     * @param parentId    parent category ID (null for root)
     * @param categoryIds list of category IDs in new order
     * @return result
     */
    @PutMapping("/reorder")
    public ResponseEntity<?> reorderCategories(
            @RequestParam(required = false) Long parentId,
            @RequestBody List<Long> categoryIds) {
        logger.info("Reorder categories: parentId={}, categoryIds={}", parentId, categoryIds);

        try {
            categoryService.reorderCategories(parentId, categoryIds);

            logger.info("Categories reordered successfully: parentId={}", parentId);
            return ResponseEntity.ok(
                    ApiResponse.success("Categories reordered successfully"));

        } catch (Exception e) {
            logger.error("Category reorder failed: parentId={}, error={}", parentId, e.getMessage());
            return ResponseEntity.badRequest().body(
                    ApiResponse.error("Category reorder failed: " + e.getMessage()));
        }
    }

    /**
     * Activate category
     *
     * @param id category id
     * @return result
     */
    @PutMapping("/{id}/activate")
    public ResponseEntity<?> activateCategory(@PathVariable Long id) {
        logger.info("Activate category: id={}", id);

        try {
            categoryService.activateCategory(id);

            logger.info("Category activated successfully: id={}", id);
            return ResponseEntity.ok(
                    ApiResponse.success("Category activated successfully"));

        } catch (CategoryNotFoundException e) {
            logger.warn("Category not found for activation: id={}", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    ApiResponse.error("Category not found"));
        } catch (Exception e) {
            logger.error("Category activation failed: id={}, error={}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    ApiResponse.error("Category activation failed"));
        }
    }

    /**
     * Deactivate category
     *
     * @param id category id
     * @return result
     */
    @PutMapping("/{id}/deactivate")
    public ResponseEntity<?> deactivateCategory(@PathVariable Long id) {
        logger.info("Deactivate category: id={}", id);

        try {
            categoryService.deactivateCategory(id);

            logger.info("Category deactivated successfully: id={}", id);
            return ResponseEntity.ok(
                    ApiResponse.success("Category deactivated successfully"));

        } catch (CategoryNotFoundException e) {
            logger.warn("Category not found for deactivation: id={}", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    ApiResponse.error("Category not found"));
        } catch (Exception e) {
            logger.error("Category deactivation failed: id={}, error={}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    ApiResponse.error("Category deactivation failed"));
        }
    }

    /**
     * Get category statistics
     *
     * @return category statistics
     */
    @GetMapping("/stats")
    public ResponseEntity<?> getCategoryStats() {
        logger.debug("Get category statistics");

        try {
            long totalCategories = categoryService.countAllCategories();
            long activeCategories = categoryService.countActiveCategories();
            long rootCategories = categoryService.countRootCategories();
            long inactiveCategories = totalCategories - activeCategories;

            Map<String, Object> stats = new HashMap<>();
            stats.put("totalCategories", totalCategories);
            stats.put("activeCategories", activeCategories);
            stats.put("inactiveCategories", inactiveCategories);
            stats.put("rootCategories", rootCategories);
            stats.put("activePercentage", totalCategories > 0 ? (double) activeCategories / totalCategories * 100 : 0);

            return ResponseEntity.ok(
                    ApiResponse.success("Category statistics retrieved successfully", stats));

        } catch (Exception e) {
            logger.error("Failed to get category statistics: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    ApiResponse.error("Failed to retrieve category statistics"));
        }
    }

    /**
     * Check slug availability
     *
     * @param slug slug
     * @return availability
     */
    @GetMapping("/check-slug")
    public ResponseEntity<Map<String, Object>> checkSlugAvailability(
            @RequestParam String slug) {
        logger.debug("Check slug availability: {}", slug);

        boolean exists = categoryService.existsBySlug(slug);

        Map<String, Object> response = new HashMap<>();
        response.put("exists", exists);
        response.put("available", !exists);
        response.put("slug", slug);

        return ResponseEntity.ok(response);
    }

    /**
     * Check name availability
     *
     * @param name name
     * @return availability
     */
    @GetMapping("/check-name")
    public ResponseEntity<Map<String, Object>> checkNameAvailability(
            @RequestParam String name) {
        logger.debug("Check name availability: {}", name);

        boolean exists = categoryService.existsByName(name);

        Map<String, Object> response = new HashMap<>();
        response.put("exists", exists);
        response.put("available", !exists);
        response.put("name", name);

        return ResponseEntity.ok(response);
    }

    // Note: Exception handling is now managed by GlobalExceptionHandler
}