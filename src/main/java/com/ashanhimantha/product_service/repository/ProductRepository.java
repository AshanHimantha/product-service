package com.ashanhimantha.product_service.repository;

import com.ashanhimantha.product_service.entity.Product;
import com.ashanhimantha.product_service.entity.Category;
import com.ashanhimantha.product_service.entity.enums.Status;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;



@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {


    Page<Product> findByStatus(Status status, Pageable pageable);

    Page<Product> findByCategoryIdAndStatus(Long categoryId, Status status, Pageable pageable);

    // Check if any products are using this category
    boolean existsByCategory(Category category);

}