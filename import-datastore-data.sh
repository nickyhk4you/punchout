#!/bin/bash

# Script to import customer datastore sample data into MongoDB
# This script imports the sample data from mongodb-customer-datastore-sample-data.json

echo "========================================"
echo "Importing Customer Datastore Sample Data"
echo "========================================"

# MongoDB connection details
MONGO_HOST=${MONGO_HOST:-localhost}
MONGO_PORT=${MONGO_PORT:-27017}
MONGO_DB=${MONGO_DB:-punchout}

echo ""
echo "MongoDB Host: $MONGO_HOST"
echo "MongoDB Port: $MONGO_PORT"
echo "Database: $MONGO_DB"
echo ""

# Check if the JSON file exists
if [ ! -f "mongodb-customer-datastore-sample-data.json" ]; then
    echo "Error: mongodb-customer-datastore-sample-data.json not found!"
    exit 1
fi

# Import the data
echo "Importing customer datastore data..."
mongoimport \
    --host $MONGO_HOST \
    --port $MONGO_PORT \
    --db $MONGO_DB \
    --collection customer_datastore \
    --file mongodb-customer-datastore-sample-data.json \
    --jsonArray \
    --mode upsert

if [ $? -eq 0 ]; then
    echo ""
    echo "✓ Customer datastore sample data imported successfully!"
    echo ""
    echo "Imported records:"
    echo "  - ACME (dev, prod)"
    echo "  - COUPA (dev, prod)"
    echo "  - ARIBA (stage)"
    echo "  - ORACLE (dev, prod)"
    echo "  - SAP (s4-dev)"
    echo "  - TEST_CUSTOMER (dev - disabled)"
    echo ""
else
    echo ""
    echo "✗ Error importing customer datastore data"
    exit 1
fi

echo "========================================"
echo "Import Complete!"
echo "========================================"
