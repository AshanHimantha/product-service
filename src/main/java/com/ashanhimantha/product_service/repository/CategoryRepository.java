package com.ashanhimantha.product_service.repository;

import com.ashanhimantha.product_service.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    // Optional: for checking existence before delete in service
    Optional<Category> findById(Long id);
}