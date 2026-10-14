@echo off
REM Starts both the backend and the frontend in separate terminal windows.
start "Feedback Backend"  cmd /k "%~dp0backend\run.cmd"
start "Feedback Frontend" cmd /k "%~dp0frontend\run.cmd"
echo Backend and frontend are starting in separate windows.
echo.
echo   Frontend: http://localhost:5173
echo   Backend : http://localhost:8080
echo   Admin   : admin / Admin@12345
