package com.ashanhimantha.product_service.repository;

import com.ashanhimantha.product_service.entity.ProductVariant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductVariantRepository extends JpaRepository<ProductVariant, Long> {
    List<ProductVariant> findByProductId(Long productId);

    /**
     * Check if any product variants exist with any of the given sizes
     */
    @Query("SELECT CASE WHEN COUNT(pv) > 0 THEN true ELSE false END FROM ProductVariant pv WHERE pv.size IN :sizes")
    boolean existsByAnySize(@Param("sizes") List<String> sizes);
}
