@echo off
:: ============================================================
:: Personal Finance Manager - Simple Run Script
:: Double-click this file to start the API
:: ============================================================

title Personal Finance Manager API

echo.
echo ==========================================
echo   Personal Finance Manager - Starting...
echo ==========================================
echo.

:: Check if Java is installed
java -version >nul 2>&1
if %errorlevel% neq 0 (
    echo ERROR: Java is not installed!
    echo.
    echo Please install Java 17 from: https://adoptium.net
    echo Download the Windows .msi installer and install it.
    echo Then run this file again.
    echo.
    pause
    exit /b 1
)

:: Check if Maven is installed
mvn --version >nul 2>&1
if %errorlevel% neq 0 (
    echo ERROR: Maven is not installed!
    echo.
    echo Please run 'install-maven-manually.ps1' first.
    echo Right-click it and select 'Run with PowerShell as Administrator'
    echo Then run this file again.
    echo.
    pause
    exit /b 1
)

:: Check if pom.xml exists in current directory
if not exist "pom.xml" (
    echo ERROR: pom.xml not found!
    echo.
    echo Make sure you put this .bat file inside the
    echo 'personal-finance-manager' folder (where pom.xml is).
    echo.
    pause
    exit /b 1
)

echo Building project (first time takes 2-5 minutes)...
echo.
call mvn clean install -DskipTests

if %errorlevel% neq 0 (
    echo.
    echo BUILD FAILED! Check the errors above.
    pause
    exit /b 1
)

echo.
echo ==========================================
echo   BUILD SUCCESS! Starting API Server...
echo ==========================================
echo.
echo   API is running at: http://localhost:8080/api
echo.
echo   Keep this window OPEN.
echo   Press Ctrl+C to stop the server.
echo.

call mvn spring-boot:run

pause
