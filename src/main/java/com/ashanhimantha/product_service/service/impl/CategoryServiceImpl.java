package com.ashanhimantha.product_service.service.impl;


import com.ashanhimantha.product_service.dto.request.CategoryRequest;
import com.ashanhimantha.product_service.entity.Category;
import com.ashanhimantha.product_service.entity.CategoryType;
import com.ashanhimantha.product_service.exception.DuplicateResourceException;
import com.ashanhimantha.product_service.exception.ResourceNotFoundException;
import com.ashanhimantha.product_service.repository.CategoryRepository;
import com.ashanhimantha.product_service.repository.CategoryTypeRepository;
import com.ashanhimantha.product_service.repository.ProductRepository;
import com.ashanhimantha.product_service.service.CategoryService;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryTypeRepository categoryTypeRepository;
    private final ProductRepository productRepository;

    public CategoryServiceImpl(CategoryRepository categoryRepository,
                               CategoryTypeRepository categoryTypeRepository,
                               ProductRepository productRepository) {
        this.categoryRepository = categoryRepository;
        this.categoryTypeRepository = categoryTypeRepository;
        this.productRepository = productRepository;
    }

    @Override
    public Category createCategory(CategoryRequest categoryRequest) {
        Category category = new Category();
        category.setName(categoryRequest.getName());
        category.setDescription(categoryRequest.getDescription());

        // Set category type if provided
        if (categoryRequest.getCategoryTypeId() != null) {
            CategoryType categoryType = categoryTypeRepository.findById(categoryRequest.getCategoryTypeId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category type not found with id: " + categoryRequest.getCategoryTypeId()));
            category.setCategoryType(categoryType);
        }

        try {
            return categoryRepository.save(category);
        } catch (DataIntegrityViolationException e) {
            throw new DuplicateResourceException("A category with the name '" + categoryRequest.getName() + "' already exists.");
        }
    }

    @Override
    public Category getCategoryById(Long categoryId) {
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + categoryId));
    }

    @Override
    public Page<Category> getAllCategories(Pageable pageable) {
        return categoryRepository.findAll(pageable);
    }

    @Override
    public List<Category> getAllCategoriesAsList() {
        return categoryRepository.findAll();
    }

    @Override // ADD THIS METHOD
    public Category updateCategory(Long categoryId, CategoryRequest categoryRequest) {
        Category existingCategory = getCategoryById(categoryId); // Find first, will throw 404 if not found
        existingCategory.setName(categoryRequest.getName());
        existingCategory.setDescription(categoryRequest.getDescription());

        // Update category type if provided
        if (categoryRequest.getCategoryTypeId() != null) {
            CategoryType categoryType = categoryTypeRepository.findById(categoryRequest.getCategoryTypeId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category type not found with id: " + categoryRequest.getCategoryTypeId()));
            existingCategory.setCategoryType(categoryType);
        } else {
            existingCategory.setCategoryType(null);
        }

        try {
            return categoryRepository.save(existingCategory);
        } catch (DataIntegrityViolationException e) {
            throw new DuplicateResourceException("A category with the name '" + categoryRequest.getName() + "' already exists.");
        }
    }

    @Override // ADD THIS METHOD
    @Transactional
    public void deleteCategory(Long categoryId) {
        Category category = getCategoryById(categoryId); // Find first, will throw 404 if not found

        // Check if any products are using this category
        if (productRepository.existsByCategory(category)) {
            // Soft delete: Set status to INACTIVE (using @SQLDelete annotation)
            categoryRepository.deleteById(categoryId);
        } else {
            // Permanent delete: No relationships exist
            categoryRepository.delete(category);
        }
    }

    @Override
    @Transactional
    public void updateCategoryImage(Long categoryId, String imageUrl) {
        Category category = getCategoryById(categoryId);
        category.setImageUrl(imageUrl);
        categoryRepository.save(category);
    }

    @Override
    public boolean categoryHasProducts(Long categoryId) {
        Category category = getCategoryById(categoryId);
        return productRepository.existsByCategory(category);
    }
}