@echo off
setlocal enabledelayedexpansion

REM ============================================
REM Stop All PunchOut Services - Windows
REM ============================================

echo.
echo ============================================
echo Stopping All PunchOut Services
echo ============================================
echo.

REM Service ports
set PORTS=9090 8080 8082

REM Stop services by port
for %%p in (%PORTS%) do (
    echo Checking port %%p...
    
    for /f "tokens=5" %%a in ('netstat -ano ^| findstr ":%%p " ^| findstr "LISTENING"') do (
        echo Stopping process on port %%p (PID: %%a^)...
        taskkill /F /PID %%a >nul 2>&1
        if !errorlevel! equ 0 (
            echo SUCCESS: Stopped service on port %%p
        ) else (
            echo WARNING: Could not stop service on port %%p
        )
    )
)

echo.
echo ============================================
echo Verifying Services Stopped
echo ============================================

REM Check if ports are free
set ALL_STOPPED=1
for %%p in (%PORTS%) do (
    netstat -ano | findstr ":%%p " | findstr "LISTENING" >nul 2>&1
    if !errorlevel! equ 0 (
        echo [  ] Port %%p still in use
        set ALL_STOPPED=0
    ) else (
        echo [OK] Port %%p is free
    )
)

echo.
if %ALL_STOPPED% equ 1 (
    echo All services stopped successfully!
) else (
    echo WARNING: Some services may still be running
    echo Run: tasklist ^| findstr "java" to check
)

echo.
echo To start services again: start-all-services-windows.bat
echo.
pause
