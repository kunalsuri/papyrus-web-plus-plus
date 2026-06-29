@echo off
rem ===========================================================================
rem  Papyrus-Web - Stop the dev stack (double-click me)
rem  Wraps stop-dev.ps1: stops frontend, backend and the database container.
rem ===========================================================================
setlocal
title Papyrus-Web :: Stop
cd /d "%~dp0"

echo Stopping the Papyrus-Web dev stack...
echo.

rem Prefer PowerShell 7 (pwsh); fall back to Windows PowerShell.
set "PS=powershell"
where pwsh >nul 2>nul && set "PS=pwsh"

"%PS%" -NoProfile -ExecutionPolicy Bypass -File "%~dp0stop-dev.ps1"

echo.
echo (Database data is preserved. To wipe it, run:  stop-dev.ps1 -RemoveData)
echo.
pause
endlocal
