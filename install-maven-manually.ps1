# ============================================================
# STEP 1 ONLY: Install Maven (run this if mvn command missing)
# Run as Administrator: Right-click -> "Run as Administrator"
# ============================================================

Write-Host "Installing Maven for Windows..." -ForegroundColor Cyan

$mavenVersion = "3.9.6"
$mavenZip = "$env:USERPROFILE\Downloads\maven.zip"
$mavenDir = "C:\maven"

# Download
Write-Host "Downloading Maven..." -ForegroundColor Yellow
Invoke-WebRequest -Uri "https://downloads.apache.org/maven/maven-3/$mavenVersion/binaries/apache-maven-$mavenVersion-bin.zip" `
    -OutFile $mavenZip -UseBasicParsing

# Extract
Write-Host "Extracting..." -ForegroundColor Yellow
if (Test-Path $mavenDir) { Remove-Item $mavenDir -Recurse -Force }
Expand-Archive -Path $mavenZip -DestinationPath "C:\" -Force
Rename-Item "C:\apache-maven-$mavenVersion" $mavenDir -Force

# Add to PATH permanently
Write-Host "Adding to PATH..." -ForegroundColor Yellow
$mavenBin = "C:\maven\bin"
$syspath = [System.Environment]::GetEnvironmentVariable("PATH","Machine")
if ($syspath -notlike "*$mavenBin*") {
    [System.Environment]::SetEnvironmentVariable("PATH","$syspath;$mavenBin","Machine")
}
$env:PATH = "$env:PATH;$mavenBin"

# Verify
Write-Host ""
mvn --version
Write-Host ""
Write-Host "Maven installed! Close this window and open a NEW PowerShell." -ForegroundColor Green
Read-Host "Press Enter to close"
