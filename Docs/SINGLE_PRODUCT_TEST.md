# Simple Product Creation Test

## Endpoint
```
POST http://localhost:8080/api/v1/products
```

## Headers
```
Authorization: Bearer YOUR_JWT_TOKEN_HERE
Content-Type: multipart/form-data
```

## Form Data (Postman/Thunder Client)

### Basic Product Info
- **name**: `Cotton T-Shirt`
- **description**: `Premium quality cotton t-shirt`
- **productType**: `STOCK`
- **categoryId**: `1`

### Variant 1 (Red - Small)
- **variants[0].color**: `Red`
- **variants[0].size**: `S`
- **variants[0].unitCost**: `10.00`
- **variants[0].sellingPrice**: `25.00`
- **variants[0].quantity**: `100`

### Variant 2 (Red - Medium)
- **variants[1].color**: `Red`
- **variants[1].size**: `M`
- **variants[1].unitCost**: `10.00`
- **variants[1].sellingPrice**: `25.00`
- **variants[1].quantity**: `150`

### Variant 3 (Blue - Small)
- **variants[2].color**: `Blue`
- **variants[2].size**: `S`
- **variants[2].unitCost**: `10.00`
- **variants[2].sellingPrice**: `25.00`
- **variants[2].quantity**: `80`

### Images
- **files**: Select 1-6 image files

---

## ✅ What's Removed
- ❌ No `reorderLevel` field
- ❌ No `reorderQuantity` field

## 🎯 For On-Demand Products (No Color)
Just **omit** the color field:
- **variants[0].size**: `Standard`
- **variants[0].unitCost**: `5.00`
- **variants[0].sellingPrice**: `15.00`
- **variants[0].quantity**: `0`

---

## cURL Example

```bash
curl --location 'http://localhost:8080/api/v1/products' \
--header 'Authorization: Bearer YOUR_JWT_TOKEN' \
--form 'name="Cotton T-Shirt"' \
--form 'description="Premium quality cotton t-shirt"' \
--form 'productType="STOCK"' \
--form 'categoryId="1"' \
--form 'variants[0].color="Red"' \
--form 'variants[0].size="S"' \
--form 'variants[0].unitCost="10.00"' \
--form 'variants[0].sellingPrice="25.00"' \
--form 'variants[0].quantity="100"' \
--form 'variants[1].color="Red"' \
--form 'variants[1].size="M"' \
--form 'variants[1].unitCost="10.00"' \
--form 'variants[1].sellingPrice="25.00"' \
--form 'variants[1].quantity="150"' \
--form 'files=@"/path/to/image1.jpg"' \
--form 'files=@"/path/to/image2.jpg"'
```

---

## Expected Response
```json
{
  "status": "SUCCESS",
  "message": "Product created successfully",
  "data": {
    "id": 1,
    "name": "Cotton T-Shirt",
    "description": "Premium quality cotton t-shirt",
    "productType": "STOCK",
    "status": "ACTIVE",
    "variants": [
      {
        "id": 1,
        "color": "Red",
        "size": "S",
        "unitCost": 10.00,
        "sellingPrice": 25.00,
        "quantity": 100,
        "sku": "COTT-RED-S-1234",
        "isActive": true,
        "isInStock": true
      },
      {
        "id": 2,
        "color": "Red",
        "size": "M",
        "unitCost": 10.00,
        "sellingPrice": 25.00,
        "quantity": 150,
        "sku": "COTT-RED-M-1235",
        "isActive": true,
        "isInStock": true
      }
    ],
    "imageUrls": [
      "https://s3.amazonaws.com/bucket/products/1/image1.jpg",
      "https://s3.amazonaws.com/bucket/products/1/image2.jpg"
    ],
    "totalStock": 250
  }
}
```

