package com.ashanhimantha.product_service.repository;

import com.ashanhimantha.product_service.entity.Product;
import com.ashanhimantha.product_service.entity.enums.ProductStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;


@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {


    Page<Product> findByStatus(ProductStatus status, Pageable pageable);

    Page<Product> findByCategoryIdAndStatus(Long categoryId, ProductStatus status, Pageable pageable);

    Optional<Product> findByIdAndSupplierId(Long id, String supplierId);

}