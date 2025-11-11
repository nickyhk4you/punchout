#!/bin/bash

# Import environment configuration data into MongoDB

MONGODB_URI=${MONGODB_URI:-"mongodb://localhost:27017"}
DATABASE_NAME=${DATABASE_NAME:-"punchout"}

echo "========================================="
echo "Importing Environment Configurations"
echo "========================================="
echo "MongoDB URI: $MONGODB_URI"
echo "Database: $DATABASE_NAME"
echo "========================================="

# Import environment configs
echo ""
echo "Importing environment configurations..."
mongoimport --uri="$MONGODB_URI" \
  --db="$DATABASE_NAME" \
  --collection=environment_configs \
  --file=mongodb-environment-configs-sample-data.json \
  --jsonArray \
  --drop

if [ $? -eq 0 ]; then
  echo "✓ Successfully imported environment configurations"
else
  echo "✗ Failed to import environment configurations"
  exit 1
fi

echo ""
echo "========================================="
echo "Data Import Complete!"
echo "========================================="
echo ""
echo "Summary:"
mongosh "$MONGODB_URI/$DATABASE_NAME" --quiet --eval "
  const count = db.environment_configs.countDocuments();
  print('Environment configurations imported: ' + count);
  print('');
  print('Configurations:');
  db.environment_configs.find({}, {environment: 1, authServiceUrl: 1, muleServiceUrl: 1, enabled: 1, _id: 0}).forEach(doc => {
    print('  - ' + doc.environment + ': ' + (doc.enabled ? 'ENABLED' : 'DISABLED'));
    print('    Auth: ' + doc.authServiceUrl);
    print('    Mule: ' + doc.muleServiceUrl);
  });
"

echo ""
echo "To view the configurations:"
echo "  mongosh $MONGODB_URI/$DATABASE_NAME"
echo "  > db.environment_configs.find().pretty()"
echo ""
echo "API Endpoints:"
echo "  GET  http://localhost:8080/api/environment-config         - List all configs"
echo "  GET  http://localhost:8080/api/environment-config/dev     - Get specific config"
echo "  GET  http://localhost:8080/api/environment-config/current - Get current config"
echo "  POST http://localhost:8080/api/environment-config         - Create new config"
echo "  PUT  http://localhost:8080/api/environment-config/dev     - Update config"
