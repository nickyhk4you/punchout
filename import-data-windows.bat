@echo off
setlocal enabledelayedexpansion

REM ============================================
REM Import All MongoDB Data - Windows
REM ============================================

echo.
echo ============================================
echo Importing PunchOut MongoDB Data
echo ============================================
echo.

REM Configuration
set MONGODB_URI=mongodb://localhost:27017
set DATABASE_NAME=punchout

REM Check if mongoimport is installed
where mongoimport >nul 2>&1
if %errorlevel% neq 0 (
    echo ERROR: mongoimport is not installed or not in PATH
    echo Please ensure MongoDB tools are installed
    pause
    exit /b 1
)

echo MongoDB URI: %MONGODB_URI%
echo Database:    %DATABASE_NAME%
echo.

REM Get current directory
set PROJECT_ROOT=%~dp0
cd /d "%PROJECT_ROOT%"

echo ============================================
echo 1. Importing Environment Configurations
echo ============================================
mongoimport --uri="%MONGODB_URI%" ^
  --db="%DATABASE_NAME%" ^
  --collection=environment_configs ^
  --file=mongodb-environment-configs-sample-data.json ^
  --jsonArray ^
  --drop

if %errorlevel% neq 0 (
    echo ERROR: Failed to import environment configurations
    pause
    exit /b 1
)
echo SUCCESS: Environment configurations imported
echo.

echo ============================================
echo 2. Importing Customer Onboarding Data
echo ============================================
mongoimport --uri="%MONGODB_URI%" ^
  --db="%DATABASE_NAME%" ^
  --collection=customer_onboarding ^
  --file=mongodb-customer-onboarding-sample-data.json ^
  --jsonArray ^
  --drop

if %errorlevel% neq 0 (
    echo ERROR: Failed to import customer onboarding data
    pause
    exit /b 1
)
echo SUCCESS: Customer onboarding data imported
echo.

echo ============================================
echo 3. Importing Customer Datastore
echo ============================================
mongoimport --uri="%MONGODB_URI%" ^
  --db="%DATABASE_NAME%" ^
  --collection=customer_datastore ^
  --file=mongodb-customer-datastore-sample-data.json ^
  --jsonArray ^
  --drop

if %errorlevel% neq 0 (
    echo ERROR: Failed to import customer datastore
    pause
    exit /b 1
)
echo SUCCESS: Customer datastore imported
echo.

echo ============================================
echo 4. Importing cXML Templates
echo ============================================
mongoimport --uri="%MONGODB_URI%" ^
  --db="%DATABASE_NAME%" ^
  --collection=cxml_templates ^
  --file=mongodb-cxml-templates-sample-data.json ^
  --jsonArray ^
  --drop

if %errorlevel% neq 0 (
    echo ERROR: Failed to import cXML templates
    pause
    exit /b 1
)
echo SUCCESS: cXML templates imported
echo.

echo ============================================
echo 5. Creating MongoDB Indexes
echo ============================================

REM Create indexes using mongosh
mongosh "%MONGODB_URI%/%DATABASE_NAME%" --quiet --eval "print('Creating indexes...');"

REM PunchOut Sessions indexes
mongosh "%MONGODB_URI%/%DATABASE_NAME%" --quiet --eval "db.punchout_sessions.createIndex({ sessionKey: 1 }, { unique: true }); print('- punchout_sessions: sessionKey (unique)');"
mongosh "%MONGODB_URI%/%DATABASE_NAME%" --quiet --eval "db.punchout_sessions.createIndex({ environment: 1, sessionDate: -1 }); print('- punchout_sessions: environment + sessionDate');"
mongosh "%MONGODB_URI%/%DATABASE_NAME%" --quiet --eval "db.punchout_sessions.createIndex({ sessionDate: 1 }, { expireAfterSeconds: 7776000 }); print('- punchout_sessions: TTL 90 days');"

REM Customer Onboarding indexes
mongosh "%MONGODB_URI%/%DATABASE_NAME%" --quiet --eval "db.customer_onboarding.createIndex({ environment: 1, deployed: 1 }); print('- customer_onboarding: environment + deployed');"
mongosh "%MONGODB_URI%/%DATABASE_NAME%" --quiet --eval "db.customer_onboarding.createIndex({ customerName: 1, environment: 1 }); print('- customer_onboarding: customerName + environment');"

REM Network Requests indexes
mongosh "%MONGODB_URI%/%DATABASE_NAME%" --quiet --eval "db.network_requests.createIndex({ sessionKey: 1, timestamp: -1 }); print('- network_requests: sessionKey + timestamp');"
mongosh "%MONGODB_URI%/%DATABASE_NAME%" --quiet --eval "db.network_requests.createIndex({ timestamp: 1 }, { expireAfterSeconds: 7776000 }); print('- network_requests: TTL 90 days');"

REM Environment Configs indexes
mongosh "%MONGODB_URI%/%DATABASE_NAME%" --quiet --eval "db.environment_configs.createIndex({ environment: 1 }, { unique: true }); print('- environment_configs: environment (unique)');"

REM Orders indexes
mongosh "%MONGODB_URI%/%DATABASE_NAME%" --quiet --eval "db.orders.createIndex({ orderId: 1 }, { unique: true }); print('- orders: orderId (unique)');"
mongosh "%MONGODB_URI%/%DATABASE_NAME%" --quiet --eval "db.orders.createIndex({ environment: 1, orderDate: -1 }); print('- orders: environment + orderDate');"

REM Invoices indexes
mongosh "%MONGODB_URI%/%DATABASE_NAME%" --quiet --eval "db.invoices.createIndex({ environment: 1, invoiceDate: -1 }); print('- invoices: environment + invoiceDate');"

echo.
echo SUCCESS: Indexes created
echo.

echo ============================================
echo Data Import Summary
echo ============================================

REM Show summary using mongosh
mongosh "%MONGODB_URI%/%DATABASE_NAME%" --quiet --eval "
  print('Collections and Document Counts:');
  print('  environment_configs:  ' + db.environment_configs.countDocuments());
  print('  customer_onboarding:  ' + db.customer_onboarding.countDocuments());
  print('  customer_datastore:   ' + db.customer_datastore.countDocuments());
  print('  cxml_templates:       ' + db.cxml_templates.countDocuments());
  print('');
  print('Environment Configurations:');
  db.environment_configs.find({}, {environment: 1, enabled: 1, _id: 0}).forEach(doc => {
    print('  - ' + doc.environment + ': ' + (doc.enabled ? 'ENABLED' : 'DISABLED'));
  });
"

echo.
echo ============================================
echo Import Complete!
echo ============================================
echo.
echo To verify data:
echo   mongosh %MONGODB_URI%/%DATABASE_NAME%
echo   db.environment_configs.find().pretty()
echo.
echo Next steps:
echo   1. Run: start-all-services-windows.bat
echo   2. Access UI: http://localhost:8080
echo.
pause
