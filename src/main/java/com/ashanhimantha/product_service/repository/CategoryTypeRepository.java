package com.ashanhimantha.product_service.repository;

import com.ashanhimantha.product_service.entity.CategoryType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CategoryTypeRepository extends JpaRepository<CategoryType, Long> {
    Optional<CategoryType> findByName(String name);

    boolean existsByName(String name);
}
