@echo off
title MedBot Server
cd /d "%~dp0"
echo Compiling MedBot...
javac Server.java
if errorlevel 1 (
  echo Compile failed. Check Java is installed and Server.java is in this folder.
  pause
  exit /b
)
start "" cmd /c "timeout /t 3 /nobreak >nul & start http://localhost:8080"
java Server
pause