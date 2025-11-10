# Category Image Upload to S3

## Overview
This feature allows SuperAdmins to upload category images directly to AWS S3 with automatic URL management.

## Features
- ✅ Upload images directly to S3
- ✅ Automatic image replacement (old image deleted when uploading new)
- ✅ Image validation (format, size)
- ✅ Delete images from S3
- ✅ Organized S3 folder structure (`categories/`)

## Configuration

### Required Properties (application.properties)
```properties
# AWS S3 Configuration
aws.region=ap-southeast-2
aws.s3.bucket-name=ecom-product-images-999
aws.s3.base-url=https://ecom-product-images-999.s3.ap-southeast-2.amazonaws.com

# File Upload Limits
spring.servlet.multipart.max-file-size=10MB
spring.servlet.multipart.max-request-size=60MB
spring.servlet.multipart.enabled=true
```

### AWS Credentials
The service uses `DefaultCredentialsProvider` which checks for credentials in this order:
1. Environment variables (`AWS_ACCESS_KEY_ID`, `AWS_SECRET_ACCESS_KEY`)
2. Java system properties
3. AWS credentials file (`~/.aws/credentials`)
4. IAM role (if running on EC2)

## API Endpoints

### 1. Upload Category Image
**POST** `/api/v1/categories/{categoryId}/upload-image`

**Authorization:** SuperAdmins only

**Request:**
- Content-Type: `multipart/form-data`
- Body: Form data with key `image` containing the image file

**Example using Postman:**
1. Select `POST` method
2. URL: `http://localhost:8080/api/v1/categories/1/upload-image`
3. Headers: `Authorization: Bearer {your_token}`
4. Body → form-data → key: `image`, type: `File`, value: select your image
5. Send

**Response:**
```json
{
    "status": "SUCCESS",
    "message": "Category image uploaded successfully",
    "data": {
        "imageUrl": "https://ecom-product-images-999.s3.ap-southeast-2.amazonaws.com/categories/category_1_abc123.jpg"
    }
}
```

**Validations:**
- Maximum file size: **5MB**
- Allowed formats: **JPEG, JPG, PNG, WebP**
- File must be a valid image

**Behavior:**
- If category already has an image, the old image is **automatically deleted** from S3
- Image is stored with pattern: `categories/category_{id}_{uuid}.{extension}`
- Category's `imageUrl` field is automatically updated

### 2. Delete Category Image
**DELETE** `/api/v1/categories/{categoryId}/delete-image`

**Authorization:** SuperAdmins only

**Example:**
```
DELETE http://localhost:8080/api/v1/categories/1/delete-image
```

**Response:**
```json
{
    "status": "SUCCESS",
    "message": "Category image deleted successfully",
    "data": null
}
```

**Behavior:**
- Deletes image from S3
- Sets category's `imageUrl` to `null`

## S3 Folder Structure
```
ecom-product-images-999/
└── categories/
    ├── category_1_abc123-456def.jpg
    ├── category_2_xyz789-012ghi.png
    └── category_3_qwe456-789rty.webp
```

## Implementation Details

### ImageUploadService
Handles all S3 operations:
- **uploadCategoryImage()** - Uploads image to S3
- **deleteCategoryImage()** - Removes image from S3

### File Naming Convention
```
categories/category_{categoryId}_{randomUUID}.{extension}
```
Example: `categories/category_1_550e8400-e29b-41d4-a716-446655440000.jpg`

### Image URL Format
```
https://{bucket-name}.s3.{region}.amazonaws.com/categories/category_{id}_{uuid}.{ext}
```

## Error Handling

### Common Errors

**1. File Too Large**
```json
{
    "status": "ERROR",
    "message": "File size exceeds maximum limit of 5MB"
}
```

**2. Invalid Format**
```json
{
    "status": "ERROR",
    "message": "Only JPEG, PNG, and WebP images are allowed"
}
```

**3. Empty File**
```json
{
    "status": "ERROR",
    "message": "File cannot be empty"
}
```

**4. Category Not Found**
```json
{
    "status": "ERROR",
    "message": "Category not found with id: 999"
}
```

