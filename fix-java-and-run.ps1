# ============================================================
# FIX: Java Version Error - Auto Installer
# RIGHT-CLICK this file -> "Run with PowerShell as Administrator"
# ============================================================

Write-Host ""
Write-Host "============================================" -ForegroundColor Cyan
Write-Host "  Java Version Fix + Auto Run Script" -ForegroundColor Cyan  
Write-Host "============================================" -ForegroundColor Cyan
Write-Host ""

# ---- Check current Java version ----
Write-Host "Checking current Java version..." -ForegroundColor Yellow
try {
    $javaInfo = java -version 2>&1
    Write-Host "  Found: $javaInfo" -ForegroundColor Gray
} catch {
    Write-Host "  Java not found at all." -ForegroundColor Red
}

# ---- Download and install Java 17 ----
Write-Host ""
Write-Host "Downloading Java 17 (Temurin)..." -ForegroundColor Yellow

$javaUrl = "https://github.com/adoptium/temurin17-binaries/releases/download/jdk-17.0.10%2B7/OpenJDK17U-jdk_x64_windows_hotspot_17.0.10_7.msi"
$javaInstaller = "$env:USERPROFILE\Downloads\java17.msi"

try {
    Invoke-WebRequest -Uri $javaUrl -OutFile $javaInstaller -UseBasicParsing
    Write-Host "  Download complete. Installing silently..." -ForegroundColor Green
    Start-Process msiexec.exe -ArgumentList "/i `"$javaInstaller`" ADDLOCAL=FeatureMain,FeatureEnvironment,FeatureJarFileRunWith,FeatureJavaHome /quiet /norestart" -Wait
    Write-Host "  Java 17 installed!" -ForegroundColor Green
} catch {
    Write-Host ""
    Write-Host "  Auto download failed. Please install manually:" -ForegroundColor Red
    Write-Host "  1. Go to: https://adoptium.net/temurin/releases/?version=17" -ForegroundColor White
    Write-Host "  2. Select: Windows | x64 | JDK | .msi" -ForegroundColor White
    Write-Host "  3. Download and run the installer" -ForegroundColor White
    Write-Host "  4. After install, close this window and run fix-java-and-run.ps1 again" -ForegroundColor White
    Write-Host ""
    Start-Process "https://adoptium.net/temurin/releases/?version=17"
    Read-Host "Press Enter to exit"
    exit
}

# ---- Set JAVA_HOME to Java 17 ----
Write-Host ""
Write-Host "Setting JAVA_HOME to Java 17..." -ForegroundColor Yellow

# Find Java 17 installation
$possiblePaths = @(
    "C:\Program Files\Eclipse Adoptium\jdk-17.0.10.7-hotspot",
    "C:\Program Files\Eclipse Adoptium\jdk-17.0.9.9-hotspot",
    "C:\Program Files\Microsoft\jdk-17.0.10.7-hotspot",
    "C:\Program Files\Java\jdk-17",
    "C:\Program Files\Java\jdk-17.0.10"
)

# Also search dynamically
$searchResult = Get-ChildItem "C:\Program Files" -Filter "jdk-17*" -ErrorAction SilentlyContinue | Select-Object -First 1
if ($searchResult) { $possiblePaths += $searchResult.FullName }

$searchResult2 = Get-ChildItem "C:\Program Files\Eclipse Adoptium" -Filter "*17*" -ErrorAction SilentlyContinue | Select-Object -First 1
if ($searchResult2) { $possiblePaths += $searchResult2.FullName }

$javaHome = $null
foreach ($path in $possiblePaths) {
    if (Test-Path "$path\bin\java.exe") {
        $javaHome = $path
        break
    }
}

if ($null -eq $javaHome) {
    # Last resort: find any java 17
    $found = Get-ChildItem "C:\Program Files" -Recurse -Filter "java.exe" -ErrorAction SilentlyContinue | 
             Where-Object { $_.FullName -like "*17*" } | Select-Object -First 1
    if ($found) { $javaHome = $found.Directory.Parent.FullName }
}

if ($javaHome) {
    [System.Environment]::SetEnvironmentVariable("JAVA_HOME", $javaHome, "Machine")
    $env:JAVA_HOME = $javaHome
    
    # Add to PATH
    $currentPath = [System.Environment]::GetEnvironmentVariable("PATH", "Machine")
    $javabin = "$javaHome\bin"
    if ($currentPath -notlike "*$javabin*") {
        [System.Environment]::SetEnvironmentVariable("PATH", "$javabin;$currentPath", "Machine")
    }
    $env:PATH = "$javabin;$env:PATH"
    
    Write-Host "  JAVA_HOME set to: $javaHome" -ForegroundColor Green
} else {
    Write-Host "  Could not find Java 17 automatically." -ForegroundColor Red
    Write-Host "  Please set JAVA_HOME manually (see below)." -ForegroundColor Red
}

# ---- Verify ----
Write-Host ""
Write-Host "Verifying Java version..." -ForegroundColor Yellow
$ver = java -version 2>&1
Write-Host "  $ver" -ForegroundColor White

# ---- Now build the project ----
Write-Host ""
Write-Host "============================================" -ForegroundColor Green
Write-Host "  Java fixed! Now building the project..." -ForegroundColor Green
Write-Host "============================================" -ForegroundColor Green
Write-Host ""

# Navigate to project folder
$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
Set-Location $scriptDir

Write-Host "Building from: $scriptDir" -ForegroundColor Gray
Write-Host ""

mvn clean install -DskipTests

if ($LASTEXITCODE -ne 0) {
    Write-Host ""
    Write-Host "Build failed. Try the manual steps below." -ForegroundColor Red
    Read-Host "Press Enter to exit"
    exit
}

Write-Host ""
Write-Host "============================================" -ForegroundColor Green
Write-Host "  SUCCESS! Starting API..." -ForegroundColor Green
Write-Host "============================================" -ForegroundColor Green
Write-Host ""
Write-Host "  API will be at: http://localhost:8080/api" -ForegroundColor Cyan
Write-Host "  Keep this window OPEN." -ForegroundColor Yellow
Write-Host ""

mvn spring-boot:run
