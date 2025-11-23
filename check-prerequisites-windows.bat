@echo off
setlocal enabledelayedexpansion

REM ============================================
REM Prerequisites Checker for Windows
REM ============================================

echo.
echo ============================================
echo Checking Prerequisites for PunchOut System
echo ============================================
echo.

set ALL_OK=1

REM Check Java
echo [1/4] Checking Java...
where java >nul 2>&1
if %errorlevel% neq 0 (
    echo [FAIL] Java is not installed or not in PATH
    echo        Download from: https://adoptium.net/
    set ALL_OK=0
) else (
    java -version 2>&1 | findstr "version" >nul
    if !errorlevel! equ 0 (
        for /f "tokens=3" %%a in ('java -version 2^>^&1 ^| findstr "version"') do set JAVA_VER=%%a
        echo [OK]   Java found: !JAVA_VER!
    )
)
echo.

REM Check Maven
echo [2/4] Checking Maven...
where mvn >nul 2>&1
if %errorlevel% neq 0 (
    echo [FAIL] Maven is not installed or not in PATH
    echo        Download from: https://maven.apache.org/download.cgi
    set ALL_OK=0
) else (
    for /f "tokens=3" %%a in ('mvn -version 2^>^&1 ^| findstr "Apache Maven"') do set MVN_VER=%%a
    echo [OK]   Maven found: !MVN_VER!
)
echo.

REM Check MongoDB
echo [3/4] Checking MongoDB...
where mongod >nul 2>&1
if %errorlevel% neq 0 (
    echo [FAIL] MongoDB is not installed or not in PATH
    echo        Download from: https://www.mongodb.com/try/download/community
    set ALL_OK=0
) else (
    for /f "tokens=3" %%a in ('mongod --version 2^>^&1 ^| findstr "db version"') do set MONGO_VER=%%a
    echo [OK]   MongoDB found: !MONGO_VER!
    
    REM Check if MongoDB service is running
    sc query MongoDB | findstr "RUNNING" >nul 2>&1
    if !errorlevel! equ 0 (
        echo [OK]   MongoDB service is running
    ) else (
        echo [WARN] MongoDB service is not running
        echo        Run: net start MongoDB
    )
)
echo.

REM Check Git (optional)
echo [4/4] Checking Git (optional)...
where git >nul 2>&1
if %errorlevel% neq 0 (
    echo [SKIP] Git is not installed (optional)
) else (
    for /f "tokens=3" %%a in ('git --version') do set GIT_VER=%%a
    echo [OK]   Git found: !GIT_VER!
)
echo.

REM Check ports
echo ============================================
echo Checking Required Ports
echo ============================================
echo.

set PORTS=9090 8080 8082 27017

for %%p in (%PORTS%) do (
    netstat -ano | findstr ":%%p " | findstr "LISTENING" >nul 2>&1
    if !errorlevel! equ 0 (
        echo [WARN] Port %%p is in use
    ) else (
        echo [OK]   Port %%p is available
    )
)

echo.
echo ============================================
echo Summary
echo ============================================
echo.

if %ALL_OK% equ 1 (
    echo [SUCCESS] All prerequisites are installed!
    echo.
    echo Next steps:
    echo   1. Run: setup-mongodb-windows.bat
    echo   2. Run: import-data-windows.bat
    echo   3. Run: start-all-services-windows.bat
) else (
    echo [FAILED] Some prerequisites are missing
    echo Please install the required software listed above
)

echo.
pause
