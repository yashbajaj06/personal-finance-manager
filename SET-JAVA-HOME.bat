@echo off
:: ============================================================
:: Run this as ADMINISTRATOR to fix JAVA_HOME
:: Right-click -> "Run as administrator"
:: ============================================================

title Fix JAVA_HOME - Run as Administrator

echo.
echo Searching for Java 17 on your computer...
echo.

set JAVA17_PATH=

:: Check common install locations
for /d %%d in ("C:\Program Files\Eclipse Adoptium\jdk-17*") do set JAVA17_PATH=%%d
for /d %%d in ("C:\Program Files\Microsoft\jdk-17*") do set JAVA17_PATH=%%d
for /d %%d in ("C:\Program Files\Java\jdk-17*") do set JAVA17_PATH=%%d
for /d %%d in ("C:\Program Files\Java\jdk17*") do set JAVA17_PATH=%%d

if not defined JAVA17_PATH (
    echo Java 17 not found in common locations.
    echo.
    echo Please install it first:
    echo 1. Go to https://adoptium.net/temurin/releases/?version=17
    echo 2. Download Windows x64 .msi
    echo 3. Install it, then run this file again
    echo.
    pause
    exit /b 1
)

echo Found Java 17 at: %JAVA17_PATH%
echo.

:: Set JAVA_HOME permanently
setx JAVA_HOME "%JAVA17_PATH%" /M
echo Set JAVA_HOME = %JAVA17_PATH%

:: Add to PATH
setx PATH "%JAVA17_PATH%\bin;%PATH%" /M
echo Added to PATH.

echo.
echo ============================================
echo  Done! Now:
echo  1. Close ALL PowerShell/CMD windows
echo  2. Open a NEW PowerShell window
echo  3. Run: mvn clean install -DskipTests
echo  4. Run: mvn spring-boot:run
echo ============================================
echo.
pause
