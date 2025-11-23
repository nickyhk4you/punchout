# Importing TradeCentric Session Data to MongoDB

This guide explains how to import extracted TradeCentric session data into MongoDB for testing.

## Quick Start

```bash
# 1. Download session data from TradeCentric
cd scripts
python3 download_tradecentric_data_api.py 20

# 2. Import to MongoDB
cd ..
./import-tradecentric-data.sh
```

## What Gets Imported

The import script processes session files from `data/tradecentric_import/` and imports them into MongoDB:

**Collection:** `punchout_tests`

**Document Structure:**
```json
{
  "sessionId": "rh69224039e025d",
  "environment": "production",
  "customerName": "JJ",
  "routeName": "[Prod] J&J",
  "extractedAt": "2025-11-23T17:27:43.123456",
  "cxmlRequest": "<?xml version=\"1.0\"...",
  "jsonResponse": "{\"operation\":\"create\"...",
  "importedAt": "2025-11-23T18:00:00.000000",
  "source": "tradecentric_import"
}
```

## Environment Mapping

Sessions are automatically categorized by environment based on the filename prefix:

| Filename Prefix | MongoDB Environment |
|----------------|---------------------|
| `Prod`, `Production` | `production` |
| `PreProd`, `Pre-Prod` | `preproduction` |
| `Staging`, `Stage` | `staging` |
| `Dev`, `Development` | `development` |

**Examples:**
- `session_Prod_JJ_...` → `production`
- `session_Dev_TestCustomer_...` → `development`
- `session_Staging_Ariba_...` → `staging`

## Usage

### Using the Shell Script (Recommended)

```bash
# Import all sessions from data/tradecentric_import/
./import-tradecentric-data.sh

# With custom MongoDB connection
MONGO_HOST=mongodb.example.com MONGO_PORT=27017 ./import-tradecentric-data.sh
```

### Using Python Script Directly

```bash
cd scripts
python3 import_tradecentric_to_mongodb.py

# With custom MongoDB connection
MONGO_HOST=mongodb.example.com MONGO_PORT=27017 python3 import_tradecentric_to_mongodb.py
```

## Verifying Import

Check imported sessions in MongoDB:

```bash
# Connect to MongoDB
mongosh

# Use the database
use punchout

# Count sessions by environment
db.punchout_tests.aggregate([
  { $group: { _id: "$environment", count: { $sum: 1 } } }
])

# Find all production sessions
db.punchout_tests.find({ environment: "production" })

# Find sessions for a specific customer
db.punchout_tests.find({ customerName: "JJ" })
```

## Re-importing Data

The import script is **idempotent** - it will skip sessions that already exist (same `sessionId` and `environment`).

To force re-import:
1. Delete existing data: `db.punchout_tests.deleteMany({ source: "tradecentric_import" })`
2. Run import script again

## Troubleshooting

### No sessions found
```
Error: No session files found in data/tradecentric_import/
```
**Solution:** Run `download_tradecentric_data_api.py` first to download sessions

### Connection refused
```
❌ Failed to connect to MongoDB
```
**Solution:** 
- Ensure MongoDB is running: `docker ps` or `mongod --version`
- Check connection details: `MONGO_HOST` and `MONGO_PORT`
- For Docker: `MONGO_HOST=localhost MONGO_PORT=27017`

### Import errors
```
❌ Error importing session...
```
**Solution:** Check the error message for details. Common issues:
- Invalid JSON in session files
- Corrupted cXML data
- MongoDB permissions

## Using Imported Sessions for Testing

After import, you can test PunchOut flows using the imported session data:

```bash
# Query sessions for testing
db.punchout_tests.find({
  environment: "production",
  customerName: "JJ"
})

# Get session details
db.punchout_tests.findOne({ sessionId: "rh69224039e025d" })
```

The imported cXML requests and JSON responses can be used to:
1. Test PunchOut gateway converters
2. Validate environment configurations
3. Debug production issues in dev/staging
4. Create regression test suites

## Integration with Tests

You can reference imported sessions in your tests:

```java
// Example: Load test data from MongoDB
@Test
public void testAribaConverter() {
    Document session = mongoCollection.find(
        Filters.and(
            Filters.eq("environment", "production"),
            Filters.eq("customerName", "Ariba")
        )
    ).first();
    
    String cxmlRequest = session.getString("cxmlRequest");
    // ... test with real production data
}
```
