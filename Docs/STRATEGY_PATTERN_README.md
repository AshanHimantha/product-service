# Product Strategy Pattern Implementation

## Overview

This implementation uses the **Strategy Pattern** to handle different product types (STOCK and NON_STOCK) with distinct pricing and inventory management logic.

## Design Pattern: Strategy Pattern

### Why Strategy Pattern?

The Strategy Pattern allows us to:
- Define a family of algorithms (pricing/stock logic)
- Encapsulate each algorithm in separate classes
- Make them interchangeable at runtime
- Add new product types without modifying existing code (Open/Closed Principle)

## Architecture

### 1. Product Types (Enum)

```
ProductType.java
├── STOCK       - Products with inventory tracking
└── NON_STOCK   - Products without inventory (services, digital goods)
```

### 2. Strategy Interface

**ProductPricingStrategy.java** - Defines the contract for all product type strategies:
- `validateProductRequest()` - Validates product creation/update requests
- `applyPricing()` - Applies pricing logic based on product type
- `canPurchase()` - Checks if product can be purchased
- `calculatePrice()` - Calculates final price for quantity
- `processPurchase()` - Handles purchase processing (inventory updates)

### 3. Concrete Strategy Implementations

#### StockProductStrategy (@Component("STOCK"))
**For physical products with inventory:**
- Requires both unitCost and sellingPrice
- Validates that sellingPrice >= unitCost
- Creates and maintains Stock entity
- Tracks inventory quantity
- Prevents purchases when stock is insufficient
- Deducts inventory on purchase

#### NonStockProductStrategy (@Component("NON_STOCK"))
**For services, digital goods, etc:**
- Requires only sellingPrice (unitCost is optional)
- No inventory tracking (Stock entity is null)
- Always available for purchase
- No inventory deduction needed
- Suitable for unlimited resources

### 4. Strategy Factory

**ProductPricingStrategyFactory.java** - Selects the appropriate strategy based on ProductType using Spring's dependency injection.

## Data Model

### Product Entity
```java
@Entity
class Product {
    Long id;
    String name;
    String description;
    Double unitCost;        // Cost to business
    Double sellingPrice;    // Price to customer
    ProductType productType; // STOCK or NON_STOCK
    ProductStatus status;   // ACTIVE or INACTIVE
    Category category;
    Stock stock;            // Only for STOCK products
    List<String> imageUrls;
}
```

### Stock Entity
```java
@Entity
class Stock {
    Long id;
    Product product;
    Integer quantity;       // Current stock level
    Integer reorderLevel;   // Minimum before reordering
    Integer reorderQuantity; // Amount to reorder
}
```

## Usage Examples

### 1. Creating a STOCK Product

```java
POST /api/v1/products
Content-Type: multipart/form-data

{
  "name": "iPhone 15",
  "description": "Latest iPhone",
  "unitCost": 800.00,
  "sellingPrice": 1200.00,
  "productType": "STOCK",
  "categoryId": 1
}
+ files: [image1.jpg, image2.jpg]
```

**Behavior:**
- Validates unitCost and sellingPrice are present
- Creates Stock entity with quantity=0
- Inventory must be managed

### 2. Creating a NON_STOCK Product

```java
POST /api/v1/products
Content-Type: multipart/form-data

{
  "name": "Web Development Service",
  "description": "Custom website development",
  "unitCost": 0.00,        // Optional for services
  "sellingPrice": 5000.00,
  "productType": "NON_STOCK",
  "categoryId": 2
}
+ files: [service-image.jpg]
```

**Behavior:**
- Only validates sellingPrice
- No Stock entity created
- Always available for purchase

### 3. Processing a Purchase

```java
// For STOCK product
purchaseService.processPurchase(productId: 1, quantity: 5);
// → Checks stock availability
// → Deducts 5 units from inventory
// → Throws exception if insufficient stock

// For NON_STOCK product
purchaseService.processPurchase(productId: 2, quantity: 1);
// → Always succeeds (no inventory check)
// → No inventory deduction
```

## Key Benefits

### 1. Extensibility
Add new product types easily:
```java
@Component("SUBSCRIPTION")
public class SubscriptionProductStrategy implements ProductPricingStrategy {
    // Implement recurring billing logic
}
```

### 2. Maintainability
Each product type's logic is isolated in its own class, making it easy to:
- Understand business rules
- Modify specific behavior
- Test independently

### 3. Flexibility
Different products can have completely different:
- Pricing models
- Availability rules
- Purchase processing logic

### 4. SOLID Principles
- **Single Responsibility**: Each strategy handles one product type
- **Open/Closed**: Add new types without modifying existing code
- **Liskov Substitution**: All strategies are interchangeable
- **Dependency Inversion**: Depends on abstraction (interface), not concrete classes

## API Endpoints

### Public Endpoints
- `GET /api/v1/products` - Get all active products (paginated)
- `GET /api/v1/products/{id}` - Get specific active product

### Admin Endpoints (requires SuperAdmin role)
- `POST /api/v1/products` - Create product (with productType)
- `PUT /api/v1/products/{id}` - Update product
- `DELETE /api/v1/products/{id}` - Delete product
- `GET /api/v1/products/admin` - Get all products (all statuses)
- `POST /api/v1/products/{id}/images` - Upload additional images

## Response DTOs

### ProductResponse (Public)
```json
{
  "id": 1,
  "name": "Product Name",
  "description": "...",
  "sellingPrice": 1200.00,
  "productType": "STOCK",
  "status": "ACTIVE",
  "category": {...},
  "imageUrls": [...]
}
```

### AdminProductResponse (Admin Only)
```json
{
  "id": 1,
  "name": "Product Name",
  "unitCost": 800.00,      // Sensitive data
  "sellingPrice": 1200.00,
  "productType": "STOCK",
  "stock": {               // Only for STOCK products
    "quantity": 50,
    "reorderLevel": 10,
    "reorderQuantity": 100
  },
  "status": "ACTIVE",
  "category": {...}
}
```

## Future Enhancements

1. **Additional Product Types:**
   - `SUBSCRIPTION` - Recurring billing
   - `RENTAL` - Time-based pricing
   - `BUNDLE` - Multiple products grouped together

2. **Advanced Pricing Strategies:**
   - Tiered pricing (volume discounts)
   - Dynamic pricing based on demand
   - Time-based pricing

3. **Inventory Management:**
   - Automatic reorder notifications
   - Stock alerts
   - Multiple warehouse support

## Database Migration

When deploying, ensure your database schema includes:

```sql
-- Add productType column to products table
ALTER TABLE products ADD COLUMN product_type VARCHAR(20) NOT NULL DEFAULT 'STOCK';

-- Create stocks table (if not exists)
CREATE TABLE stocks (
    id BIGSERIAL PRIMARY KEY,
    product_id BIGINT UNIQUE NOT NULL,
    quantity INTEGER NOT NULL DEFAULT 0,
    reorder_level INTEGER NOT NULL DEFAULT 0,
    reorder_quantity INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE
);
```

## Testing

Test both product types:

1. **STOCK Products:**
   - Test inventory deduction
   - Test insufficient stock scenarios
   - Test pricing validation

2. **NON_STOCK Products:**
   - Test unlimited availability
   - Test optional unit cost
   - Test no inventory creation

---

**Author:** Product Service Team  
**Last Updated:** November 2025

