package com.ashanhimantha.product_service.repository;

import com.ashanhimantha.product_service.entity.Category;
import com.ashanhimantha.product_service.entity.CategoryType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    // Optional: for checking existence before delete in service
    Optional<Category> findById(Long id);

    // Check if any categories are using this category type
    boolean existsByCategoryType(CategoryType categoryType);

    // Permanent delete bypassing @SQLDelete annotation
    @Modifying
    @Query(value = "DELETE FROM categories WHERE id = :id", nativeQuery = true)
    void permanentlyDeleteById(@Param("id") Long id);
}