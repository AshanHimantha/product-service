# Product Variants System - Complete Guide

## Overview

This implementation allows you to create products with **color and size variants**, where each variant automatically gets its own **stock tracking, unit cost, and selling price**.

Perfect for products like:
- **T-Shirts**: Multiple colors (Red, Blue, Black) × Multiple sizes (S, M, L, XL)
- **Shoes**: Different colors × Different sizes
- **Any physical product** with variations

---

## 🎯 How It Works

### Example: Creating a T-Shirt Product

When you create a T-Shirt product with:
- **Colors**: Red, Blue, Black
- **Sizes**: S, M, L, XL

The system **automatically generates 12 variants** (3 colors × 4 sizes):
1. Red-S, Red-M, Red-L, Red-XL
2. Blue-S, Blue-M, Blue-L, Blue-XL
3. Black-S, Black-M, Black-L, Black-XL

**Each variant has its own:**
- ✅ Unit Cost (what you paid for it)
- ✅ Selling Price (what customers pay)
- ✅ Stock Quantity (inventory tracking)
- ✅ Auto-generated SKU (e.g., TSHR-RED-M-1234)

---

## 📝 API Request Example

### Create a Product with Variants

```http
POST /api/v1/products
Content-Type: multipart/form-data
Authorization: Bearer {superadmin-token}

{
  "name": "Premium Cotton T-Shirt",
  "description": "High-quality cotton t-shirt available in multiple colors and sizes",
  "unitCost": 0,      // Optional for variant products (each variant has its own cost)
  "sellingPrice": 0,  // Optional for variant products (each variant has its own price)
  "productType": "STOCK",
  "categoryId": 1,
  "variants": [
    {
      "color": "Red",
      "size": "S",
      "unitCost": 10.00,
      "sellingPrice": 25.00,
      "quantity": 50,
      "reorderLevel": 10,
      "reorderQuantity": 100
    },
    {
      "color": "Red",
      "size": "M",
      "unitCost": 10.00,
      "sellingPrice": 25.00,
      "quantity": 100,
      "reorderLevel": 10,
      "reorderQuantity": 100
    },
    {
      "color": "Red",
      "size": "L",
      "unitCost": 10.00,
      "sellingPrice": 25.00,
      "quantity": 75,
      "reorderLevel": 10,
      "reorderQuantity": 100
    },
    {
      "color": "Red",
      "size": "XL",
      "unitCost": 12.00,
      "sellingPrice": 27.00,
      "quantity": 30,
      "reorderLevel": 10,
      "reorderQuantity": 50
    },
    {
      "color": "Blue",
      "size": "S",
      "unitCost": 10.00,
      "sellingPrice": 25.00,
      "quantity": 40,
      "reorderLevel": 10,
      "reorderQuantity": 100
    },
    {
      "color": "Blue",
      "size": "M",
      "unitCost": 10.00,
      "sellingPrice": 25.00,
      "quantity": 120,
      "reorderLevel": 10,
      "reorderQuantity": 100
    },
    {
      "color": "Blue",
      "size": "L",
      "unitCost": 10.00,
      "sellingPrice": 25.00,
      "quantity": 80,
      "reorderLevel": 10,
      "reorderQuantity": 100
    },
    {
      "color": "Blue",
      "size": "XL",
      "unitCost": 12.00,
      "sellingPrice": 27.00,
      "quantity": 25,
      "reorderLevel": 10,
      "reorderQuantity": 50
    },
    {
      "color": "Black",
      "size": "S",
      "unitCost": 10.00,
      "sellingPrice": 25.00,
      "quantity": 60,
      "reorderLevel": 10,
      "reorderQuantity": 100
    },
    {
      "color": "Black",
      "size": "M",
      "unitCost": 10.00,
      "sellingPrice": 25.00,
      "quantity": 150,
      "reorderLevel": 10,
      "reorderQuantity": 100
    },
    {
      "color": "Black",
      "size": "L",
      "unitCost": 10.00,
      "sellingPrice": 25.00,
      "quantity": 90,
      "reorderLevel": 10,
      "reorderQuantity": 100
    },
    {
      "color": "Black",
      "size": "XL",
      "unitCost": 12.00,
      "sellingPrice": 27.00,
      "quantity": 35,
      "reorderLevel": 10,
      "reorderQuantity": 50
    }
  ]
}

Files: [tshirt-red.jpg, tshirt-blue.jpg, tshirt-black.jpg]
```

