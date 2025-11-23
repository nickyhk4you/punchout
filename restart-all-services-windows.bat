@echo off

REM ============================================
REM Restart All PunchOut Services - Windows
REM ============================================

echo.
echo ============================================
echo Restarting All PunchOut Services
echo ============================================
echo.

REM Stop all services first
echo Step 1: Stopping services...
call stop-all-services-windows.bat

echo.
echo Waiting 3 seconds before restart...
timeout /t 3 /nobreak >nul

REM Start all services
echo.
echo Step 2: Starting services...
call start-all-services-windows.bat
