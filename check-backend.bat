@echo off
echo ========================================
echo IPL Backend Connectivity Check
echo ========================================
echo.

echo Checking if backend is responding on port 8081...
echo.

REM Try to fetch matches API
powershell -Command "try { $response = Invoke-WebRequest -Uri 'http://localhost:8081/ipl-backend/api/matches' -UseBasicParsing -TimeoutSec 5; Write-Host '✅ SUCCESS: Backend is running!' -ForegroundColor Green; Write-Host 'Response Status:' $response.StatusCode; Write-Host 'First 200 chars of response:'; $response.Content.Substring(0, [Math]::Min(200, $response.Content.Length)) } catch { Write-Host '❌ FAILED: Cannot reach backend on port 8081' -ForegroundColor Red; Write-Host 'Error:' $_.Exception.Message }"

echo.
echo ========================================
echo Next steps:
echo 1. If FAILED: Start backend first
echo    cd IPL-BACKEND
echo    mvn spring-boot:run
echo.
echo 2. If SUCCESS: Start frontend
echo    cd IPL-FRONTEND
echo    npm start
echo.
echo 3. Then open: http://localhost:3001/dashboard
echo ========================================
pause
