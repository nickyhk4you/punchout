@echo off
setlocal enabledelayedexpansion

REM ============================================
REM Start All PunchOut Services - Windows
REM ============================================

echo.
echo ============================================
echo Starting All PunchOut Backend Services
echo ============================================
echo.

REM Get current directory
set PROJECT_ROOT=%~dp0
cd /d "%PROJECT_ROOT%"

REM Check if Maven is installed
where mvn >nul 2>&1
if %errorlevel% neq 0 (
    echo ERROR: Maven is not installed or not in PATH
    echo Please install Maven from: https://maven.apache.org/download.cgi
    pause
    exit /b 1
)

REM Check if Java is installed
where java >nul 2>&1
if %errorlevel% neq 0 (
    echo ERROR: Java is not installed or not in PATH
    echo Please install Java 17 from: https://adoptium.net/
    pause
    exit /b 1
)

echo Java version:
java -version
echo.
echo Maven version:
mvn -version | findstr "Apache Maven"
echo.

REM Service configuration
set SERVICES[0]=punchout-gateway:9090:Gateway Service
set SERVICES[1]=punchout-ui-backend:8080:UI Backend Service
set SERVICES[2]=punchout-mock-service:8082:Mock Service

REM Build common module first
echo ============================================
echo Building Common Module...
echo ============================================
call mvn clean install -pl punchout-common -am -DskipTests
if %errorlevel% neq 0 (
    echo ERROR: Failed to build common module
    pause
    exit /b 1
)
echo.

REM Function to check if port is in use
REM Returns 0 if port is free, 1 if in use
:CheckPort
netstat -ano | findstr ":%~1 " | findstr "LISTENING" >nul 2>&1
exit /b %errorlevel%

REM Function to kill process on port
:KillPort
set PORT=%~1
for /f "tokens=5" %%a in ('netstat -ano ^| findstr ":%PORT% " ^| findstr "LISTENING"') do (
    echo    Stopping existing process on port %PORT% (PID: %%a^)...
    taskkill /F /PID %%a >nul 2>&1
    timeout /t 2 /nobreak >nul
)
exit /b 0

REM Start each service
for /L %%i in (0,1,2) do (
    set SERVICE_CONFIG=!SERVICES[%%i]!
    
    REM Parse service configuration
    for /f "tokens=1,2,3 delims=:" %%a in ("!SERVICE_CONFIG!") do (
        set SERVICE_DIR=%%a
        set PORT=%%b
        set SERVICE_NAME=%%c
        
        echo ============================================
        echo Starting !SERVICE_NAME!...
        echo ============================================
        
        REM Kill existing process on port
        call :KillPort !PORT!
        
        REM Build service
        echo Building !SERVICE_NAME!...
        call mvn clean compile -pl !SERVICE_DIR! -am -DskipTests
        if !errorlevel! neq 0 (
            echo ERROR: Build failed for !SERVICE_NAME!
            echo.
            set /p CONTINUE="Continue with other services? (y/n): "
            if /i not "!CONTINUE!"=="y" exit /b 1
        ) else (
            REM Start service in background
            echo Starting !SERVICE_NAME! on port !PORT!...
            cd /d "%PROJECT_ROOT%\!SERVICE_DIR!"
            start "!SERVICE_NAME!" /MIN cmd /c "mvn spring-boot:run > %TEMP%\!SERVICE_DIR!.log 2>&1"
            cd /d "%PROJECT_ROOT%"
            
            REM Wait for service to start
            echo Waiting for !SERVICE_NAME! to start...
            set WAIT_COUNT=0
            :WaitLoop
            timeout /t 1 /nobreak >nul
            call :CheckPort !PORT!
            if !errorlevel! equ 0 (
                echo SUCCESS: !SERVICE_NAME! started on port !PORT!
                echo.
                goto :NextService
            )
            set /a WAIT_COUNT+=1
            if !WAIT_COUNT! lss 30 goto :WaitLoop
            
            echo WARNING: !SERVICE_NAME! may not have started. Check logs: %TEMP%\!SERVICE_DIR!.log
            echo.
        )
        :NextService
    )
)

echo.
echo ============================================
echo Service Status Summary
echo ============================================

REM Check each service status
call :CheckPort 9090
if %errorlevel% equ 0 (
    echo [OK] Gateway Service      - http://localhost:9090
) else (
    echo [  ] Gateway Service      - Not running on port 9090
)

call :CheckPort 8080
if %errorlevel% equ 0 (
    echo [OK] UI Backend Service   - http://localhost:8080
) else (
    echo [  ] UI Backend Service   - Not running on port 8080
)

call :CheckPort 8082
if %errorlevel% equ 0 (
    echo [OK] Mock Service         - http://localhost:8082
) else (
    echo [  ] Mock Service         - Not running on port 8082
)

echo ============================================
echo.
echo Log files location: %TEMP%
echo   Gateway:    %TEMP%\punchout-gateway.log
echo   UI Backend: %TEMP%\punchout-ui-backend.log
echo   Mock:       %TEMP%\punchout-mock-service.log
echo.
echo To stop all services: stop-all-services-windows.bat
echo To view running services: tasklist | findstr "java"
echo.
echo API Endpoints:
echo   Gateway:    http://localhost:9090/actuator/health
echo   UI Backend: http://localhost:8080/api/health
echo   Mock:       http://localhost:8082/actuator/health
echo.
pause
