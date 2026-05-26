# ============================================================
# Personal Finance Manager - Auto Setup Script for Windows
# Just RIGHT-CLICK this file and "Run with PowerShell"
# ============================================================

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  Personal Finance Manager - Auto Setup" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# ----------------------------
# STEP 1: Check Java
# ----------------------------
Write-Host "[1/4] Checking Java..." -ForegroundColor Yellow

$javaInstalled = $false
try {
    $javaVersion = java -version 2>&1
    Write-Host "  Java found: $javaVersion" -ForegroundColor Green
    $javaInstalled = $true
} catch {
    Write-Host "  Java NOT found." -ForegroundColor Red
}

if (-not $javaInstalled) {
    Write-Host ""
    Write-Host "  Java is not installed. Opening download page..." -ForegroundColor Red
    Write-Host "  Please download Java 17 from: https://adoptium.net" -ForegroundColor White
    Start-Process "https://adoptium.net"
    Write-Host ""
    Write-Host "  INSTRUCTIONS:" -ForegroundColor Yellow
    Write-Host "  1. Download the .msi installer (Windows x64)" -ForegroundColor White
    Write-Host "  2. Run the installer, keep all default options" -ForegroundColor White
    Write-Host "  3. Make sure 'Add to PATH' option is CHECKED during install" -ForegroundColor White
    Write-Host "  4. After install, close PowerShell and run this script again" -ForegroundColor White
    Write-Host ""
    Read-Host "Press Enter to exit"
    exit
}

# ----------------------------
# STEP 2: Download & Setup Maven
# ----------------------------
Write-Host ""
Write-Host "[2/4] Checking Maven..." -ForegroundColor Yellow

$mvnInstalled = $false
try {
    $mvnVersion = mvn --version 2>&1
    Write-Host "  Maven found: $mvnVersion" -ForegroundColor Green
    $mvnInstalled = $true
} catch {
    Write-Host "  Maven NOT found. Installing automatically..." -ForegroundColor Red
}

if (-not $mvnInstalled) {
    $mavenVersion = "3.9.6"
    $mavenUrl = "https://downloads.apache.org/maven/maven-3/$mavenVersion/binaries/apache-maven-$mavenVersion-bin.zip"
    $mavenZip = "$env:USERPROFILE\Downloads\apache-maven-$mavenVersion-bin.zip"
    $mavenDir = "C:\maven"

    Write-Host "  Downloading Maven $mavenVersion..." -ForegroundColor Yellow
    try {
        Invoke-WebRequest -Uri $mavenUrl -OutFile $mavenZip -UseBasicParsing
        Write-Host "  Download complete." -ForegroundColor Green
    } catch {
        # Try mirror if main fails
        Write-Host "  Trying mirror..." -ForegroundColor Yellow
        $mavenUrl2 = "https://archive.apache.org/dist/maven/maven-3/$mavenVersion/binaries/apache-maven-$mavenVersion-bin.zip"
        Invoke-WebRequest -Uri $mavenUrl2 -OutFile $mavenZip -UseBasicParsing
    }

    Write-Host "  Extracting Maven to C:\maven ..." -ForegroundColor Yellow
    if (Test-Path $mavenDir) { Remove-Item $mavenDir -Recurse -Force }
    Expand-Archive -Path $mavenZip -DestinationPath "C:\" -Force
    Rename-Item "C:\apache-maven-$mavenVersion" $mavenDir -Force

    Write-Host "  Adding Maven to system PATH..." -ForegroundColor Yellow
    $mavenBin = "C:\maven\bin"
    $currentPath = [System.Environment]::GetEnvironmentVariable("PATH", "Machine")
    if ($currentPath -notlike "*$mavenBin*") {
        [System.Environment]::SetEnvironmentVariable("PATH", "$currentPath;$mavenBin", "Machine")
    }
    $env:PATH = "$env:PATH;$mavenBin"

    Write-Host "  Maven installed successfully!" -ForegroundColor Green
}

# ----------------------------
# STEP 3: Find the project folder
# ----------------------------
Write-Host ""
Write-Host "[3/4] Locating project..." -ForegroundColor Yellow

# Try to find pom.xml in current or parent directories
$projectPath = $null

# Check current directory first
if (Test-Path ".\pom.xml") {
    $projectPath = (Get-Location).Path
}
# Check if we're inside the project
elseif (Test-Path "..\pom.xml") {
    $projectPath = (Get-Item "..").FullName
}
# Search common locations
else {
    $searchPaths = @(
        "$env:USERPROFILE\Desktop",
        "$env:USERPROFILE\Downloads",
        "$env:USERPROFILE\Documents",
        "D:\Company Github Project\UNZIPPED"
    )
    foreach ($path in $searchPaths) {
        $found = Get-ChildItem -Path $path -Filter "pom.xml" -Recurse -ErrorAction SilentlyContinue | Select-Object -First 1
        if ($found) {
            $projectPath = $found.DirectoryName
            break
        }
    }
}

if ($null -eq $projectPath) {
    Write-Host "  Could not find project automatically." -ForegroundColor Red
    Write-Host ""
    $projectPath = Read-Host "  Please paste the full path to personal-finance-manager folder"
}

Write-Host "  Project found at: $projectPath" -ForegroundColor Green
Set-Location $projectPath

# ----------------------------
# STEP 4: Build & Run
# ----------------------------
Write-Host ""
Write-Host "[4/4] Building project..." -ForegroundColor Yellow
Write-Host "  This will take 2-5 minutes on first run (downloading libraries)" -ForegroundColor Gray
Write-Host ""

# Run Maven build
& mvn clean install -DskipTests

if ($LASTEXITCODE -ne 0) {
    Write-Host ""
    Write-Host "BUILD FAILED. Check the errors above." -ForegroundColor Red
    Read-Host "Press Enter to exit"
    exit
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Green
Write-Host "  BUILD SUCCESSFUL!" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Green
Write-Host ""
Write-Host "Starting the API server..." -ForegroundColor Yellow
Write-Host ""
Write-Host "  Once started, your API will be at:" -ForegroundColor White
Write-Host "  http://localhost:8080/api" -ForegroundColor Cyan
Write-Host ""
Write-Host "  Keep this window OPEN while using the API." -ForegroundColor Yellow
Write-Host "  Press Ctrl+C to stop the server." -ForegroundColor Gray
Write-Host ""

& mvn spring-boot:run
