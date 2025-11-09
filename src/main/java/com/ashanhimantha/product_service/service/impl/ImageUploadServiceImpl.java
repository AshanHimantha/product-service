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
import java.util.ArrayList;
import java.util.List;
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

    @Value("${aws.region:ap-southeast-2}")
    private String awsRegion;

    private static final String CATEGORY_FOLDER = "categories/";
    private static final String PRODUCT_FOLDER = "products/";
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB

    @Override
    public String uploadCategoryImage(MultipartFile file, Long categoryId) {
        log.debug("Uploading category image for category ID: {}", categoryId);
        return uploadImage(file, CATEGORY_FOLDER, "category_" + categoryId);
    }

    @Override
    public void deleteCategoryImage(String imageUrl) {
        deleteImage(imageUrl);
    }

    @Override
    public String uploadImage(MultipartFile file, String folder, String identifier) {
        log.debug("Uploading image to folder: {} with identifier: {}", folder, identifier);

        validateFile(file);

        String originalFilename = file.getOriginalFilename();
        String fileExtension = getFileExtension(originalFilename);
        String fileName = folder + identifier + "_" + UUID.randomUUID() + fileExtension;

        try {
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(fileName)
                    .contentType(file.getContentType())
                    .contentLength(file.getSize())
                    .build();

            s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));

            String imageUrl = buildImageUrl(fileName);
            log.info("Successfully uploaded image: {}", imageUrl);
            return imageUrl;

        } catch (IOException e) {
            log.error("Failed to upload image: {}", e.getMessage());
            throw new RuntimeException("Failed to upload image to S3", e);
        }
    }

    @Override
    public List<String> uploadImages(List<MultipartFile> files, String folder, String identifier) {
        log.debug("Uploading {} images to folder: {} with identifier: {}", files.size(), folder, identifier);

        List<String> imageUrls = new ArrayList<>();

        for (MultipartFile file : files) {
            if (file == null || file.isEmpty()) {
                continue;
            }

            try {
                String imageUrl = uploadImage(file, folder, identifier);
                imageUrls.add(imageUrl);
            } catch (Exception e) {
                log.error("Failed to upload one image in batch: {}", e.getMessage());
                // Clean up previously uploaded images on failure
                deleteImages(imageUrls);
                throw new RuntimeException("Failed to upload images to S3. All uploads rolled back.", e);
            }
        }

        log.info("Successfully uploaded {} images", imageUrls.size());
        return imageUrls;
    }

    @Override
    public void deleteImage(String imageUrl) {
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
            log.info("Successfully deleted image: {}", fileName);

        } catch (Exception e) {
            log.error("Failed to delete image: {}", e.getMessage());
            // Don't throw exception, just log it - deletion failure shouldn't block operations
        }
    }

    @Override
    public void deleteImages(List<String> imageUrls) {
        if (imageUrls == null || imageUrls.isEmpty()) {
            return;
        }

        log.debug("Deleting {} images", imageUrls.size());

        for (String imageUrl : imageUrls) {
            deleteImage(imageUrl);
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
        return String.format("https://%s.s3.%s.amazonaws.com/%s", bucketName, awsRegion, fileName);
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
