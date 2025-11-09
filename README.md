# Product Service

[![Java](https://img.shields.io/badge/Java-17-orange.svg?logo=openjdk)](https://openjdk.java.net/) [![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.2.5-6DB33F.svg?logo=spring)](https://spring.io/projects/spring-boot) [![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-336791.svg?logo=postgresql)](https://www.postgresql.org/) [![Maven](https://img.shields.io/badge/Maven-3.8+-C71A36.svg?logo=apache-maven)](https://maven.apache.org/) [![Packaging](https://img.shields.io/badge/Packaging-JAR-blue.svg)](https://maven.apache.org/plugins/maven-jar-plugin/) [![Swagger](https://img.shields.io/badge/API_Docs-Swagger-85EA2D.svg?logo=swagger)](http://localhost:8082/swagger-ui.html)

This repository contains the source code for the Product Service, a core backend microservice for the eCommerce platform.

## 1. Overview

The Product Service is the central hub for managing the entire product catalog, including products, categories, category types, and product variants with image management. It is designed with a clear separation of concerns for different user roles with role-based access control.

## 2. Core Features

### Public (Customer-Facing)
- **Browse Products:** Get a paginated list of all **active** products available for purchase.
- **View Product Details:** View detailed information about a single active product including all variants.
- **Browse Categories:** Get a paginated or simple list of all **active** categories.
- **Browse Category Types:** Get a paginated or simple list of all **active** category types.
- **View Category Details:** View details of a single category including associated products.

### Admin-Facing (SuperAdmins)
- **Product Management:**
  - Create products with multiple variants (color, size, pricing, stock)
  - Update existing products and variants
  - Delete products (soft delete)
  - Upload/manage up to 6 images per product
  - View all products including inactive ones
  
- **Category Management:**
  - Full CRUD operations for product categories
  - Upload category images to AWS S3
  - Soft delete support for data integrity
  - View all categories including inactive ones
  
- **Category Type Management:**
  - Create, update, and manage category types (e.g., Vegetables, Fruits, Dairy)
  - Organize categories into logical groupings
  - Control visibility and ordering
  
- **Variant Management:**
  - Manage product variants with different colors, sizes, and pricing
  - Track inventory levels per variant
  - Update stock quantities
  - Support for SKU management

## 3. Technology Stack & Key Patterns

- **Framework:** [Spring Boot](https://spring.io/projects/spring-boot) (v3.2.5)
- **Language:** [Java 17](https://www.oracle.com/java/technologies/javase/jdk17-archive-downloads.html)
- **Build Tool:** [Apache Maven](https://maven.apache.org/)
- **Packaging:** JAR
- **Authentication:** [Spring Security](https://spring.io/projects/spring-security) with JWT validation via AWS Cognito
- **Database:** [PostgreSQL](https://www.postgresql.org/) (v16) with [Spring Data JPA / Hibernate](https://spring.io/projects/spring-data-jpa)
- **Cloud Storage:** [AWS S3](https://aws.amazon.com/s3/) for image storage
- **API Documentation:** [SpringDoc OpenAPI 3](https://springdoc.org/) (v2.6.0) / Swagger UI
- **Mapping:** [MapStruct](https://mapstruct.org/) (v1.5.5) for clean and efficient DTO-to-Entity mapping
- **Connection Pool:** [HikariCP](https://github.com/brettwooldridge/HikariCP) for optimal database performance
- **Validation:** Jakarta Bean Validation for request validation

### Key Patterns & Practices
- **Role-Based Access Control (RBAC):** Fine-grained permissions using `@PreAuthorize` with Cognito groups
- **Soft Deletes:** Implemented for Categories, Category Types, and Products to ensure data integrity
- **Global Exception Handling:** Centralized `@RestControllerAdvice` provides consistent error responses
- **DTO Segregation:** Separate Request, Public Response, and Admin Response DTOs for security
- **Strategy Pattern:** Clean architecture for handling different product types (Stock vs Non-Stock)
- **Multipart File Upload:** Support for image uploads with validation (max 6 images per product)
- **Pagination Support:** Consistent pagination across all list endpoints using Spring Data's `Pageable`

## 4. Setup and Configuration

### Prerequisites
- Java JDK 17+
- Apache Maven 3.8+
- PostgreSQL Server 16+
- AWS Account with:
  - Configured Cognito User Pool with required user groups (`SuperAdmins`)
  - S3 Bucket for image storage
  - IAM credentials with S3 access

### Configuration
1.  **Clone the repository:**
    ```bash
    git clone <your-repo-url>
    cd product-service
    ```
    
2.  **Create the Database:**
    - In PostgreSQL, create a new database named `product_db`:
    ```sql
    CREATE DATABASE product_db;
    ```
    
3.  **Update `application.properties`:**
    - Copy `application-example.properties` to `application.properties`
    - Configure the following properties:
    ```properties
    # Database Configuration
    spring.datasource.url=jdbc:postgresql://localhost:5432/product_db
    spring.datasource.username=your_username
    spring.datasource.password=your_password
    
    # AWS Cognito Configuration
    spring.security.oauth2.resourceserver.jwt.jwk-set-uri=https://cognito-idp.{region}.amazonaws.com/{userPoolId}/.well-known/jwks.json
    
    # AWS S3 Configuration
    aws.s3.bucket-name=your-bucket-name
    aws.region=your-region
    aws.access-key-id=your-access-key
    aws.secret-access-key=your-secret-key
    ```

## 5. Running the Application

### Build and Run
Build and run the application using Maven:

```bash
# Build the project
mvn clean install

# Run the application
mvn spring-boot:run

# Or run the JAR directly
java -jar target/product-service-0.0.1-SNAPSHOT.jar
```

The service will start on **http://localhost:8082**

### Health Check
Verify the service is running:
```bash
curl http://localhost:8082/actuator/health
```

## 6. API Documentation

### Swagger UI (Interactive Documentation)
Once the application is running, access the interactive Swagger UI at:

**[http://localhost:8082/swagger-ui.html](http://localhost:8082/swagger-ui.html)**

The Swagger UI provides:
- Complete API documentation for all endpoints
- Interactive testing interface
- Request/response schemas with examples
- Authentication support (Bearer token)
- Parameter descriptions and validation rules

### OpenAPI Specification
The raw OpenAPI 3.0 specification is available at:

**[http://localhost:8082/v3/api-docs](http://localhost:8082/v3/api-docs)**

### API Endpoints Overview

#### Products (`/api/v1/products`)
- `GET /api/v1/products` - Get all active products (Public)
- `GET /api/v1/products/{productId}` - Get product by ID (Public)
- `GET /api/v1/products/admin` - Get all products for admin (Admin)
- `GET /api/v1/products/admin/{productId}` - Get product details for admin (Admin)
- `POST /api/v1/products` - Create product with images (Admin)
- `PUT /api/v1/products/{productId}` - Update product (Admin)
- `DELETE /api/v1/products/{productId}` - Delete product (Admin)
- `POST /api/v1/products/{productId}/images` - Upload product images (Admin)

#### Categories (`/api/v1/categories`)
- `GET /api/v1/categories` - Get all active categories (Public)
- `GET /api/v1/categories/list` - Get simple category list (Public)
- `GET /api/v1/categories/{categoryId}` - Get category by ID (Public)
- `GET /api/v1/categories/admin` - Get all categories for admin (Admin)
- `POST /api/v1/categories` - Create category with image (Admin)
- `PUT /api/v1/categories/{categoryId}` - Update category (Admin)
- `DELETE /api/v1/categories/{categoryId}` - Delete category (Admin)

#### Category Types (`/api/v1/category-types`)
- `GET /api/v1/category-types` - Get all active category types (Public)
- `GET /api/v1/category-types/list` - Get simple category type list (Public)
- `POST /api/v1/category-types` - Create category type (Admin)
- `PUT /api/v1/category-types/{categoryTypeId}` - Update category type (Admin)
- `DELETE /api/v1/category-types/{categoryTypeId}` - Delete category type (Admin)

#### Product Variants (`/api/v1/variants`)
- `GET /api/v1/variants/{variantId}` - Get variant by ID (Admin)
- `PUT /api/v1/variants/{variantId}` - Update variant (Admin)
- `DELETE /api/v1/variants/{variantId}` - Delete variant (Admin)
- `PUT /api/v1/variants/{variantId}/stock` - Update variant stock (Admin)


## 7. Authentication & Authorization

### JWT Token
All protected endpoints require a valid JWT token from AWS Cognito. Include the token in the Authorization header:
```
Authorization: Bearer <your-jwt-token>
```

### User Roles
- **SuperAdmins** - Full access to all administrative endpoints
- **Public** - Access to public product and category browsing endpoints

### Testing with Swagger
1. Obtain a JWT token from AWS Cognito
2. In Swagger UI, click the **Authorize** button (🔒 icon)
3. Enter: `Bearer <your-jwt-token>`
4. Click **Authorize**
5. All subsequent requests will include the token

## 8. Database Schema

The service uses PostgreSQL with the following main entities:
- **Product** - Core product information with soft delete support
- **ProductVariant** - Variants with color, size, pricing, and stock levels
- **Category** - Product categorization with image support
- **CategoryType** - Higher-level category groupings

All entities include audit fields (`createdAt`, `updatedAt`) managed by JPA.

## 9. Additional Documentation

Detailed technical documentation is available in the `/Docs` folder:
- **PRODUCT_VARIANTS_GUIDE.md** - Comprehensive guide on product variants
- **CATEGORY_IMAGE_UPLOAD.md** - Category image upload implementation
- **CATEGORY_TYPE_FEATURE.md** - Category type feature documentation
- **SWAGGER_DOCUMENTATION.md** - Swagger configuration and usage
- **STRATEGY_PATTERN_README.md** - Product type strategy pattern
- **MULTIPART_UPLOAD_FIX.md** - Multipart file upload troubleshooting

## 10. Contributing

When contributing to this project:
1. Follow Java coding conventions
2. Write comprehensive unit tests
3. Update Swagger documentation for new endpoints
4. Update this README for significant changes
5. Test with Postman collection before submitting PR

## 11. License

This project is licensed under the Apache License 2.0 - see the LICENSE file for details.

## 12. Contact & Support

- **Developer:** Ashan Himantha
- **Email:** ashanhimantha321@gmail.com
- **GitHub:** [@ashanhimantha](https://github.com/ashanhimantha)

---

**Note:** This service is part of the ecommerce microservices ecosystem. Ensure all dependent services (Authentication, Order Service, etc.) are properly configured for full functionality.
