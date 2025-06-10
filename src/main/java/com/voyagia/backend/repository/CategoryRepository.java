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

    // Search
    @Query("SELECT c FROM Category c WHERE " +
            "LOWER(c.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(c.description) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    Page<Category> searchCategories(@Param("searchTerm") String searchTerm, Pageable pageable);
}