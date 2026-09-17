@echo off
REM CampusOS launcher — double-click this (not the jar) so errors stay visible.
cd /d "%~dp0"
java -version 2>&1 | find "21" >nul
if errorlevel 1 (
  echo [CampusOS] JDK 21 required. Installed java:
  java -version
  echo Download Temurin 21: https://adoptium.net/temurin/releases/?version=21
  pause
  exit /b 1
)
echo [CampusOS] starting...
java -jar target\campusos-0.1.0.jar
if errorlevel 1 (
  echo.
  echo [CampusOS] exited with an error (see above).
  pause
)
