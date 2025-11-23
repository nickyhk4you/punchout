#!/bin/bash

# Script to import customer onboarding sample data into MongoDB
# This script imports the sample data from mongodb-customer-onboarding-sample-data.json

echo "========================================"
echo "Importing Customer Onboarding Sample Data"
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
if [ ! -f "mongodb-customer-onboarding-sample-data.json" ]; then
    echo "Error: mongodb-customer-onboarding-sample-data.json not found!"
    exit 1
fi

# Import the data
echo "Importing customer onboarding data..."
mongoimport \
    --host $MONGO_HOST \
    --port $MONGO_PORT \
    --db $MONGO_DB \
    --collection customer_onboarding \
    --file mongodb-customer-onboarding-sample-data.json \
    --jsonArray \
    --mode upsert

if [ $? -eq 0 ]; then
    echo ""
    echo "✓ Customer onboarding sample data imported successfully!"
    echo ""
    echo "Imported records:"
    echo "  - ACME Corporation (CUSTOM, dev) - DEPLOYED"
    echo "  - Global Manufacturing Inc (COUPA, prod) - DEPLOYED"
    echo "  - TechCorp Solutions (ARIBA, stage) - READY_TO_DEPLOY"
    echo "  - PharmaCo International (ORACLE, dev) - DRAFT"
    echo ""
    echo "These onboarding configurations can now be used for PunchOut testing!"
    echo ""
else
    echo ""
    echo "✗ Error importing customer onboarding data"
    exit 1
fi

echo "========================================"
echo "Import Complete!"
echo "========================================"
