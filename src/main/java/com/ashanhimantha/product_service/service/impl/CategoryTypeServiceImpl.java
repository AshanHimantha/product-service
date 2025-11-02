package com.ashanhimantha.product_service.service.impl;

import com.ashanhimantha.product_service.dto.request.CategoryTypeRequest;
import com.ashanhimantha.product_service.entity.CategoryType;
import com.ashanhimantha.product_service.entity.enums.SizingType;
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
        // Check if name already exists
        if (categoryTypeRepository.existsByName(request.getName())) {
            throw new DuplicateResourceException("A category type with the name '" + request.getName() + "' already exists.");
        }

        CategoryType categoryType = new CategoryType();
        categoryType.setName(request.getName());
        categoryType.setSizingType(request.getSizingType());
        categoryType.setSizeOptionsFromList(request.getSizeOptions());

        try {
            return categoryTypeRepository.save(categoryType);
        } catch (DataIntegrityViolationException e) {
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
    public List<CategoryType> getCategoryTypesBySizingType(SizingType sizingType) {
        return categoryTypeRepository.findBySizingType(sizingType);
    }

    @Override
    public CategoryType updateCategoryType(Long id, CategoryTypeRequest request) {
        CategoryType existingCategoryType = getCategoryTypeById(id);

        // Check if the new name conflicts with another category type
        if (!existingCategoryType.getName().equals(request.getName())
                && categoryTypeRepository.existsByName(request.getName())) {
            throw new DuplicateResourceException("A category type with the name '" + request.getName() + "' already exists.");
        }

        existingCategoryType.setName(request.getName());
        existingCategoryType.setSizingType(request.getSizingType());
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
        if (!categoryTypeRepository.existsById(id)) {
            throw new ResourceNotFoundException("Category type not found with id: " + id);
        }
        categoryTypeRepository.deleteById(id);
    }
}
