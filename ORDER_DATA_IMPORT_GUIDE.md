# Order Data Import Guide

This guide explains how to import enhanced order sample data and network requests into MongoDB for testing and development.

## Overview

We've created comprehensive mock data for orders with:
- **8 detailed order records** with complete customer information, line items, shipping/billing addresses, and extrinsics
- **18 network request records** showing inbound/outbound communications for each order
- Realistic data including cXML and OCI formats, various customer types, and different order statuses

## Files Created

1. **mongodb-orders-sample-data.json** - Order documents with detailed line items, addresses, and metadata
2. **mongodb-order-network-requests-sample-data.json** - Network requests linked to orders showing the full request/response flow
3. **import-order-data.sh** - Shell script to import data into MongoDB

## Data Structure

### Orders Collection
Each order includes:
- Order ID, session key, customer information
- Multiple line items with pricing and descriptions
- Ship-to and bill-to addresses
- Tax calculations
- Extrinsics (custom metadata like project codes, cost centers, etc.)
- Status tracking (RECEIVED, PROCESSING, CONFIRMED)
- Mule ESB integration IDs
- Environment (PRODUCTION, STAGING)
- Source system (ARIBA, OCI)

### Network Requests Collection
Each network request includes:
- Linked to orders via `orderId` field
- Direction (INBOUND/OUTBOUND)
- Full request/response headers and bodies
- Timing information (duration in milliseconds)
- Source and destination systems
- Success/failure status
- Request types (cXML OrderRequest, cXML ConfirmationRequest, OCI OrderMessage, REST API)

## Import Instructions

### Option 1: Using the Import Script (Recommended)

```bash
# Make the script executable (already done)
chmod +x import-order-data.sh

# Run the import script with default settings (localhost:27017)
./import-order-data.sh

# Or specify custom MongoDB connection
MONGODB_URI="mongodb://user:password@hostname:27017" DATABASE_NAME="punchout" ./import-order-data.sh
```

### Option 2: Manual Import

```bash
# Import orders
mongoimport --uri="mongodb://localhost:27017" \
  --db=punchout \
  --collection=orders \
  --file=mongodb-orders-sample-data.json \
  --jsonArray \
  --drop

# Import network requests
mongoimport --uri="mongodb://localhost:27017" \
  --db=punchout \
  --collection=network_requests \
  --file=mongodb-order-network-requests-sample-data.json \
  --jsonArray
```

## Verifying the Import

After importing, verify the data:

```bash
# Connect to MongoDB
mongosh mongodb://localhost:27017/punchout

# Check order count
db.orders.countDocuments()
# Expected: 8

# Check network request count for orders
db.network_requests.countDocuments({orderId: {$exists: true}})
# Expected: 18

# View a sample order
db.orders.findOne()

# View network requests for a specific order
db.network_requests.find({orderId: "ORD-2025-001"}).pretty()
```

## Sample Data Summary

### Orders by Status:
- **CONFIRMED**: 5 orders (ORD-2025-001, 003, 007, 008, 010)
- **PROCESSING**: 1 order (ORD-2025-002)
- **RECEIVED**: 1 order (ORD-2025-005)

### Orders by Source:
- **ARIBA (cXML)**: 5 orders
- **OCI**: 3 orders

### Network Request Flow:
Each confirmed order typically has 3 network requests:
1. **INBOUND**: Order received from procurement platform
2. **OUTBOUND**: Order sent to Mule ESB for processing
3. **OUTBOUND**: Confirmation sent back to procurement platform

## UI Features

After importing the data, the UI will show:

### Orders List Page (`/orders`)
- All orders with filtering options
- Order status, customer, date, and total amount
- Clickable rows to view order details

### Order Detail Page (`/orders/[orderId]`)
- Complete order information (customer, dates, status)
- Order summary with totals
- Shipping and billing addresses
- Line items table with quantities and pricing
- **Network Requests section with tabs:**
  - All requests
  - Inbound requests only
  - Outbound requests only
- Extrinsics metadata
- Link to session if available

### Network Request Detail Page (`/orders/[orderId]/requests/[requestId]`)
- Request overview (timestamp, direction, method, status)
- Request and response headers
- Request and response bodies (formatted)
- Error messages (if any)
- Navigation back to order

## Example Orders

1. **ORD-2025-001** - WatersCorp Laboratory Supply
   - $1,250.50 total
   - 3 line items (HPLC columns and supplies)
   - Status: CONFIRMED

2. **ORD-2025-007** - Enterprise Analytical Services
   - $15,750.99 total (largest order)
   - 4 line items (annual service contract and consumables)
   - Status: CONFIRMED
   - High priority flag

3. **ORD-2025-002** - ClientCorp Pharmaceuticals
   - $890.75 total
   - Status: PROCESSING (not yet confirmed)
   - Update operation (modified quantity)

## Testing Scenarios

1. **View all orders**: Navigate to `/orders`
2. **View order details**: Click on any order to see full details
3. **Filter network requests**: Use the All/Inbound/Outbound tabs
4. **View request details**: Click "View" on any network request
5. **Navigate between orders and sessions**: Click session key link if available

## Next Steps

1. Start the backend services:
   ```bash
   ./start-all-services.sh
   ```

2. Start the frontend:
   ```bash
   cd punchout-ui-frontend
   npm run dev
   ```

3. Navigate to http://localhost:3000/orders

4. Explore the orders and their network requests!

## Troubleshooting

### MongoDB Connection Issues
- Ensure MongoDB is running: `mongosh mongodb://localhost:27017`
- Check the connection string in the import script
- Verify network connectivity to MongoDB server

### Import Failures
- Check file paths are correct
- Ensure JSON files are valid
- Verify you have write permissions to MongoDB

### Data Not Showing in UI
- Verify backend is running and connected to MongoDB
- Check browser console for API errors
- Ensure the correct MongoDB database name is configured in backend
- Check backend logs for any errors

## Additional Resources

- MongoDB Import Documentation: https://www.mongodb.com/docs/database-tools/mongoimport/
- Punchout Architecture: See PUNCHOUT_ARCHITECTURE.md
- Order Processing: See ORDER_PROCESSING_IMPLEMENTATION_SUMMARY.md
