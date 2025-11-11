#!/bin/bash

# Import order and network request data into MongoDB
# This script imports the sample order data and network requests

MONGODB_URI=${MONGODB_URI:-"mongodb://localhost:27017"}
DATABASE_NAME=${DATABASE_NAME:-"punchout"}

echo "========================================="
echo "Importing Order Data to MongoDB"
echo "========================================="
echo "MongoDB URI: $MONGODB_URI"
echo "Database: $DATABASE_NAME"
echo "========================================="

# Import orders
echo ""
echo "Importing orders..."
mongoimport --uri="$MONGODB_URI" \
  --db="$DATABASE_NAME" \
  --collection=orders \
  --file=mongodb-orders-sample-data.json \
  --jsonArray \
  --drop

if [ $? -eq 0 ]; then
  echo "✓ Successfully imported orders"
else
  echo "✗ Failed to import orders"
  exit 1
fi

# Import network requests for orders
echo ""
echo "Importing network requests for orders..."
mongoimport --uri="$MONGODB_URI" \
  --db="$DATABASE_NAME" \
  --collection=network_requests \
  --file=mongodb-order-network-requests-sample-data.json \
  --jsonArray

if [ $? -eq 0 ]; then
  echo "✓ Successfully imported network requests"
else
  echo "✗ Failed to import network requests"
  exit 1
fi

echo ""
echo "========================================="
echo "Data Import Complete!"
echo "========================================="
echo ""
echo "Summary:"
mongosh "$MONGODB_URI/$DATABASE_NAME" --quiet --eval "
  const orderCount = db.orders.countDocuments();
  const networkRequestCount = db.network_requests.countDocuments({orderId: {\$exists: true}});
  print('Orders imported: ' + orderCount);
  print('Network requests for orders: ' + networkRequestCount);
"

echo ""
echo "To view the data, you can use:"
echo "  mongosh $MONGODB_URI/$DATABASE_NAME"
echo "  > db.orders.find().pretty()"
echo "  > db.network_requests.find({orderId: {$exists: true}}).pretty()"
