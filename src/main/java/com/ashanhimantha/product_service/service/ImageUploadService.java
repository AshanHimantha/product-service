package com.ashanhimantha.product_service.service;

import org.springframework.web.multipart.MultipartFile;

public interface ImageUploadService {
    String uploadCategoryImage(MultipartFile file, Long categoryId);
    void deleteCategoryImage(String imageUrl);
}

