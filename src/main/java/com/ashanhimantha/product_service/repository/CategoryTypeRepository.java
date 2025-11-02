package com.ashanhimantha.product_service.repository;

import com.ashanhimantha.product_service.entity.CategoryType;
import com.ashanhimantha.product_service.entity.enums.SizingType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryTypeRepository extends JpaRepository<CategoryType, Long> {
    Optional<CategoryType> findByName(String name);
    List<CategoryType> findBySizingType(SizingType sizingType);
    boolean existsByName(String name);
}

