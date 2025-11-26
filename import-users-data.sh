#!/bin/bash

# Import user data into MongoDB

MONGODB_URI=${MONGODB_URI:-"mongodb://localhost:27017"}
DATABASE_NAME=${DATABASE_NAME:-"punchout"}

echo "========================================="
echo "Importing User Data"
echo "========================================="
echo "MongoDB URI: $MONGODB_URI"
echo "Database: $DATABASE_NAME"
echo "========================================="

# Import users
echo ""
echo "Importing users..."
mongoimport --uri="$MONGODB_URI" \
  --db="$DATABASE_NAME" \
  --collection=users \
  --file=mongodb-users-sample-data.json \
  --jsonArray \
  --drop

if [ $? -eq 0 ]; then
  echo "✓ Successfully imported users"
else
  echo "✗ Failed to import users"
  exit 1
fi

# Create indexes for users collection
echo ""
echo "Creating indexes for users collection..."
mongosh "$MONGODB_URI/$DATABASE_NAME" --quiet --eval "
  db.users.createIndex({ userId: 1 }, { unique: true });
  print('  - users: userId (unique)');
  
  db.users.createIndex({ username: 1 }, { unique: true });
  print('  - users: username (unique)');
  
  db.users.createIndex({ email: 1 }, { unique: true });
  print('  - users: email (unique)');
  
  db.users.createIndex({ role: 1, status: 1 });
  print('  - users: role + status');
  
  db.users.createIndex({ department: 1 });
  print('  - users: department');
  
  db.users.createIndex({ status: 1 });
  print('  - users: status');
  
  db.users.createIndex({ createdAt: -1 });
  print('  - users: createdAt (descending)');
  
  db.users.createIndex({ lastLoginAt: -1 });
  print('  - users: lastLoginAt (descending)');
"

echo ""
echo "========================================="
echo "Data Import Complete!"
echo "========================================="
echo ""
echo "Summary:"
mongosh "$MONGODB_URI/$DATABASE_NAME" --quiet --eval "
  const count = db.users.countDocuments();
  print('Users imported: ' + count);
  print('');
  print('User breakdown:');
  db.users.aggregate([
    { \$group: { _id: '\$role', count: { \$sum: 1 } } },
    { \$sort: { count: -1 } }
  ]).forEach(doc => {
    print('  ' + doc._id + ': ' + doc.count + ' users');
  });
  print('');
  print('Status breakdown:');
  db.users.aggregate([
    { \$group: { _id: '\$status', count: { \$sum: 1 } } },
    { \$sort: { count: -1 } }
  ]).forEach(doc => {
    print('  ' + doc._id + ': ' + doc.count + ' users');
  });
"

echo ""
echo "API Endpoints:"
echo "  GET  http://localhost:8080/api/users                - List all users"
echo "  GET  http://localhost:8080/api/users/{id}           - Get user by ID"
echo "  GET  http://localhost:8080/api/users/role/ADMIN     - Get users by role"
echo "  GET  http://localhost:8080/api/users/status/ACTIVE  - Get users by status"
echo "  GET  http://localhost:8080/api/users/search?q=john  - Search users"
echo "  POST http://localhost:8080/api/users                - Create new user"
echo "  PUT  http://localhost:8080/api/users/{id}           - Update user"
echo "  DELETE http://localhost:8080/api/users/{id}         - Delete user"
echo ""
