package com.ashanhimantha.product_service.service.impl;

import com.ashanhimantha.product_service.service.ImageUploadService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ImageUploadServiceImpl implements ImageUploadService {

    private final S3Client s3Client;

    @Value("${aws.s3.bucket-name}")
    private String bucketName;

    @Value("${aws.s3.base-url:}")
    private String s3BaseUrl;

    private static final String CATEGORY_FOLDER = "categories/";
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB

    @Override
    public String uploadCategoryImage(MultipartFile file, Long categoryId) {
        System.out.println("=== IMAGE UPLOAD SERVICE DEBUG ===");
        System.out.println("File: " + (file != null ? file.getOriginalFilename() : "null"));
        System.out.println("Category ID: " + categoryId);
        System.out.println("Bucket name: " + bucketName);
        System.out.println("S3 base URL: " + s3BaseUrl);

        validateFile(file);
        System.out.println("File validation passed");

        String originalFilename = file.getOriginalFilename();
        String fileExtension = getFileExtension(originalFilename);
        String fileName = CATEGORY_FOLDER + "category_" + categoryId + "_" + UUID.randomUUID() + fileExtension;
        System.out.println("Generated S3 key: " + fileName);

        try {
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(fileName)
                    .contentType(file.getContentType())
                    .contentLength(file.getSize())
                    .build();

            System.out.println("Uploading to S3...");
            s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));
            System.out.println("S3 upload completed successfully");

            String imageUrl = buildImageUrl(fileName);
            log.info("Successfully uploaded category image: {}", imageUrl);
            System.out.println("Final image URL: " + imageUrl);
            System.out.println("=== END IMAGE UPLOAD SERVICE DEBUG ===");
            return imageUrl;

        } catch (IOException e) {
            log.error("Failed to upload category image: {}", e.getMessage());
            System.err.println("ERROR in uploadCategoryImage: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to upload image to S3", e);
        } catch (Exception e) {
            System.err.println("UNEXPECTED ERROR in uploadCategoryImage: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to upload image to S3", e);
        }
    }

    @Override
    public void deleteCategoryImage(String imageUrl) {
        if (imageUrl == null || imageUrl.isEmpty()) {
            return;
        }

        try {
            String fileName = extractFileNameFromUrl(imageUrl);

            DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(fileName)
                    .build();

            s3Client.deleteObject(deleteObjectRequest);
            log.info("Successfully deleted category image: {}", fileName);

        } catch (Exception e) {
            log.error("Failed to delete category image: {}", e.getMessage());
            // Don't throw exception, just log it - deletion failure shouldn't block operations
        }
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File cannot be empty");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("File size exceeds maximum limit of 5MB");
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("File must be an image");
        }

        // Validate image format
        String[] allowedFormats = {"image/jpeg", "image/jpg", "image/png", "image/webp"};
        boolean isValidFormat = false;
        for (String format : allowedFormats) {
            if (format.equals(contentType)) {
                isValidFormat = true;
                break;
            }
        }

        if (!isValidFormat) {
            throw new IllegalArgumentException("Only JPEG, PNG, and WebP images are allowed");
        }
    }

    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return ".jpg";
        }
        return filename.substring(filename.lastIndexOf("."));
    }

    private String buildImageUrl(String fileName) {
        if (s3BaseUrl != null && !s3BaseUrl.isEmpty()) {
            return s3BaseUrl + "/" + fileName;
        }
        // Fallback to standard S3 URL format
        return "https://" + bucketName + ".s3.amazonaws.com/" + fileName;
    }

    private String extractFileNameFromUrl(String imageUrl) {
        // Extract filename from full URL
        // Example: https://bucket.s3.amazonaws.com/categories/image.jpg -> categories/image.jpg
        if (imageUrl.contains(bucketName)) {
            int index = imageUrl.indexOf(bucketName) + bucketName.length() + 1;
            return imageUrl.substring(index);
        }
        // If it's already just the filename
        return imageUrl;
    }
}
