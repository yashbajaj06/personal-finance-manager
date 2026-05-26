@echo off
title Personal Finance Manager API

echo Killing old Java processes...
taskkill /F /IM java.exe >nul 2>&1

echo Going to project folder...
cd /d "C:\Users\bajaj\personal-finance-manager"

echo You are now in: %CD%
echo.

echo Building...
call mvn clean install -DskipTests

if %errorlevel% neq 0 (
    echo BUILD FAILED!
    pause
    exit /b 1
)

echo.
echo ================================
echo  SUCCESS! API starting...
echo  Open Postman and use:
echo  http://localhost:8080/api
echo ================================
echo.

call mvn spring-boot:run
pause
