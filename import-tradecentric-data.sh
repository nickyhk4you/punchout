#!/bin/bash

# Script to import TradeCentric session data into MongoDB
# This script imports extracted session data from data/tradecentric_import/

echo "========================================"
echo "Importing TradeCentric Session Data"
echo "========================================"

# MongoDB connection details
MONGO_HOST=${MONGO_HOST:-localhost}
MONGO_PORT=${MONGO_PORT:-27017}
MONGO_DB=${MONGO_DB:-punchout}

echo ""
echo "MongoDB Host: $MONGO_HOST"
echo "MongoDB Port: $MONGO_PORT"
echo "Database: $MONGO_DB"
echo "Collection: customer_onboarding"
echo ""

# Check if the data directory exists
if [ ! -d "data/tradecentric_import" ]; then
    echo "Error: data/tradecentric_import directory not found!"
    echo "Please run scripts/download_tradecentric_data_api.py first"
    exit 1
fi

# Check if there are any session files
SESSION_COUNT=$(ls data/tradecentric_import/session_*_metadata.json 2>/dev/null | wc -l)
if [ "$SESSION_COUNT" -eq 0 ]; then
    echo "Error: No session files found in data/tradecentric_import/"
    echo "Please run scripts/download_tradecentric_data_api.py first"
    exit 1
fi

echo "Found $SESSION_COUNT session(s) to import"
echo ""

# Run the Python import script
python3 scripts/import_tradecentric_to_mongodb.py

if [ $? -eq 0 ]; then
    echo ""
    echo "✓ TradeCentric session data imported successfully!"
    echo ""
    echo "You can now test PunchOut sessions using the imported data."
    echo "Sessions are organized by environment in the punchout_tests collection."
    echo ""
else
    echo ""
    echo "✗ Error importing TradeCentric session data"
    exit 1
fi

echo "========================================"
echo "Import Complete!"
echo "========================================"