---

## 📤 API Response Example

### Admin Response (includes all variant details)

```json
{
  "id": 1,
  "name": "Premium Cotton T-Shirt",
  "description": "High-quality cotton t-shirt available in multiple colors and sizes",
  "unitCost": 0.0,
  "sellingPrice": 0.0,
  "productType": "STOCK",
  "status": "ACTIVE",
  "totalStock": 855,  // Sum of all variant quantities
  "category": {
    "id": 1,
    "name": "Clothing"
  },
  "variants": [
    {
      "id": 1,
      "color": "Red",
      "size": "S",
      "unitCost": 10.00,
      "sellingPrice": 25.00,
      "quantity": 50,
      "reorderLevel": 10,
      "reorderQuantity": 100,
      "sku": "PREM-RED-S-4567",
      "isActive": true,
      "isInStock": true,
      "needsReorder": false
    },
    {
      "id": 2,
      "color": "Red",
      "size": "M",
      "unitCost": 10.00,
      "sellingPrice": 25.00,
      "quantity": 100,
      "reorderLevel": 10,
      "reorderQuantity": 100,
      "sku": "PREM-RED-M-4568",
      "isActive": true,
      "isInStock": true,
      "needsReorder": false
    }
    // ... all other variants
  ],
  "imageUrls": [
    "https://bucket.s3.region.amazonaws.com/products/1/uuid1.jpg",
    "https://bucket.s3.region.amazonaws.com/products/1/uuid2.jpg",
    "https://bucket.s3.region.amazonaws.com/products/1/uuid3.jpg"
  ],
  "createdAt": "2025-11-03T10:00:00Z",
  "updatedAt": "2025-11-03T10:00:00Z"
}
```

### Public Response (customer view)

```json
{
  "id": 1,
  "name": "Premium Cotton T-Shirt",
  "description": "High-quality cotton t-shirt available in multiple colors and sizes",
  "sellingPrice": 0.0,  // Base price (variants have individual prices)
  "productType": "STOCK",
  "status": "ACTIVE",
  "totalStock": 855,
  "category": {
    "id": 1,
    "name": "Clothing"
  },
  "variants": [
    {
      "id": 1,
      "color": "Red",
      "size": "S",
      "sellingPrice": 25.00,
      "quantity": 50,
      "sku": "PREM-RED-S-4567",
      "isActive": true,
      "isInStock": true
      // Note: unitCost is hidden from public view
    }
    // ... all other variants
  ],
  "imageUrls": [...],
  "createdAt": "2025-11-03T10:00:00Z",
  "updatedAt": "2025-11-03T10:00:00Z"
}
```

---

## 🔄 Product Types Comparison

### 1. Simple Stock Product (No Variants)
```json
{
  "name": "USB Cable",
  "unitCost": 2.00,
  "sellingPrice": 10.00,
  "productType": "STOCK",
  "categoryId": 5
  // No variants field - single stock tracking
}
```

### 2. Variant Stock Product (With Colors/Sizes)
```json
{
  "name": "T-Shirt",
  "productType": "STOCK",
  "categoryId": 1,
  "variants": [
    { "color": "Red", "size": "M", "unitCost": 10, "sellingPrice": 25, "quantity": 100 },
    { "color": "Blue", "size": "L", "unitCost": 10, "sellingPrice": 25, "quantity": 75 }
  ]
  // Each variant has its own stock, cost, and price
}
```

### 3. Non-Stock Product (Services)
```json
{
  "name": "Consulting Service",
  "sellingPrice": 500.00,
  "productType": "NON_STOCK",
  "categoryId": 10
  // No stock tracking - always available
}
```

---

## 🗄️ Database Schema

The system automatically creates these tables:

### products table
```sql
CREATE TABLE products (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    unit_cost DOUBLE PRECISION NOT NULL,
    selling_price DOUBLE PRECISION NOT NULL,
    product_type VARCHAR(20) NOT NULL DEFAULT 'STOCK',
    status VARCHAR(20) NOT NULL,
    category_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    FOREIGN KEY (category_id) REFERENCES categories(id)
);
```

