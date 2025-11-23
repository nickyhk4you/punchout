@echo off
REM ============================================
REM MongoDB Setup for Windows - PunchOut System
REM ============================================

echo.
echo ============================================
echo MongoDB Setup for Windows
echo ============================================
echo.

REM Check if MongoDB is installed
where mongod >nul 2>&1
if %errorlevel% neq 0 (
    echo ERROR: MongoDB is not installed or not in PATH
    echo.
    echo Please install MongoDB Community Edition from:
    echo https://www.mongodb.com/try/download/community
    echo.
    echo After installation, add MongoDB to your PATH:
    echo 1. Right-click "This PC" ^> Properties ^> Advanced System Settings
    echo 2. Click "Environment Variables"
    echo 3. Edit "Path" and add: C:\Program Files\MongoDB\Server\7.0\bin
    echo.
    pause
    exit /b 1
)

echo MongoDB is installed: 
mongod --version | findstr "db version"
echo.

REM Create MongoDB data directory
set MONGODB_DATA_DIR=%USERPROFILE%\mongodb\data
set MONGODB_LOG_DIR=%USERPROFILE%\mongodb\logs

echo Creating MongoDB directories...
if not exist "%MONGODB_DATA_DIR%" (
    mkdir "%MONGODB_DATA_DIR%"
    echo Created: %MONGODB_DATA_DIR%
) else (
    echo Directory already exists: %MONGODB_DATA_DIR%
)

if not exist "%MONGODB_LOG_DIR%" (
    mkdir "%MONGODB_LOG_DIR%"
    echo Created: %MONGODB_LOG_DIR%
) else (
    echo Directory already exists: %MONGODB_LOG_DIR%
)

echo.
echo ============================================
echo MongoDB directories created successfully
echo ============================================
echo Data directory: %MONGODB_DATA_DIR%
echo Log directory:  %MONGODB_LOG_DIR%
echo.

REM Create MongoDB configuration file
set MONGODB_CONFIG=%USERPROFILE%\mongodb\mongod.cfg

echo Creating MongoDB configuration file...
(
echo # MongoDB Configuration for PunchOut System
echo systemLog:
echo   destination: file
echo   path: %MONGODB_LOG_DIR%\mongod.log
echo   logAppend: true
echo storage:
echo   dbPath: %MONGODB_DATA_DIR%
echo   journal:
echo     enabled: true
echo net:
echo   port: 27017
echo   bindIp: 127.0.0.1
) > "%MONGODB_CONFIG%"

echo MongoDB configuration created: %MONGODB_CONFIG%
echo.

REM Check if MongoDB service is running
sc query MongoDB | findstr "RUNNING" >nul 2>&1
if %errorlevel% equ 0 (
    echo MongoDB service is already running
    echo.
    echo To restart MongoDB:
    echo   net stop MongoDB
    echo   net start MongoDB
    echo.
) else (
    echo ============================================
    echo Installing MongoDB as Windows Service
    echo ============================================
    echo.
    echo Please run the following command as Administrator:
    echo.
    echo mongod --config "%MONGODB_CONFIG%" --install
    echo net start MongoDB
    echo.
    echo Or run MongoDB manually:
    echo mongod --config "%MONGODB_CONFIG%"
    echo.
)

echo ============================================
echo Setup Complete!
echo ============================================
echo.
echo Next steps:
echo 1. Start MongoDB service (if not running)
echo 2. Run: import-data-windows.bat to import sample data
echo 3. Run: start-all-services-windows.bat to start PunchOut services
echo.
pause
