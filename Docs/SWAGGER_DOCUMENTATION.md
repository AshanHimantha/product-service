# Swagger/OpenAPI Documentation Implementation

## Overview

This document provides information about the Swagger/OpenAPI documentation implementation for the Product Service API.

## What Was Implemented

### 1. Dependencies Added
- **SpringDoc OpenAPI**: Version 2.3.0 (compatible with Spring Boot 3.x)
  - Automatically generates OpenAPI 3.0 documentation
  - Provides interactive Swagger UI

### 2. Configuration Files

#### OpenApiConfig.java
Located at: `src/main/java/com/ashanhimantha/product_service/config/OpenApiConfig.java`

This configuration file defines:
- **API Information**: Title, description, version, contact details, license
- **Servers**: Local development and production server URLs
- **Security Schemes**: JWT Bearer token authentication (AWS Cognito)
- Global security requirement for all endpoints

#### application.properties
Added the following Swagger configuration:
```properties
# SWAGGER/OPENAPI CONFIGURATION
springdoc.api-docs.path=/api-docs
springdoc.swagger-ui.path=/swagger-ui.html
springdoc.swagger-ui.enabled=true
springdoc.swagger-ui.operationsSorter=method
springdoc.swagger-ui.tagsSorter=alpha
springdoc.swagger-ui.tryItOutEnabled=true
springdoc.show-actuator=false
```

### 3. Controller Annotations

All controllers have been enhanced with comprehensive Swagger annotations:

#### AbstractController
- Added `@ApiResponses` with common HTTP response codes (200, 400, 401, 403, 404, 500)
- These are inherited by all controllers

#### ProductController
- `@Tag`: Groups all product endpoints together
- `@Operation`: Detailed description for each endpoint
- `@Parameter`: Documents path variables, query parameters, and request parameters
- `@SecurityRequirement`: Specifies which endpoints require authentication

#### CategoryController
- Similar annotations for category management
- Documents multipart file upload endpoints

#### CategoryTypeController
- Documents category type classification endpoints

#### ProductVariantController
- Documents product variant/SKU management endpoints
- Includes stock and pricing update operations

## Accessing Swagger Documentation

### 1. Start the Application
```bash
mvn spring-boot:run
```
Or run the application from your IDE.

### 2. Access Swagger UI
Open your browser and navigate to:
```
http://localhost:8080/swagger-ui.html
```

### 3. Access OpenAPI JSON Specification
The raw OpenAPI specification is available at:
```
http://localhost:8080/api-docs
```

## Using Swagger UI

### Public Endpoints
These endpoints don't require authentication:
- `GET /api/v1/products` - Get all active products
- `GET /api/v1/products/{productId}` - Get product by ID
- `GET /api/v1/categories` - Get all categories
- `GET /api/v1/category-types` - Get all category types

You can test these directly by clicking "Try it out" → "Execute"

### Protected Endpoints (Admin)
Endpoints marked with 🔒 require JWT authentication:

#### To test protected endpoints:
1. **Obtain JWT Token**: Get a valid JWT token from AWS Cognito (e.g., through your login flow)

2. **Authorize in Swagger**:
   - Click the **"Authorize"** button at the top right
   - Enter your token in the format: `Bearer <your-jwt-token>`
   - Click "Authorize"
   - Click "Close"

3. **Test Endpoints**: Now you can test protected endpoints with your authenticated session

### Testing Multipart File Uploads
For endpoints that accept file uploads (products, categories):
1. Click "Try it out"
2. Fill in the JSON fields
3. Click "Choose File" to select images
4. Click "Execute"

## API Structure

### Products (`/api/v1/products`)
- **Public**: Get active products, get product by ID
- **Admin**: Full CRUD operations, image management, product variants

### Categories (`/api/v1/categories`)
- **Public**: Get all categories, get category by ID
- **Admin**: Create, update, delete categories, image management, status updates

### Category Types (`/api/v1/category-types`)
- **Public**: Get all types, get type by ID
- **Admin**: Full CRUD operations, status updates

### Product Variants (`/api/v1/product-variants`)
- **Admin Only**: Create variants, manage stock, update pricing, status management

## Features

### 1. Interactive API Testing
- Test all endpoints directly from the browser
- See request/response examples
- Validate your API without external tools like Postman

### 2. Automatic Documentation
- Documentation stays in sync with code
- Changes to controllers automatically update Swagger docs
- No need to maintain separate API documentation

### 3. Schema Definitions
- All request/response DTOs are automatically documented
- See required fields, data types, and validation rules
- Example values for better understanding

### 4. Authentication
- JWT Bearer token authentication configured
- Test secured endpoints with your Cognito tokens
- Security clearly marked on each endpoint

## Customization

### Changing API Information
Edit `OpenApiConfig.java` to update:
- API title and description
- Version number
- Contact information
- Server URLs
- License information

### Adding More Documentation
To enhance documentation on specific endpoints:

```java
@Operation(
    summary = "Short description",
    description = "Detailed description with examples",
    parameters = {
        @Parameter(name = "paramName", description = "What this param does", example = "exampleValue")
    }
)
```

### Hiding Endpoints
To exclude specific endpoints from Swagger:
```java
@Hidden
@GetMapping("/internal-endpoint")
public ResponseEntity<?> internalMethod() {
    // This won't appear in Swagger
}
```

## Troubleshooting

### Swagger UI Not Loading
1. Verify the application is running on port 8080
2. Check `application.properties` for correct Swagger configuration
3. Clear browser cache and try again

### Authentication Not Working
1. Ensure your JWT token is valid and not expired
2. Verify the token includes the required roles (e.g., `SuperAdmins`)
3. Check that `spring.security.oauth2.resourceserver.jwt.jwk-set-uri` is correctly configured

### Endpoints Not Showing
1. Verify controllers are properly annotated with `@RestController`
2. Check that the controller package is being scanned by Spring Boot
3. Restart the application

## Production Considerations

### Disable Swagger in Production (Optional)
Add to `application.properties`:
```properties
springdoc.swagger-ui.enabled=false
springdoc.api-docs.enabled=false
```

Or use Spring profiles:
```properties
# application-prod.properties
springdoc.swagger-ui.enabled=false
```

### Secure Swagger UI
Consider adding additional security:
```java
@Configuration
public class SecurityConfig {
    // Add authentication for /swagger-ui.html
    // Add IP whitelist for documentation access
}
```

## Best Practices

1. **Keep Descriptions Updated**: When changing endpoints, update the `@Operation` description
2. **Document Parameters**: Always add `@Parameter` annotations with clear descriptions
3. **Use Examples**: Provide example values to help API consumers
4. **Test Regularly**: Use Swagger UI to test endpoints during development
5. **Version Your API**: Update version in `OpenApiConfig.java` when making breaking changes

## Resources

- [SpringDoc Documentation](https://springdoc.org/)
- [OpenAPI Specification](https://swagger.io/specification/)
- [Swagger UI](https://swagger.io/tools/swagger-ui/)

## Summary

Swagger documentation is now fully implemented for the Product Service. Access it at `http://localhost:8080/swagger-ui.html` to explore and test all available endpoints interactively.

