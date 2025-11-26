@echo off
setlocal

REM ============================================
REM Import User Data - Windows
REM ============================================

echo.
echo ============================================
echo Importing User Data
echo ============================================
echo.

REM Configuration
set MONGODB_URI=mongodb://localhost:27017
set DATABASE_NAME=punchout

echo MongoDB URI: %MONGODB_URI%
echo Database:    %DATABASE_NAME%
echo.

REM Get current directory
set PROJECT_ROOT=%~dp0
cd /d "%PROJECT_ROOT%"

echo ============================================
echo Importing Users
echo ============================================
mongoimport --uri="%MONGODB_URI%" ^
  --db="%DATABASE_NAME%" ^
  --collection=users ^
  --file=mongodb-users-sample-data.json ^
  --jsonArray ^
  --drop

if %errorlevel% neq 0 (
    echo ERROR: Failed to import users
    pause
    exit /b 1
)
echo SUCCESS: Users imported
echo.

echo ============================================
echo Creating Indexes for Users Collection
echo ============================================

mongosh "%MONGODB_URI%/%DATABASE_NAME%" --quiet --eval "db.users.createIndex({ userId: 1 }, { unique: true }); print('  - users: userId (unique)');"
mongosh "%MONGODB_URI%/%DATABASE_NAME%" --quiet --eval "db.users.createIndex({ username: 1 }, { unique: true }); print('  - users: username (unique)');"
mongosh "%MONGODB_URI%/%DATABASE_NAME%" --quiet --eval "db.users.createIndex({ email: 1 }, { unique: true }); print('  - users: email (unique)');"
mongosh "%MONGODB_URI%/%DATABASE_NAME%" --quiet --eval "db.users.createIndex({ role: 1, status: 1 }); print('  - users: role + status');"
mongosh "%MONGODB_URI%/%DATABASE_NAME%" --quiet --eval "db.users.createIndex({ department: 1 }); print('  - users: department');"
mongosh "%MONGODB_URI%/%DATABASE_NAME%" --quiet --eval "db.users.createIndex({ status: 1 }); print('  - users: status');"
mongosh "%MONGODB_URI%/%DATABASE_NAME%" --quiet --eval "db.users.createIndex({ createdAt: -1 }); print('  - users: createdAt');"
mongosh "%MONGODB_URI%/%DATABASE_NAME%" --quiet --eval "db.users.createIndex({ lastLoginAt: -1 }); print('  - users: lastLoginAt');"

echo.
echo SUCCESS: Indexes created
echo.

echo ============================================
echo Data Import Summary
echo ============================================

REM Show summary using mongosh
mongosh "%MONGODB_URI%/%DATABASE_NAME%" --quiet --eval "print('Users imported: ' + db.users.countDocuments()); print(''); print('User breakdown:'); db.users.aggregate([{ $group: { _id: '$role', count: { $sum: 1 } } }, { $sort: { count: -1 } }]).forEach(doc => { print('  ' + doc._id + ': ' + doc.count + ' users'); }); print(''); print('Status breakdown:'); db.users.aggregate([{ $group: { _id: '$status', count: { $sum: 1 } } }, { $sort: { count: -1 } }]).forEach(doc => { print('  ' + doc._id + ': ' + doc.count + ' users'); });"

echo.
echo ============================================
echo Import Complete!
echo ============================================
echo.
echo API Endpoints:
echo   GET  http://localhost:8080/api/users                - List all users
echo   GET  http://localhost:8080/api/users/{id}           - Get user by ID
echo   GET  http://localhost:8080/api/users/role/ADMIN     - Get users by role
echo   GET  http://localhost:8080/api/users/status/ACTIVE  - Get users by status
echo   GET  http://localhost:8080/api/users/search?q=john  - Search users
echo   POST http://localhost:8080/api/users                - Create new user
echo   PUT  http://localhost:8080/api/users/{id}           - Update user
echo   DELETE http://localhost:8080/api/users/{id}         - Delete user
echo.
pause