### product_variants table (NEW!)
```sql
CREATE TABLE product_variants (
    id BIGSERIAL PRIMARY KEY,
    product_id BIGINT NOT NULL,
    color VARCHAR(255) NOT NULL,
    size VARCHAR(255) NOT NULL,
    unit_cost DOUBLE PRECISION NOT NULL,
    selling_price DOUBLE PRECISION NOT NULL,
    quantity INTEGER NOT NULL DEFAULT 0,
    reorder_level INTEGER NOT NULL DEFAULT 10,
    reorder_quantity INTEGER NOT NULL DEFAULT 50,
    sku VARCHAR(50),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE,
    UNIQUE (product_id, color, size)
);
```

### stocks table (for simple products)
```sql
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

---

## 💡 Key Features

### Auto-Generated SKU
Each variant gets a unique SKU automatically:
- Format: `PROD-COL-SIZE-RAND`
- Example: `TSHR-RED-M-4567`
- Customizable in the request

### Stock Tracking per Variant
- Each color/size combination has its own inventory
- Automatic reorder alerts when stock is low
- Total stock calculation across all variants

### Flexible Pricing
- Different sizes can have different prices (e.g., XL costs more)
- Different colors can have different costs
- Base price on product is optional when using variants

### Validation
- Ensures selling price >= unit cost
- Prevents duplicate color/size combinations
- Validates required fields per variant

---

## 🚀 Use Cases

### Fashion/Clothing
```
T-Shirts, Jeans, Dresses, Shoes
→ Colors: Red, Blue, Black, White
→ Sizes: XS, S, M, L, XL, XXL
```

### Electronics Accessories
```
Phone Cases
→ Colors: Clear, Black, Red
→ Models: iPhone 15, iPhone 14, Samsung S24
```

### Furniture
```
Office Chairs
→ Colors: Black, Gray, Blue
→ Sizes: Standard, Executive, Ergonomic
```

---

## 🎨 Frontend Integration Tips

### Display Variants as Dropdowns
```javascript
// Customer selects color and size
const selectedVariant = product.variants.find(v => 
  v.color === selectedColor && v.size === selectedSize
);

if (selectedVariant.isInStock) {
  // Show "Add to Cart" button
  // Display price: $${selectedVariant.sellingPrice}
} else {
  // Show "Out of Stock"
}
```

### Show Available Options Only
```javascript
// Get unique colors that are in stock
const availableColors = [...new Set(
  product.variants
    .filter(v => v.isInStock)
    .map(v => v.color)
)];

// Get available sizes for selected color
const availableSizes = product.variants
  .filter(v => v.color === selectedColor && v.isInStock)
  .map(v => v.size);
```

---

## 📊 Admin Dashboard Features

### Inventory Management
- View stock levels for each variant
- Get alerts for variants that need reordering
- Update quantities per variant
- Disable specific variants without deleting the product

### Profit Analysis
- Track profit margin per variant: `(sellingPrice - unitCost) / sellingPrice * 100`
- Identify best-selling color/size combinations
- Optimize inventory based on demand

---

## ✅ Testing Checklist

1. **Create product with variants** ✓
2. **Verify auto-generated SKUs** ✓
3. **Check stock quantities per variant** ✓
4. **Test unique constraint** (same color+size should fail) ✓
5. **Validate pricing rules** (selling price >= unit cost) ✓
6. **Test total stock calculation** ✓
7. **Verify cascade delete** (deleting product deletes variants) ✓

---

## 🎓 Summary

**What You Get:**
- 🎨 **Automatic variant generation** from colors and sizes
- 📦 **Individual stock tracking** per variant
- 💰 **Flexible pricing** per color/size
- 🏷️ **Auto-generated SKUs** for inventory management
- 📊 **Total stock calculation** across all variants
- ✨ **Clean API** responses for both admin and public views

**Strategy Pattern Applied:**
- STOCK products → Can have variants OR simple stock
- NON_STOCK products → No inventory tracking (services)
- Easy to extend with new product types (RENTAL, SUBSCRIPTION, etc.)

---

Ready to use! Just send the POST request with your variants array and the system handles the rest! 🚀

