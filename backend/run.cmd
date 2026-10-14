@echo off
title Feedback Backend - http://localhost:8080
cd /d "%~dp0"

if not defined ADMIN_USERNAME set "ADMIN_USERNAME=admin"
if not defined ADMIN_EMAIL set "ADMIN_EMAIL=admin@example.com"
if not defined ADMIN_PASSWORD set "ADMIN_PASSWORD=Admin@12345"

echo ============================================
echo  Feedback Backend  (profile: dev / embedded H2)
echo  URL:   http://localhost:8080
echo  Admin: %ADMIN_USERNAME% / %ADMIN_PASSWORD%
echo ============================================
echo.

if exist "target\feedback-backend-1.0.0.jar" (
    java -jar "target\feedback-backend-1.0.0.jar"
) else (
    call mvnw.cmd spring-boot:run
)

echo.
echo Backend stopped. Press any key to close.
pause >nul
