package com.ashanhimantha.product_service.service;

import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ImageUploadService {
    String uploadCategoryImage(MultipartFile file, Long categoryId);
    void deleteCategoryImage(String imageUrl);

    // Generic methods for centralized image upload
    String uploadImage(MultipartFile file, String folder, String identifier);
    List<String> uploadImages(List<MultipartFile> files, String folder, String identifier);
    void deleteImage(String imageUrl);
    void deleteImages(List<String> imageUrls);
}
