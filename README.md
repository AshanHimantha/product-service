# Harvest Hub - Product Service

[![Java](https://img.shields.io/badge/Java-17-orange.svg?logo=openjdk)](https://openjdk.java.net/) [![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.2.5-6DB33F.svg?logo=spring)](https://spring.io/projects/spring-boot) [![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-336791.svg?logo=postgresql)](https://www.postgresql.org/) [![Maven](https://img.shields.io/badge/Maven-3.8+-C71A36.svg?logo=apache-maven)](https://maven.apache.org/) [![Packaging](https://img.shields.io/badge/Packaging-JAR-blue.svg)](https://maven.apache.org/plugins/maven-jar-plugin/)

This repository contains the source code for the Product Service, a core backend microservice for the **Harvest Hub** eCommerce platform.

## 1. Overview

The Product Service is the central hub for managing the entire product catalog. It handles the creation, approval, and lifecycle of products, as well as the management of product categories. It is designed with a clear separation of concerns for different user roles: Customers, Suppliers, Data Stewards, and SuperAdmins.

## 2. Core Features

### Public (Customer-Facing)
- **Browse Products:** Get a paginated list of all **approved** products.
- **Browse Categories:** Get a paginated or simple list of all **active** categories.
- **View Product Details:** View the details of a single **approved** product. The response DTO for public users (`ProductResponse`) safely excludes sensitive business data like `unitCost`.

### Supplier-Facing
- **Create Product:** Submit a new product for review. The product is created with a `PENDING_APPROVAL` status.
- **Update Product:** Update the details of a product **owned by the supplier**. The product's status is reset to `PENDING_APPROVAL` for re-review.
- **Delete Product:** "Soft delete" a product **owned by the supplier**, marking it as inactive.

### Data Steward & Admin-Facing
- **Product Approval Workflow:** Update a product's status to `APPROVED` or `REJECTED`.
- **View Pending Products:** Get a list of all products currently in `PENDING_APPROVAL` status.
- **View Any Product:** Admins can view the full details of any product, regardless of its approval status.
- **Full Category Management (CRUD):** `SuperAdmins` can create, view, update, and "soft delete" product categories.

## 3. Technology Stack & Key Patterns

- **Framework:** [Spring Boot](https://spring.io/projects/spring-boot) (v3.5.5)
- **Language:** [Java 17](https://www.oracle.com/java/technologies/javase/jdk17-archive-downloads.html)
- **Build Tool:** [Apache Maven](https://maven.apache.org/)
- **Packaging:** JAR
- **Authentication:** [Spring Security](https://spring.io/projects/spring-security) with JWT validation via AWS Cognito.
- **Database:** [PostgreSQL](https://www.postgresql.org/) with [Spring Data JPA / Hibernate](https://spring.io/projects/spring-data-jpa).
- **Mapping:** [MapStruct](https://mapstruct.org/) for clean and efficient DTO-to-Entity mapping.
- **Key Patterns:**
  - **Role-Based Access Control (RBAC):** Fine-grained permissions using `@PreAuthorize`.
  - **Soft Deletes:** Implemented for Categories and Products to ensure data integrity.
  - **Global Exception Handling:** A `@RestControllerAdvice` provides consistent error responses.
  - **DTO Segregation:** Separate `Request`, public `Response`, and `AdminResponse` DTOs.

## 4. Setup and Configuration

### Prerequisites
- Java JDK 17+
- Apache Maven 3.8+
- PostgreSQL Server
- A configured AWS Cognito User Pool with the required user groups.

### Configuration
1.  **Clone the repository:**
    ```bash
    git clone <your-repo-url>
    cd product-service
    ```
2.  **Create the Database:**
    - In PostgreSQL, create a new database named `product_db`.
    ```sql
    CREATE DATABASE product_db;
    ```
3.  **Update `application.properties`:**
    - Open `src/main/resources/application.properties`.
    - Verify the PostgreSQL `datasource` credentials.
    - Update the `spring.security.oauth2.resourceserver.jwt.jwk-set-uri` with your Cognito User Pool's JWKS URI.

## 5. Running the Application

Build and run the application using Maven:

```bash
# Build the project
mvn clean install

# Run the application
java -jar target/product-service-0.0.1-SNAPSHOT.jar
```

The service will start on http://localhost:8082.

## 6. API Collection (Postman)

A complete Postman collection for testing all API endpoints is available publicly. It includes example requests for all user roles and functionalities.

[![Run in Postman](https://run.pstmn.io/button.svg)](https://www.postman.com/xd5555-3122/workspace/harvest-hub-apis/collection/36340838-1e0606fd-176f-42cd-93b9-8f87f388f089?action=share&source=copy-link&creator=36340838)

**To use the collection:**

1.  Click the "Run in Postman" button above to import it into your workspace.
2.  In the collection's **Variables** tab, set the `baseUrl` to `http://localhost:8082`.
3.  To test secure endpoints, paste a valid JWT `AccessToken` into the **CURRENT VALUE** of the `accessToken` variable in the collection's **Variables** tab. All requests are pre-configured to use this token for authorization.