**5. S3 Upload Failed**
```json
{
    "status": "ERROR",
    "message": "Failed to upload image to S3"
}
```

## Usage Examples

### Example 1: Create Category then Upload Image
```bash
# Step 1: Create category
POST /api/v1/categories
{
  "name": "T-Shirts",
  "description": "Casual t-shirts",
  "categoryTypeId": 1
}

# Response: Category ID = 1

# Step 2: Upload image
POST /api/v1/categories/1/upload-image
Form-data: image = [select file]

# Category now has imageUrl in database
```

### Example 2: Replace Existing Image
```bash
# Upload new image (old one automatically deleted)
POST /api/v1/categories/1/upload-image
Form-data: image = [new image file]
```

### Example 3: Remove Image
```bash
# Delete image from S3 and clear URL
DELETE /api/v1/categories/1/delete-image
```

## Security

### IAM Permissions Required
Your AWS credentials need these S3 permissions:
```json
{
    "Version": "2012-10-17",
    "Statement": [
        {
            "Effect": "Allow",
            "Action": [
                "s3:PutObject",
                "s3:GetObject",
                "s3:DeleteObject"
            ],
            "Resource": "arn:aws:s3:::ecom-product-images-999/categories/*"
        }
    ]
}
```

### Access Control
- Only **SuperAdmins** can upload/delete images
- Image URLs are public (anyone can view with the URL)
- No authentication required to view images once uploaded

## S3 Bucket Configuration

### CORS Configuration (if accessing from browser)
```json
[
    {
        "AllowedHeaders": ["*"],
        "AllowedMethods": ["GET", "PUT", "POST", "DELETE"],
        "AllowedOrigins": ["*"],
        "ExposeHeaders": []
    }
]
```

### Bucket Policy (Public Read Access)
```json
{
    "Version": "2012-10-17",
    "Statement": [
        {
            "Sid": "PublicReadGetObject",
            "Effect": "Allow",
            "Principal": "*",
            "Action": "s3:GetObject",
            "Resource": "arn:aws:s3:::ecom-product-images-999/*"
        }
    ]
}
```

## Integration with Category Response

When you fetch categories, the image URL is included:
```json
{
    "status": "SUCCESS",
    "message": "Categories retrieved successfully",
    "data": [
        {
            "id": 1,
            "name": "T-Shirts",
            "description": "Casual t-shirts",
            "imageUrl": "https://ecom-product-images-999.s3.ap-southeast-2.amazonaws.com/categories/category_1_abc123.jpg",
            "categoryType": {
                "id": 1,
                "name": "Clothing Sizes - Letter",
                "sizingType": "CLOTHING_LETTER",
                "sizeOptions": ["S", "M", "L", "XL"]
            }
        }
    ]
}
```

## Testing Checklist

- [ ] Upload valid image (JPEG, PNG, WebP)
- [ ] Upload image exceeding 5MB (should fail)
- [ ] Upload non-image file (should fail)
- [ ] Replace existing image (old one deleted)
- [ ] Delete image
- [ ] Upload without authentication (should fail)
- [ ] Upload as non-SuperAdmin (should fail)
- [ ] Verify image accessible via URL
- [ ] Verify old images deleted from S3

## Troubleshooting

### Issue: "Access Denied" when uploading
**Solution:** Check AWS credentials and IAM permissions

### Issue: Image URL returns 404
**Solution:** 
- Verify S3 bucket name is correct
- Check bucket policy allows public read
- Ensure image actually uploaded to S3

### Issue: "Failed to upload image to S3"
**Solution:**
- Check AWS credentials are configured
- Verify S3 bucket exists
- Check network connectivity to AWS
- Review application logs for detailed error

### Issue: Old images not being deleted
**Solution:** Check IAM permissions include `s3:DeleteObject`

## Future Enhancements

1. **Image Optimization** - Compress/resize images before upload
2. **Multiple Images** - Support image galleries per category
3. **CDN Integration** - Use CloudFront for faster image delivery
4. **Image Variants** - Generate thumbnails automatically
5. **Async Upload** - Upload in background for large files

