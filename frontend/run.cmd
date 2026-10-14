@echo off
title Feedback Frontend - http://localhost:5173
cd /d "%~dp0"

echo ============================================
echo  Feedback Frontend  (Vite dev server)
echo  URL: http://localhost:5173
echo ============================================
echo.

if not exist "node_modules" (
    echo Installing dependencies...
    call npm install
)

call npm run dev

echo.
echo Frontend stopped. Press any key to close.
pause >nul
