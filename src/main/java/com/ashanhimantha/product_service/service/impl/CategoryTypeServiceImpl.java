package com.ashanhimantha.product_service.service.impl;

import com.ashanhimantha.product_service.dto.request.CategoryTypeRequest;
import com.ashanhimantha.product_service.entity.CategoryType;
import com.ashanhimantha.product_service.exception.DuplicateResourceException;
import com.ashanhimantha.product_service.exception.ResourceNotFoundException;
import com.ashanhimantha.product_service.repository.CategoryTypeRepository;
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
        existingCategoryType.setSizeOptionsFromList(request.getSizeOptions());

        try {
            return categoryTypeRepository.save(existingCategoryType);
        } catch (DataIntegrityViolationException e) {
            throw new DuplicateResourceException("A category type with the name '" + request.getName() + "' already exists.");
        }
    }

    @Override
    @Transactional
    public void deleteCategoryType(Long id) {
        // Check if the category type exists before attempting to delete
        if (!categoryTypeRepository.existsById(id)) {
            throw new ResourceNotFoundException("Category type not found with id: " + id);
        }
        categoryTypeRepository.deleteById(id);
    }
}