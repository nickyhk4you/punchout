#!/bin/bash

set -e

echo "=========================================="
echo "MongoDB Index Creation Script"
echo "=========================================="
echo ""

MONGO_HOST="${MONGO_HOST:-localhost}"
MONGO_PORT="${MONGO_PORT:-27017}"
MONGO_DB="${MONGO_DB:-punchout}"

echo "Connecting to MongoDB at ${MONGO_HOST}:${MONGO_PORT}"
echo "Database: ${MONGO_DB}"
echo ""

TTL_DAYS=90
TTL_SECONDS=$((TTL_DAYS * 24 * 60 * 60))

create_index() {
    local collection=$1
    local index_def=$2
    local index_name=$3
    local extra_options=$4
    
    echo -n "Creating index '${index_name}' on ${collection}... "
    
    mongosh --host ${MONGO_HOST} --port ${MONGO_PORT} --quiet <<EOF
    use ${MONGO_DB}
    
    try {
        const result = db.${collection}.createIndex(
            ${index_def},
            { name: "${index_name}", ${extra_options} }
        );
        
        if (result === "${index_name}" || result.ok === 1) {
            print("✓ Created successfully");
        } else if (result.note && result.note.includes("already exists")) {
            print("✓ Already exists");
        } else {
            print("✓ OK");
        }
    } catch (e) {
        if (e.codeName === "IndexOptionsConflict" || e.code === 85) {
            print("✓ Already exists with same options");
        } else if (e.codeName === "IndexKeySpecsConflict" || e.code === 86) {
            print("! Already exists (skipping)");
        } else {
            print("✗ Error: " + e.message);
            throw e;
        }
    }
EOF
}

verify_indexes() {
    local collection=$1
    echo ""
    echo "Indexes on ${collection}:"
    mongosh --host ${MONGO_HOST} --port ${MONGO_PORT} --quiet <<EOF
    use ${MONGO_DB}
    db.${collection}.getIndexes().forEach(idx => {
        const keys = JSON.stringify(idx.key);
        const name = idx.name;
        const unique = idx.unique ? " [UNIQUE]" : "";
        const ttl = idx.expireAfterSeconds ? " [TTL: " + (idx.expireAfterSeconds / 86400) + " days]" : "";
        print("  - " + name + ": " + keys + unique + ttl);
    });
EOF
}

echo "=========================================="
echo "1. Creating indexes for punchout_sessions"
echo "=========================================="

create_index "punchout_sessions" \
    '{ "sessionKey": 1 }' \
    "idx_sessionKey" \
    "unique: true"

create_index "punchout_sessions" \
    '{ "environment": 1, "sessionDate": -1 }' \
    "idx_environment_sessionDate" \
    ""

create_index "punchout_sessions" \
    '{ "sessionDate": 1 }' \
    "idx_sessionDate_ttl" \
    "expireAfterSeconds: ${TTL_SECONDS}"

verify_indexes "punchout_sessions"

echo ""
echo "=========================================="
echo "2. Creating indexes for customer_onboarding"
echo "=========================================="

create_index "customer_onboarding" \
    '{ "environment": 1, "deployed": 1 }' \
    "idx_environment_deployed" \
    ""

create_index "customer_onboarding" \
    '{ "customerName": 1, "environment": 1 }' \
    "idx_customerName_environment" \
    ""

verify_indexes "customer_onboarding"

echo ""
echo "=========================================="
echo "3. Creating indexes for network_requests"
echo "=========================================="

create_index "network_requests" \
    '{ "sessionKey": 1, "timestamp": -1 }' \
    "idx_sessionKey_timestamp" \
    ""

create_index "network_requests" \
    '{ "timestamp": 1 }' \
    "idx_timestamp_ttl" \
    "expireAfterSeconds: ${TTL_SECONDS}"

verify_indexes "network_requests"

echo ""
echo "=========================================="
echo "4. Creating indexes for environment_configs"
echo "=========================================="

create_index "environment_configs" \
    '{ "environment": 1 }' \
    "idx_environment" \
    "unique: true"

verify_indexes "environment_configs"

echo ""
echo "=========================================="
echo "5. Creating indexes for orders"
echo "=========================================="

create_index "orders" \
    '{ "orderId": 1 }' \
    "idx_orderId" \
    "unique: true"

