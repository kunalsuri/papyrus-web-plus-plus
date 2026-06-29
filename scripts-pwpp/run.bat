@echo off
rem ===========================================================================
rem  Papyrus-Web - Start the dev stack (double-click me, after setup.bat)
rem  Wraps start-dev.ps1: starts database, backend and frontend.
rem ===========================================================================
setlocal
title Papyrus-Web :: Run
cd /d "%~dp0"

echo ===========================================================================
echo   Papyrus-Web - Start dev stack
echo ===========================================================================
echo.
echo Starting database, backend and frontend.
echo The backend and frontend each open in their own window.
echo.

rem Prefer PowerShell 7 (pwsh); fall back to Windows PowerShell.
set "PS=powershell"
where pwsh >nul 2>nul && set "PS=pwsh"

"%PS%" -NoProfile -ExecutionPolicy Bypass -File "%~dp0start-dev.ps1"
set "RC=%ERRORLEVEL%"

echo.
if "%RC%"=="0" (
  echo Stack launched.  Open  http://localhost:5173  in your browser.
  echo To stop everything, double-click  stop.bat
) else (
  echo Startup reported a problem ^(exit %RC%^). If it says no JAR was found,
  echo run  setup.bat  first.
)
echo.
pause
endlocal
