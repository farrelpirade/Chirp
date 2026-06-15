Write-Host "==========================================" -ForegroundColor Cyan
Write-Host "      [ CHIRP PROJECT LAUNCHER ]" -ForegroundColor Cyan
Write-Host "==========================================" -ForegroundColor Cyan
Write-Host ""

# Set Java Home path for Java 25
$env:JAVA_HOME = "C:\Program Files\Java\jdk-25"

# 1. Launch Spring Boot Backend
Write-Host "[1/2] Launching Java Spring Boot Backend..." -ForegroundColor Green
Start-Process cmd -ArgumentList '/k "echo Starting Spring Boot backend... && cd backend && mvnw.cmd spring-boot:run"'

# 2. Check and launch Nuxt Frontend
Write-Host "[2/2] Launching Nuxt Frontend..." -ForegroundColor Green
if (-not (Test-Path "frontend\node_modules")) {
    Write-Host "node_modules not found. Installing dependencies first..." -ForegroundColor Yellow
    Push-Location frontend
    npm install
    Pop-Location
}

Start-Process cmd -ArgumentList '/k "echo Starting Nuxt frontend... && cd frontend && npm run dev"'

Write-Host ""
Write-Host "==========================================" -ForegroundColor Cyan
Write-Host " Chirp Backend and Frontend are starting!" -ForegroundColor Cyan
Write-Host "==========================================" -ForegroundColor Cyan
Write-Host ""
Read-Host "Press Enter to exit..."
