@echo off
REM Download and Setup Tomcat 10
setlocal enabledelayedexpansion

echo Downloading Tomcat 10...
powershell -Command "(New-Object System.Net.ServicePointManager).SecurityProtocol = [System.Net.ServicePointManager]::SecurityProtocol -bor [System.Net.SecurityProtocolType]::Tls12; (New-Object System.Net.WebClient).DownloadFile('https://archive.apache.org/dist/tomcat/tomcat-10/v10.1.15/bin/apache-tomcat-10.1.15-windows-x64.zip', 'tomcat.zip')"

echo Extracting Tomcat...
powershell -Command "Expand-Archive -Path 'tomcat.zip' -DestinationPath '.' -Force"

echo Copying WAR file to Tomcat webapps...
copy target\ipl-backend.war apache-tomcat-10.1.15\webapps\

echo.
echo ✅ Setup complete!
echo.
echo To start Tomcat, run:
echo   apache-tomcat-10.1.15\bin\startup.bat
echo.
echo Then open: http://localhost:8080/ipl-backend
echo.
pause
