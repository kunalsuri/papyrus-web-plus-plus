@echo off
rem ===========================================================================
rem  Papyrus-Web - First-time setup (double-click me)
rem  Wraps setup-dev.ps1: checks tools, configures GitHub auth, builds the app.
rem ===========================================================================
setlocal
title Papyrus-Web :: Setup
cd /d "%~dp0"

echo ===========================================================================
echo   Papyrus-Web - First-time setup
echo ===========================================================================
echo.
echo This will:
echo    - check your tools (Java 21, Maven, Node, npm, Docker)
echo    - set up GitHub Packages auth (you may be asked for a username and a
echo      token - the token stays hidden as you type)
echo    - build the application (this can take a while the first time)
echo.

rem Prefer PowerShell 7 (pwsh); fall back to Windows PowerShell.
set "PS=powershell"
where pwsh >nul 2>nul && set "PS=pwsh"

"%PS%" -NoProfile -ExecutionPolicy Bypass -File "%~dp0setup-dev.ps1" -ConfigureAuth
set "RC=%ERRORLEVEL%"

echo.
if "%RC%"=="0" (
  echo Setup completed successfully.  Next: double-click  run.bat
) else (
  echo Setup reported a problem ^(exit %RC%^). Review the messages above.
)
echo.
pause
endlocal
