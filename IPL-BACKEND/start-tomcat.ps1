# Download and run Apache Tomcat 10 with IPL Backend WAR

$ErrorActionPreference = "Stop"
$scriptDir = (Get-Item -Path $PSScriptRoot).FullName
Set-Location $scriptDir

Write-Host "IPL Backend - Tomcat Setup Script" -ForegroundColor Green
Write-Host "=================================" -ForegroundColor Green
Write-Host "Working Directory: $scriptDir" -ForegroundColor Gray
Write-Host ""

# Check if WAR file exists
$warFile = "$scriptDir\target\ipl-backend.war"
if (-not (Test-Path $warFile)) {
    Write-Host "ERROR: WAR file not found at: $warFile" -ForegroundColor Red
    Write-Host "Build the project first:" -ForegroundColor Red
    Write-Host "   cd $scriptDir" -ForegroundColor Yellow
    Write-Host "   mvn clean package -DskipTests" -ForegroundColor Yellow
    exit 1
}

$tomcatVersion = "10.1.15"
$tomcatHome = "$scriptDir\apache-tomcat-$tomcatVersion"
$tomcatZip = "$scriptDir\tomcat-$tomcatVersion.zip"
$tomcatUrl = "https://archive.apache.org/dist/tomcat/tomcat-10/v$tomcatVersion/bin/apache-tomcat-$tomcatVersion-windows-x64.zip"

# Download Tomcat if not exists
if (-not (Test-Path $tomcatHome)) {
    Write-Host "Downloading Apache Tomcat $tomcatVersion..." -ForegroundColor Yellow
    [Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12
    (New-Object System.Net.WebClient).DownloadFile($tomcatUrl, $tomcatZip)
    
    Write-Host "Extracting Tomcat..." -ForegroundColor Yellow
    Expand-Archive -Path $tomcatZip -DestinationPath $scriptDir -Force
    Remove-Item $tomcatZip
}

# Copy WAR file
Write-Host "Deploying WAR file..." -ForegroundColor Yellow
Copy-Item $warFile "$tomcatHome\webapps\" -Force

# Start Tomcat
Write-Host ""
Write-Host "Setup Complete!" -ForegroundColor Green
Write-Host ""
Write-Host "Starting Apache Tomcat on port 8080..." -ForegroundColor Green
Write-Host "Backend URL: http://localhost:8080/ipl-backend" -ForegroundColor Cyan
Write-Host ""

$env:CATALINA_HOME = $tomcatHome
&"$tomcatHome\bin\catalina.bat" run
