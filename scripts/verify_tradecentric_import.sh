#!/bin/bash

# Quick verification script to check TradeCentric data import

echo "🔍 Verifying TradeCentric Data Import"
echo "======================================"
echo ""

# Check total count
echo "📊 Total TradeCentric sessions imported:"
mongosh punchout --quiet --eval "db.customer_onboarding.countDocuments({source: 'tradecentric_import'})" 2>&1 | grep -v "Warning"
echo ""

# Check by environment
echo "📊 Sessions by environment:"
mongosh punchout --quiet --eval "
db.customer_onboarding.aggregate([
  { \$match: { source: 'tradecentric_import' } },
  { \$group: { _id: '\$environment', count: { \$sum: 1 } } },
  { \$sort: { _id: 1 } }
]).forEach(doc => print('  ' + doc._id + ': ' + doc.count + ' sessions'))
" 2>&1 | grep -v "Warning"
echo ""

# List all customers for 'prod' environment
echo "👥 Customers available in PROD environment:"
mongosh punchout --quiet --eval "
db.customer_onboarding.find(
  { source: 'tradecentric_import', environment: 'prod' },
  { customerName: 1, _id: 0 }
).sort({ customerName: 1 }).forEach(doc => print('  - ' + doc.customerName))
" 2>&1 | grep -v "Warning"
echo ""

echo "✅ Verification complete!"
echo ""
echo "These customers should now appear at:"
echo "http://localhost:3000/developer/punchout when PROD is selected"
