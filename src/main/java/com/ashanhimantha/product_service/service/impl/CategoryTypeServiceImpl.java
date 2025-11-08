package com.ashanhimantha.product_service.service.impl;

import com.ashanhimantha.product_service.dto.request.CategoryTypeRequest;
import com.ashanhimantha.product_service.entity.CategoryType;
import com.ashanhimantha.product_service.entity.enums.Status;
import com.ashanhimantha.product_service.exception.DuplicateResourceException;
import com.ashanhimantha.product_service.exception.ResourceNotFoundException;
import com.ashanhimantha.product_service.repository.CategoryRepository;
import com.ashanhimantha.product_service.repository.CategoryTypeRepository;
import com.ashanhimantha.product_service.repository.ProductVariantRepository;
import com.ashanhimantha.product_service.service.CategoryTypeService;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryTypeServiceImpl implements CategoryTypeService {

    private final CategoryTypeRepository categoryTypeRepository;
    private final CategoryRepository categoryRepository;
    private final ProductVariantRepository productVariantRepository;

    @Override
    public CategoryType createCategoryType(CategoryTypeRequest request) {
        // Check if a category type with the same name already exists
        if (categoryTypeRepository.existsByName(request.getName())) {
            throw new DuplicateResourceException("A category type with the name '" + request.getName() + "' already exists.");
        }

        CategoryType categoryType = new CategoryType();
        categoryType.setName(request.getName());
        categoryType.setSizeOptionsFromList(request.getSizeOptions());

        try {
            return categoryTypeRepository.save(categoryType);
        } catch (DataIntegrityViolationException e) {
            // This handles potential race conditions where a duplicate name is inserted
            // between the check and the save operation.
            throw new DuplicateResourceException("A category type with the name '" + request.getName() + "' already exists.");
        }
    }

    @Override
    public CategoryType getCategoryTypeById(Long id) {
        return categoryTypeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category type not found with id: " + id));
    }

    @Override
    public List<CategoryType> getAllCategoryTypes() {
        return categoryTypeRepository.findAll();
    }

    @Override
    public CategoryType updateCategoryType(Long id, CategoryTypeRequest request) {
        // First, retrieve the existing category type
        CategoryType existingCategoryType = getCategoryTypeById(id);

        // Check if the name is being changed and if the new name already exists
        if (!existingCategoryType.getName().equals(request.getName())
                && categoryTypeRepository.existsByName(request.getName())) {
            throw new DuplicateResourceException("A category type with the name '" + request.getName() + "' already exists.");
        }

        // Update the properties of the existing entity
        existingCategoryType.setName(request.getName());
        // Check if any sizes are being removed that are currently in use by product variants
        List<String> existingSizes = existingCategoryType.getSizeOptionsAsList();
        List<String> newSizes = request.getSizeOptions();

        // Find sizes that are being removed (exist in old but not in new)
        List<String> removedSizes = existingSizes.stream()
                .filter(size -> !newSizes.contains(size))
                .toList();

        // If any sizes are being removed, check if they're in use
        if (!removedSizes.isEmpty()) {
            if (productVariantRepository.existsByAnySize(removedSizes)) {
                throw new IllegalStateException("Cannot remove size(s): " + String.join(", ", removedSizes) +
                        ". One or more product variants are using these sizes.");
            }
        }

        existingCategoryType.setSizeOptionsFromList(request.getSizeOptions());
        existingCategoryType.setStatus(request.getStatus());

        try {
            return categoryTypeRepository.save(existingCategoryType);
        } catch (DataIntegrityViolationException e) {
            throw new DuplicateResourceException("A category type with the name '" + request.getName() + "' already exists.");
        }
    }

    @Override
    public CategoryType updateCategoryTypeStatus(Long id, String status) {
        // Retrieve existing category type
        CategoryType existing = getCategoryTypeById(id);

        if (status == null || status.isBlank()) {
            throw new IllegalArgumentException("Status must be provided");
        }

        try {
            Status s = Status.valueOf(status.trim().toUpperCase());
            existing.setStatus(s);
            return categoryTypeRepository.save(existing);
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Invalid status value: " + status + ". Allowed values: ACTIVE, INACTIVE");
        }
    }

    @Override
    @Transactional
    public void deleteCategoryType(Long id) {
        // Check if the category type exists before attempting to delete
        CategoryType categoryType = categoryTypeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category type not found with id: " + id));

        // Check if any categories are using this category type
        if (categoryRepository.existsByCategoryType(categoryType)) {
            throw new IllegalStateException("Cannot delete category type. It is being used by one or more categories.");
        }

        categoryTypeRepository.deleteById(id);
    }
}