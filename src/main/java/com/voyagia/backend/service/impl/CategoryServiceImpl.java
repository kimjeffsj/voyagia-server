package com.voyagia.backend.service.impl;

import com.voyagia.backend.entity.Category;
import com.voyagia.backend.exception.*;
import com.voyagia.backend.repository.CategoryRepository;
import com.voyagia.backend.repository.ProductRepository;
import com.voyagia.backend.service.CategoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class CategoryServiceImpl implements CategoryService {
    private static final Logger logger = LoggerFactory.getLogger(CategoryServiceImpl.class);
    private static final int MAX_CATEGORY_DEPTH = 5;

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository; // checking products

    public CategoryServiceImpl(CategoryRepository categoryRepository, ProductRepository productRepository) {
        this.categoryRepository = categoryRepository;
        this.productRepository = productRepository;
    }

    // CRUD
    @Override
    @Transactional
    public Category createCategory(Category category) {
        logger.info("Create category: name={}, slug={}", category.getName(), category.getSlug());

        // validate
        validateCategoryForCreation(category);

        // Check duplicate
        if (existsBySlug(category.getSlug())) {
            logger.warn("Category exists already with the slug: {}", category.getSlug());
            throw new CategoryAlreadyExistsException("slug", category.getSlug());
        }

        if (existsByName(category.getName())) {
            logger.warn("Category exists already with the name: {}", category.getName());
            throw new CategoryAlreadyExistsException("name", category.getName());
        }

        // validate parent and depth
        if (category.getParent() != null) {
            validateParentCategory(category.getParent().getId());
            validateCategoryDepth(category.getParent().getId());
        }

        if (category.getIsActive() == null) {
            category.setIsActive(true);
        }
        if (category.getSortOrder() == null) {
            category.setSortOrder(getNextSortOrder(category.getParent()));
        }

        // Save category
        Category savedCategory = categoryRepository.save(category);
        logger.info("Category created successfully: id={}, name={}", savedCategory.getId(), savedCategory.getName());

        return savedCategory;
    }

    @Override
    public Category findById(Long id) {
        logger.debug("Find category by ID: {}", id);
        return categoryRepository.findById(id)
                .orElseThrow(() -> new CategoryNotFoundException(id));
    }

    @Override
    public Optional<Category> findByIdOptional(Long id) {
        logger.debug("Find category by ID (Optional): {}", id);
        return categoryRepository.findById(id);
    }

    @Override
    public Category findBySlug(String slug) {
        logger.debug("Find category by slug: {}", slug);
        return categoryRepository.findBySlug(slug)
                .orElseThrow(() -> new CategoryNotFoundException(slug, "slug"));
    }

    @Override
    public Optional<Category> findBySlugOptional(String slug) {
        logger.debug("Find category by slug (Optional): {}", slug);
        return categoryRepository.findBySlug(slug);
    }

    @Override
    public Category findByName(String name) {
        logger.debug("Find category by name: {}", name);
        return categoryRepository.findByName(name)
                .orElseThrow(() -> new CategoryNotFoundException(name, "name"));
    }

    @Override
    public Optional<Category> findByNameOptional(String name) {
        logger.debug("Find category by name (Optional): {}", name);
        return categoryRepository.findByName(name);
    }

    @Override
    @Transactional
    public Category updateCategory(Long id, Category categoryDetails) {
        logger.info("Update category details: id={}", id);

        Category existingCategory = findById(id);

        // When slug changes validate
        if (!existingCategory.getSlug().equals(categoryDetails.getSlug()) &&
                existsBySlug(categoryDetails.getSlug())) {
            throw new CategoryAlreadyExistsException("slug", categoryDetails.getSlug());
        }

        // When name changes validate
        if (!existingCategory.getName().equals(categoryDetails.getName()) &&
                existsByName(categoryDetails.getName())) {
            throw new CategoryAlreadyExistsException("name", categoryDetails.getName());
        }

        // When parent changes validate
        if (categoryDetails.getParent() != null) {
            Long newParentId = categoryDetails.getParent().getId();
            if (!Objects.equals(existingCategory.getParent().getId(), newParentId)) {
                validateCircularReference(id, newParentId);
                validateCategoryDepth(newParentId);
            }
        }

        updateCategoryFields(existingCategory, categoryDetails);

        Category updatedCategory = categoryRepository.save(existingCategory);
        logger.info("Category updated successfully: id={}", updatedCategory.getId());

        return updatedCategory;
    }

    @Override
    @Transactional
    public void deleteCategory(Long id) {
        logger.info("Delete category(soft): id={}", id);

        Category category = findById(id);
        category.setIsActive(false);

        // deactivate sub categories
        deactivateDescendants(id);

        categoryRepository.save(category);
        logger.info("Category deleted(soft): id={}", id);
    }

    @Override
    @Transactional
    public void deleteCategoryPermanently(Long id) {
        logger.info("Delete category permanently: id={}", id);

        Category category = findById(id);

        // Validate deletion
        long productCount = getProductCount(id);
        long childrenCount = categoryRepository.findByParentId(id).size();

        if (productCount > 0 || childrenCount > 0) {
            throw new CategoryDeleteException(id, category.getName(),
                    Math.toIntExact(productCount), Math.toIntExact(childrenCount));
        }

        categoryRepository.delete(category);
        logger.info("Category permanently deleted: id={}", id);
    }

    // Category query and list
    @Override
    public List<Category> findAllActiveCategories() {
        logger.debug("Query all active categories");
        return categoryRepository.findByIsActiveTrueOrderBySortOrder();
    }

    @Override
    public Page<Category> findAllActiveCategoriesWithPagination(Pageable pageable) {
        logger.debug("Query all active categories (paginated): page={}, size={}",
                pageable.getPageNumber(), pageable.getPageSize());
        return categoryRepository.findByIsActiveTrue(pageable);
    }

    @Override
    public List<Category> findRootCategories() {
        logger.debug("Query root category");
        return categoryRepository.findRootCategories();
    }

    @Override
    public List<Category> findChildrenByParentId(Long parentId) {
        logger.debug("Query sub categories: parentId={}", parentId);

        // Check parent category
        if (!categoryRepository.existsById(parentId)) {
            throw new CategoryNotFoundException(parentId);
        }

        return categoryRepository.findByParentIdOrderBySortOrder(parentId);
    }

    @Override
    public List<Category> findAllDescendants(Long categoryId) {
        logger.debug("Query all descendant categories: categoryId={}", categoryId);

        Category category = findById(categoryId);
        List<Category> descendants = new ArrayList<>();
        collectDescendants(category, descendants);

        return descendants;
    }

    @Override
    public List<Category> findAllAncestors(Long categoryId) {
        logger.debug("Query all ancestor categories: categoryId={}", categoryId);

        Category category = findById(categoryId);
        List<Category> ancestors = new ArrayList<>();

        Category current = category.getParent();
        while (current != null) {
            ancestors.add(0, current);
            current = current.getParent();
        }

        return ancestors;
    }

    @Override
    public String getCategoryPath(Long categoryId) {
        logger.debug("Query category path: categoryId={}", categoryId);

        Category category = findById(categoryId);
        List<Category> ancestors = findAllAncestors(categoryId);

        List<String> pathElements = ancestors.stream()
                .map(Category::getName)
                .collect(Collectors.toList());
        pathElements.add(category.getName());

        return String.join(" > ", pathElements);
    }

    @Override
    @Transactional
    public Category moveCategory(Long categoryId, Long newParentId) {
        logger.info("Move category: categoryId={}, newParentId={}", categoryId, newParentId);

        Category category = findById(categoryId);

        // Validate new parent
        if (newParentId != null) {
            validateCircularReference(categoryId, newParentId);
            validateCategoryDepth(newParentId);

            Category newParent = findById(newParentId);
            category.setParent(newParent);
        } else {
            category.setParent(null); // Move to root
        }

        // Adjust sort order
        category.setSortOrder(getNextSortOrder(category.getParent()));

        Category movedCategory = categoryRepository.save(category);
        logger.info("Category moved successfully: categoryId={}", categoryId);

        return movedCategory;
    }

    // Hierarchy management
    @Override
    @Transactional
    public void updateSortOrder(Long categoryId, Integer newSortOrder) {
        logger.info("Update category sort order: categoryId={}, newSortOrder={}", categoryId, newSortOrder);

        Category category = findById(categoryId);
        category.setSortOrder(newSortOrder);
        categoryRepository.save(category);

        logger.info("Category sort order updated successfully: categoryId={}", categoryId);
    }

    @Override
    @Transactional
    public void reorderCategories(Long parentId, List<Long> categoryIds) {
        logger.info("Bulk reorder categories: parentId={}, categoryIds={}", parentId, categoryIds);

        for (int i = 0; i < categoryIds.size(); i++) {
            updateSortOrder(categoryIds.get(i), i + 1);
        }

        logger.info("Categories bulk reordered successfully: parentId={}", parentId);
    }

    @Override
    public List<Category> getCategoryTree() {
        logger.debug("Get complete category tree");
        return buildCategoryTree(findRootCategories());
    }

    @Override
    public List<Category> getCategorySubTree(Long rootCategoryId) {
        logger.debug("Get category subtree: rootCategoryId={}", rootCategoryId);

        Category rootCategory = findById(rootCategoryId);
        return buildCategoryTree(List.of(rootCategory));
    }

    @Override
    public int getCategoryDepth(Long categoryId) {
        logger.debug("Calculate category depth: categoryId={}", categoryId);

        Category category = findById(categoryId);
        int depth = 0;

        Category current = category.getParent();
        while (current != null) {
            depth++;
            current = current.getParent();
        }

        return depth;
    }

    @Override
    public boolean isAncestorOf(Long ancestorId, Long descendantId) {
        if (Objects.equals(ancestorId, descendantId)) {
            return false; // Same category is not an ancestor
        }

        try {
            Category descendant = findById(descendantId);
            Category current = descendant.getParent();

            while (current != null) {
                if (Objects.equals(current.getId(), ancestorId)) {
                    return true;
                }
                current = current.getParent();
            }

            return false;
        } catch (CategoryNotFoundException e) {
            return false;
        }
    }

    // Search and filtering
    @Override
    public Page<Category> searchCategories(String keyword, Pageable pageable) {
        logger.debug("Search categories: keyword={}", keyword);

        if (!StringUtils.hasText(keyword)) {
            return findAllActiveCategoriesWithPagination(pageable);
        }

        return categoryRepository.searchCategories(keyword, pageable);
    }

    @Override
    public List<Category> findCategoriesByDepth(int depth) {
        logger.debug("Find categories by depth: depth={}", depth);

        if (depth < 0) {
            throw new InvalidCategoryDataException("depth", depth);
        }

        if (depth == 0) {
            return findRootCategories();
        }

        // Complex logic to retrieve categories at specific depth
        // TODO: will need optimized query
        List<Category> result = new ArrayList<>();
        List<Category> currentLevel = findRootCategories();

        for (int currentDepth = 0; currentDepth < depth && !currentLevel.isEmpty(); currentDepth++) {
            List<Category> nextLevel = new ArrayList<>();
            for (Category category : currentLevel) {
                List<Category> children = categoryRepository.findByParentIdOrderBySortOrder(category.getId());
                nextLevel.addAll(children);
            }
            currentLevel = nextLevel;
        }

        return currentLevel;
    }

    // Category status management
    @Override
    @Transactional
    public void activateCategory(Long id) {
        logger.info("Activate category: id={}", id);

        Category category = findById(id);
        category.setIsActive(true);
        categoryRepository.save(category);

        logger.info("Category activated successfully: id={}", id);
    }

    @Override
    @Transactional
    public void deactivateCategory(Long id) {
        logger.info("Deactivate category: id={}", id);

        Category category = findById(id);
        category.setIsActive(false);

        // Deactivate all sub categories
        deactivateDescendants(id);

        categoryRepository.save(category);
        logger.info("Category deactivated successfully: id={}", id);
    }

    // Validate and utility
    @Override
    public boolean existsBySlug(String slug) {
        return categoryRepository.existsBySlug(slug);
    }

    @Override
    public boolean existsByName(String name) {
        return categoryRepository.existsByName(name);
    }

    @Override
    public boolean existsByNameAndParent(String name, Long parentId) {
        // Repository existsByNameAndParent method is not available yet, so provide temporary implementation
        // Temporary basic implementation provided
        List<Category> siblings = parentId != null ?
                categoryRepository.findByParentIdOrderBySortOrder(parentId) :
                categoryRepository.findRootCategories();

        return siblings.stream()
                .anyMatch(category -> category.getName().equals(name));
    }

    @Override
    public long getProductCount(Long categoryId) {
        logger.debug("Get product count for category: categoryId={}", categoryId);

        // Use ProductRepository's countByCategoryId method
        return productRepository.findByCategoryIdAndIsActiveTrue(categoryId, PageRequest.of(0, 1)).getTotalElements();
    }

    @Override
    public boolean hasChildren(Long categoryId) {
        return !categoryRepository.findByParentId(categoryId).isEmpty();
    }

    @Override
    public long countAllCategories() {
        return categoryRepository.count();
    }

    @Override
    public long countActiveCategories() {
        return categoryRepository.findByIsActiveTrue(PageRequest.of(0, 1)).getTotalElements();
    }

    @Override
    public long countRootCategories() {
        return categoryRepository.findRootCategories().size();
    }

    // Helper methods

    /**
     * Validate category data for creation
     */
    private void validateCategoryForCreation(Category category) {
        if (category == null) {
            throw new InvalidCategoryDataException("Category information is required.");
        }

        if (!StringUtils.hasText(category.getName())) {
            throw new InvalidCategoryDataException("Category name is required.");
        }

        if (!StringUtils.hasText(category.getSlug())) {
            throw new InvalidCategoryDataException("Slug is required.");
        }

        if (category.getName().length() > 100) {
            throw new InvalidCategoryDataException("Category name cannot exceed 100 characters.");
        }

        if (category.getSlug().length() > 100) {
            throw new InvalidCategoryDataException("Slug cannot exceed 100 characters.");
        }
    }

    /**
     * Validate parent category
     */
    private void validateParentCategory(Long parentId) {
        if (parentId != null && !categoryRepository.existsById(parentId)) {
            throw new CategoryNotFoundException(parentId);
        }
    }

    /**
     * Validate category depth limit
     */
    private void validateCategoryDepth(Long parentId) {
        if (parentId != null) {
            int parentDepth = getCategoryDepth(parentId);
            if (parentDepth >= MAX_CATEGORY_DEPTH - 1) {
                throw new InvalidCategoryDataException("Category depth exceeds maximum allowed depth: " + MAX_CATEGORY_DEPTH);
            }
        }
    }

    /**
     * Validate circular reference
     */
    private void validateCircularReference(Long categoryId, Long newParentId) {
        if (Objects.equals(categoryId, newParentId)) {
            throw new CircularReferenceException(categoryId, newParentId);
        }

        if (isAncestorOf(categoryId, newParentId)) {
            Category category = findById(categoryId);
            Category parent = findById(newParentId);
            throw new CircularReferenceException(categoryId, newParentId,
                    category.getName(), parent.getName());
        }
    }

    /**
     * Update category fields
     */
    private void updateCategoryFields(Category existingCategory, Category categoryDetails) {
        if (StringUtils.hasText(categoryDetails.getName())) {
            existingCategory.setName(categoryDetails.getName());
        }
        if (StringUtils.hasText(categoryDetails.getDescription())) {
            existingCategory.setDescription(categoryDetails.getDescription());
        }
        if (StringUtils.hasText(categoryDetails.getSlug())) {
            existingCategory.setSlug(categoryDetails.getSlug());
        }
        if (StringUtils.hasText(categoryDetails.getImageUrl())) {
            existingCategory.setImageUrl(categoryDetails.getImageUrl());
        }
        if (categoryDetails.getSortOrder() != null) {
            existingCategory.setSortOrder(categoryDetails.getSortOrder());
        }
        if (categoryDetails.getParent() != null) {
            existingCategory.setParent(categoryDetails.getParent());
        }
    }

    /**
     * Calculate next sort order
     */
    private Integer getNextSortOrder(Category parent) {
        List<Category> siblings = parent != null ?
                categoryRepository.findByParentIdOrderBySortOrder(parent.getId()) :
                categoryRepository.findRootCategories();

        return siblings.stream()
                .mapToInt(Category::getSortOrder)
                .max()
                .orElse(0) + 1;
    }

    /**
     * Recursively collect descendant categories
     */
    private void collectDescendants(Category category, List<Category> descendants) {
        List<Category> children = categoryRepository.findByParentIdOrderBySortOrder(category.getId());
        for (Category child : children) {
            descendants.add(child);
            collectDescendants(child, descendants);
        }
    }

    /**
     * Recursively deactivate descendant categories
     */
    private void deactivateDescendants(Long categoryId) {
        List<Category> children = categoryRepository.findByParentId(categoryId);
        for (Category child : children) {
            child.setIsActive(false);
            categoryRepository.save(child);
            deactivateDescendants(child.getId());
        }
    }

    /**
     * Build category tree structure
     */
    private List<Category> buildCategoryTree(List<Category> rootCategories) {
        for (Category category : rootCategories) {
            loadChildren(category);
        }
        return rootCategories;
    }

    /**
     * Recursively load category children
     */
    private void loadChildren(Category category) {
        List<Category> children = categoryRepository.findByParentIdOrderBySortOrder(category.getId());
        category.setChildren(children);

        for (Category child : children) {
            loadChildren(child);
        }
    }
}
