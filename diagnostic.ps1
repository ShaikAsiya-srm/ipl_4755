# IPL App Diagnostic Script
# Run this to check if everything is working

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "   IPL APP DIAGNOSTIC TOOL" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Check Java
Write-Host "1. Checking Java..." -ForegroundColor Yellow
try {
    $javaVersion = java -version 2>&1
    Write-Host "   ✅ Java is installed" -ForegroundColor Green
    Write-Host "   $javaVersion"
} catch {
    Write-Host "   ❌ Java NOT found! Install Java 11+" -ForegroundColor Red
}

Write-Host ""

# Check Maven
Write-Host "2. Checking Maven..." -ForegroundColor Yellow
try {
    $mvnVersion = mvn -version 2>&1 | Select-String "Apache Maven"
    Write-Host "   ✅ Maven is installed" -ForegroundColor Green
    mvn -version 2>&1 | Select-String "Maven"
} catch {
    Write-Host "   ❌ Maven NOT found! Install Maven 3.6+" -ForegroundColor Red
}

Write-Host ""

# Check Node.js
Write-Host "3. Checking Node.js..." -ForegroundColor Yellow
try {
    $nodeVersion = node -v
    $npmVersion = npm -v
    Write-Host "   ✅ Node.js: $nodeVersion" -ForegroundColor Green
    Write-Host "   ✅ npm: $npmVersion" -ForegroundColor Green
} catch {
    Write-Host "   ❌ Node.js NOT found! Install Node.js 14+" -ForegroundColor Red
}

Write-Host ""

# Check if backend is running
Write-Host "4. Checking Backend (port 8081)..." -ForegroundColor Yellow
try {
    $response = Invoke-WebRequest -Uri "http://localhost:8081/ipl-backend/api/matches" -UseBasicParsing -TimeoutSec 3
    if ($response.StatusCode -eq 200) {
        Write-Host "   ✅ Backend is running on port 8081" -ForegroundColor Green
        $content = $response.Content | ConvertFrom-Json 2>$null
        if ($content -and $content.Count -gt 0) {
            Write-Host "   ✅ API returned data: $($content.Count) matches found" -ForegroundColor Green
        } else {
            Write-Host "   ⚠️  API returned empty array" -ForegroundColor Yellow
        }
    }
} catch {
    Write-Host "   ❌ Backend NOT running on port 8081" -ForegroundColor Red
    Write-Host "   Start it with: cd IPL-BACKEND && mvn spring-boot:run"
}

Write-Host ""

# Check if frontend is running
Write-Host "5. Checking Frontend (port 3001)..." -ForegroundColor Yellow
try {
    $response = Invoke-WebRequest -Uri "http://localhost:3001" -UseBasicParsing -TimeoutSec 3
    if ($response.StatusCode -eq 200) {
        Write-Host "   ✅ Frontend is running on port 3001" -ForegroundColor Green
    }
} catch {
    Write-Host "   ❌ Frontend NOT running on port 3001" -ForegroundColor Red
    Write-Host "   Start it with: cd IPL-FRONTEND && npm start"
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "   SUMMARY" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "If both services are down, run:" -ForegroundColor White
Write-Host "  1. Terminal 1: cd IPL-BACKEND && mvn spring-boot:run" -ForegroundColor Gray
Write-Host "  2. Terminal 2: cd IPL-FRONTEND && npm start" -ForegroundColor Gray
Write-Host ""
Write-Host "Then open: http://localhost:3001/dashboard" -ForegroundColor Green
Write-Host ""

pause
