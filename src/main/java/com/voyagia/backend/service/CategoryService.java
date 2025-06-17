package com.voyagia.backend.service;

import com.voyagia.backend.entity.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

/**
 * Category interface
 */
public interface CategoryService {

    // CRUD

    /**
     * Create Category
     *
     * @param category category
     * @return created category
     * @throws CategoryAlreadyExistsException slug or name is already exists
     * @throws InvalidCategoryDataException   invalid category data
     * @throws CircularReferenceException     Circular reference exceptions
     */
    Category createCategory(Category category);

    /**
     * Find category by ID
     *
     * @param id category ID
     * @return category
     * @throws CategoryNotFoundException category not found
     */
    Category findById(Long id);

    /**
     * Find category by ID (Optional)
     *
     * @param id category ID
     * @return category (Optional)
     */
    Optional<Category> findByIdOptional(Long id);


    /**
     * Find category by slug
     *
     * @param slug category slug
     * @return category
     * @throws CategoryNotFoundExcpetion category not found
     */
    Category findBySlug(String slug);

    /**
     * Find category by slug (optional)
     *
     * @param slug category slug
     * @return category (Optional)
     */
    Optional<Category> findBySlugOptional(String slug);

    /**
     * Find category by name
     *
     * @param name category name
     * @return category
     * @throws CategoryNotFoundException category not found
     */
    Category findByName(String name);

    /**
     * Find category by name(Optional)
     *
     * @param name category name
     * @return category (Optional)
     */
    Optional<Category> findByNameOptional(String name);

    /**
     * Update Category
     *
     * @param id              category ID
     * @param categoryDetails category details
     * @return updated category
     * @throws CategoryNotFoundException      category not found
     * @throws CategoryAlreadyExistsException slug/name already exists
     * @throws CircularReferenceException     circular reference occurred
     */
    Category updateCategory(Long id, Category categoryDetails);

    /**
     * Delete category(Soft delete - deactivate)
     *
     * @param id category ID
     * @throws CategoryNotFoundException category not found
     */
    void deleteCategory(Long id);

    /**
     * Delete category permanently
     * caution: if the category has sub category or items cannot delete
     *
     * @param id category ID
     * @throws CategoryNotFoundException category not found
     * @throws CategoryDeleteException   category cannot be deleted
     */
    void deleteCategoryPermanently(Long id);

    // Category query & list

    /**
     * Query all active categories
     *
     * @return active categories list
     */
    List<Category> findAllActiveCategories();

    /**
     * Query all active categories (paginated)
     *
     * @param pageable paging information
     * @return active categories list (paginated)
     */
    Page<Category> findAllActiveCategoriesWithPagination(Pageable pageable);

    /**
     * Query root categories
     *
     * @return root categories list
     */
    List<Category> findRootCategories();

    /**
     * Query sub categories under parent category
     *
     * @param parentId parent category ID
     * @return sub category list
     * @throws CategoryNotFoundException parent category not found
     */
    List<Category> findChildrenByParentId(Long parentId);

    /**
     * Find all descendant categories of a specific category (recursive).
     *
     * @param categoryId ancestor category ID
     * @return list of all descendant categories
     * @throws CategoryNotFoundException when category is not found
     */
    List<Category> findAllDescendants(Long categoryId);

    /**
     * Find all ancestor categories of a specific category (up to root).
     *
     * @param categoryId category ID
     * @return list of ancestor categories (ordered from root)
     * @throws CategoryNotFoundException when category is not found
     */
    List<Category> findAllAncestors(Long categoryId);

    /**
     * Get the full path of a specific category (from root to itself).
     *
     * @param categoryId category ID
     * @return category path (e.g., "Electronics > Smartphones > Android")
     * @throws CategoryNotFoundException when category is not found
     */
    String getCategoryPath(Long categoryId);

    // ================================
    // Hierarchy Management
    // ================================