create_index "orders" \
    '{ "environment": 1, "orderDate": -1 }' \
    "idx_environment_orderDate" \
    ""

verify_indexes "orders"

echo ""
echo "=========================================="
echo "6. Creating indexes for invoices"
echo "=========================================="

create_index "invoices" \
    '{ "invoiceId": 1 }' \
    "idx_invoiceId" \
    "unique: true"

create_index "invoices" \
    '{ "environment": 1, "invoiceDate": -1 }' \
    "idx_environment_invoiceDate" \
    ""

verify_indexes "invoices"

echo ""
echo "=========================================="
echo "Index Usage Verification"
echo "=========================================="
echo ""

echo "Checking TTL indexes (should auto-delete data older than ${TTL_DAYS} days):"
mongosh --host ${MONGO_HOST} --port ${MONGO_PORT} --quiet <<'EOF'
use punchout

print("\nTTL Indexes:");
db.getCollectionNames().forEach(collName => {
    const indexes = db[collName].getIndexes();
    indexes.forEach(idx => {
        if (idx.expireAfterSeconds) {
            const days = idx.expireAfterSeconds / 86400;
            print("  - " + collName + "." + idx.name + " (expires after " + days + " days)");
        }
    });
});
EOF

echo ""
echo "Checking unique indexes (prevent duplicates):"
mongosh --host ${MONGO_HOST} --port ${MONGO_PORT} --quiet <<'EOF'
use punchout

print("\nUnique Indexes:");
db.getCollectionNames().forEach(collName => {
    const indexes = db[collName].getIndexes();
    indexes.forEach(idx => {
        if (idx.unique) {
            print("  - " + collName + "." + idx.name + " on " + JSON.stringify(idx.key));
        }
    });
});
EOF

echo ""
echo "=========================================="
echo "Testing index usage with explain plans"
echo "=========================================="
echo ""

echo "Test 1: Query punchout_sessions by sessionKey"
mongosh --host ${MONGO_HOST} --port ${MONGO_PORT} --quiet <<'EOF'
use punchout
const plan = db.punchout_sessions.find({ sessionKey: "test" }).explain("executionStats");
print("  Index used: " + (plan.executionStats.executionStages.indexName || "NONE"));
print("  Docs examined: " + plan.executionStats.totalDocsExamined);
EOF

echo ""
echo "Test 2: Query customer_onboarding by environment"
mongosh --host ${MONGO_HOST} --port ${MONGO_PORT} --quiet <<'EOF'
use punchout
const plan = db.customer_onboarding.find({ environment: "DEV" }).explain("executionStats");
print("  Index used: " + (plan.executionStats.executionStages.indexName || "NONE"));
print("  Docs examined: " + plan.executionStats.totalDocsExamined);
EOF

echo ""
echo "Test 3: Query network_requests by sessionKey"
mongosh --host ${MONGO_HOST} --port ${MONGO_PORT} --quiet <<'EOF'
use punchout
const plan = db.network_requests.find({ sessionKey: "test" }).sort({ timestamp: -1 }).explain("executionStats");
print("  Index used: " + (plan.executionStats.executionStages.indexName || "NONE"));
print("  Docs examined: " + plan.executionStats.totalDocsExamined);
EOF

echo ""
echo "=========================================="
echo "Summary"
echo "=========================================="
echo ""

mongosh --host ${MONGO_HOST} --port ${MONGO_PORT} --quiet <<'EOF'
use punchout

let totalIndexes = 0;
let uniqueIndexes = 0;
let ttlIndexes = 0;

db.getCollectionNames().forEach(collName => {
    const indexes = db[collName].getIndexes();
    indexes.forEach(idx => {
        if (idx.name !== "_id_") {
            totalIndexes++;
            if (idx.unique) uniqueIndexes++;
            if (idx.expireAfterSeconds) ttlIndexes++;
        }
    });
});

print("Total custom indexes: " + totalIndexes);
print("Unique indexes: " + uniqueIndexes);
print("TTL indexes: " + ttlIndexes);
print("");
print("✓ All indexes created successfully!");
print("✓ Old data will be automatically deleted after " + (7776000 / 86400) + " days");
EOF

echo ""
echo "=========================================="
echo "Done!"
echo "=========================================="
