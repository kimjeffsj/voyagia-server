package com.voyagia.backend.repository;

import com.voyagia.backend.entity.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    // Basic Search
    Optional<Category> findBySlug(String slug);

    Optional<Category> findByName(String name);

    boolean existsBySlug(String slug);

    boolean existsByName(String name);

    // Active Categories
    List<Category> findByIsActiveTrueOrderBySortOrder();

    Page<Category> findByIsActiveTrue(Pageable pageable);

    // Parent-child Relationships
    List<Category> findByParentIsNullAndIsActiveTrueOrderBySortOrder(); // Root category

    List<Category> findByParentAndIsActiveTrueOrderBySortOrder(Category parent);

    List<Category> findByParentId(Long parentId);

    // Hierarchial Structure queries
    @Query("SELECT c FROM Category c WHERE c.parent IS NULL ORDER BY c.sortOrder")
    List<Category> findRootCategories();

    @Query("SELECT c FROM Category c WHERE c.parent.id = :parentId ORDER BY c.sortOrder")
    List<Category> findByParentIdOrderBySortOrder(@Param("parentId") Long parentId);
    
    // Alternative query without using parent.id navigation - use native SQL
    @Query(value = "SELECT * FROM categories WHERE parent_id = :parentId ORDER BY sort_order", nativeQuery = true)
    List<Category> findChildrenByParentId(@Param("parentId") Long parentId);

    // Search - improved with active filter and slug search
    @Query("SELECT c FROM Category c WHERE c.isActive = true AND (" +
            "LOWER(c.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(c.description) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(c.slug) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    Page<Category> searchCategories(@Param("searchTerm") String searchTerm, Pageable pageable);

    // Advanced search with multiple criteria
    @Query("SELECT c FROM Category c WHERE " +
            "(:activeOnly = false OR c.isActive = true) AND " +
            "(:searchTerm IS NULL OR :searchTerm = '' OR " +
            "LOWER(c.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(c.description) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(c.slug) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) " +
            "ORDER BY c.sortOrder ASC")
    Page<Category> searchCategoriesAdvanced(@Param("searchTerm") String searchTerm, 
                                           @Param("activeOnly") boolean activeOnly, 
                                           Pageable pageable);

    // Fetch JOIN queries to solve lazy loading issues
    @Query("SELECT c FROM Category c LEFT JOIN FETCH c.parent LEFT JOIN FETCH c.children WHERE c.id = :id")
    Optional<Category> findByIdWithParentAndChildren(@Param("id") Long id);

    @Query("SELECT c FROM Category c LEFT JOIN FETCH c.parent WHERE c.slug = :slug")
    Optional<Category> findBySlugWithParent(@Param("slug") String slug);

    @Query("SELECT c FROM Category c LEFT JOIN FETCH c.parent WHERE c.name = :name")
    Optional<Category> findByNameWithParent(@Param("name") String name);
}