    /**
     * Change the parent of a category.
     *
     * @param categoryId  category ID to move
     * @param newParentId new parent category ID (null to move to root)
     * @return updated category
     * @throws CategoryNotFoundException    when category is not found
     * @throws CircularReferenceException   when circular reference occurs
     * @throws InvalidCategoryDataException when invalid move request
     */
    Category moveCategory(Long categoryId, Long newParentId);

    /**
     * Change the sort order of a category.
     *
     * @param categoryId   category ID
     * @param newSortOrder new sort order
     * @throws CategoryNotFoundException when category is not found
     */
    void updateSortOrder(Long categoryId, Integer newSortOrder);

    /**
     * Bulk update sort order of categories with the same parent.
     *
     * @param parentId    parent category ID (null for root categories)
     * @param categoryIds list of category IDs in new order
     */
    void reorderCategories(Long parentId, List<Long> categoryIds);

    /**
     * Get the category tree structure.
     *
     * @return complete category tree structure
     */
    List<Category> getCategoryTree();

    /**
     * Get a subtree rooted at a specific category.
     *
     * @param rootCategoryId root category ID
     * @return subtree structure
     * @throws CategoryNotFoundException when category is not found
     */
    List<Category> getCategorySubTree(Long rootCategoryId);

    /**
     * Calculate the depth of a category.
     * Root category has depth 0.
     *
     * @param categoryId category ID
     * @return category depth
     * @throws CategoryNotFoundException when category is not found
     */
    int getCategoryDepth(Long categoryId);

    /**
     * Check if a category is an ancestor of another category.
     *
     * @param ancestorId   candidate ancestor category ID
     * @param descendantId candidate descendant category ID
     * @return whether ancestor relationship exists
     */
    boolean isAncestorOf(Long ancestorId, Long descendantId);

    // ================================
    // Search and Filtering


    /**
     * Search categories by keyword.
     * Searches based on category name and description.
     *
     * @param keyword  search keyword
     * @param pageable paging information
     * @return page of searched categories
     */
    Page<Category> searchCategories(String keyword, Pageable pageable);

    /**
     * Find categories by specific depth.
     *
     * @param depth category depth (0: root, 1: first level sub, ...)
     * @return list of categories at the specified depth
     */
    List<Category> findCategoriesByDepth(int depth);

    // ================================
    // Category Status Management
    // ================================

    /**
     * Activate a category.
     *
     * @param id category ID to activate
     * @throws CategoryNotFoundException when category is not found
     */
    void activateCategory(Long id);

    /**
     * Deactivate a category.
     * Child categories will also be deactivated.
     *
     * @param id category ID to deactivate
     * @throws CategoryNotFoundException when category is not found
     */
    void deactivateCategory(Long id);

    // ================================
    // Validation and Utilities
    // ================================

    /**
     * Check if a slug exists.
     *
     * @param slug slug to check
     * @return whether it exists
     */
    boolean existsBySlug(String slug);

    /**
     * Check if a name exists.
     *
     * @param name name to check
     * @return whether it exists
     */
    boolean existsByName(String name);

    /**
     * Check if a category with the same name exists under a specific parent.
     *
     * @param name     name to check
     * @param parentId parent category ID (null for root level)
     * @return whether it exists
     */
    boolean existsByNameAndParent(String name, Long parentId);

    /**
     * Get the number of products linked to a category.
     *
     * @param categoryId category ID
     * @return number of linked products
     * @throws CategoryNotFoundException when category is not found
     */
    long getProductCount(Long categoryId);

    /**
     * Check if a category has child categories.
     *
     * @param categoryId category ID
     * @return whether child categories exist
     * @throws CategoryNotFoundException when category is not found
     */
    boolean hasChildren(Long categoryId);

    /**
     * Get the total number of categories.
     *
     * @return total number of categories
     */
    long countAllCategories();

    /**
     * Get the number of active categories.
     *
     * @return number of active categories
     */
    long countActiveCategories();

    /**
     * Get the number of root categories.
     *
     * @return number of root categories
     */
    long countRootCategories();
}
