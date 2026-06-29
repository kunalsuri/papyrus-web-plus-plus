<#
.SYNOPSIS
    Start the full dev stack: database, backend and frontend.
.DESCRIPTION
    Orchestrates startup in order:
      1. Database (inline; waits until ready).
      2. Backend - launched in its own terminal window; this script then polls
         port 8080 until it answers.
      3. Frontend - launched in its own terminal window; polls port 5173.
    Backend and frontend keep running in their windows so you see live logs.
    Stop everything with  .\stop-dev.ps1.
.PARAMETER Build
    Run build-all.ps1 first (full rebuild of UI + backend JAR).
.PARAMETER SkipFrontend
    Start only the database and backend (e.g. when using `npm start` yourself).
.PARAMETER BackendTimeoutSec
    How long to wait for the backend to answer on 8080 (default 240).
#>
# Copyright (c) 2026 Kunal Suri. SPDX-License-Identifier: EPL-2.0
[CmdletBinding()]
param(
    [switch]$Build,
    [switch]$SkipFrontend,
    [int]$BackendTimeoutSec = 240
)

$ErrorActionPreference = 'Stop'
. "$PSScriptRoot\shared_core_lib.ps1"

# Launch a script in a separate window: a Windows Terminal tab if available,
# otherwise a standalone PowerShell window. -NoExit keeps logs visible.
function Start-InNewWindow {
    param([Parameter(Mandatory)][string]$Title, [Parameter(Mandatory)][string]$ScriptPath, [string[]]$ScriptArgs = @())
    $shell = Get-PreferredShell
    $shellArgs = @('-NoExit', '-NoProfile', '-ExecutionPolicy', 'Bypass', '-File', $ScriptPath) + $ScriptArgs
    if (Test-CommandExists 'wt') {
        try {
            wt.exe -w 0 new-tab --title $Title $shell @shellArgs
            return
        } catch {
            Write-Warn "Windows Terminal launch failed; falling back to a plain window. ($($_.Exception.Message))"
        }
    }
    Start-Process -FilePath $shell -ArgumentList $shellArgs | Out-Null
}

Write-Section 'Papyrus-Web :: Start dev stack'

if ($Build) {
    & (Join-Path $PwppScriptsDir 'build-all.ps1')
    if ($LASTEXITCODE -ne 0) { Write-Err 'Build failed - aborting.'; exit 1 }
}

# --- 1. Database (inline) ------------------------------------------------
& (Join-Path $PwppScriptsDir 'start-db.ps1')
if ($LASTEXITCODE -ne 0) { Write-Err 'Database failed to start - aborting.'; exit 1 }

# Make sure a JAR exists before opening a window that would just error out.
if (-not (Find-BackendJar)) {
    Write-Err 'No backend JAR found. Run  .\build-all.ps1  (or  .\start-dev.ps1 -Build).'
    exit 1
}

# --- 2. Backend (new window) --------------------------------------------
Write-Step 'Launching backend in a new window...'
Start-InNewWindow -Title 'PWPP Backend' -ScriptPath (Join-Path $PwppScriptsDir 'start-backend.ps1')
if (-not (Wait-TcpPort -Port $PwppBackendPort -TimeoutSec $BackendTimeoutSec -Name 'backend')) {
    Write-Warn 'Backend not ready yet; check its window. Continuing.'
}

# --- 3. Frontend (new window) -------------------------------------------
if (-not $SkipFrontend) {
    Write-Step 'Launching frontend in a new window...'
    Start-InNewWindow -Title 'PWPP Frontend' -ScriptPath (Join-Path $PwppScriptsDir 'start-frontend.ps1')
    Wait-TcpPort -Port $PwppFrontendPort -TimeoutSec 120 -Name 'frontend' | Out-Null
}

# --- Summary -------------------------------------------------------------
Write-Section 'Dev stack running'
Write-Host '  Frontend    ' -NoNewline; Write-Host "http://localhost:$PwppFrontendPort" -ForegroundColor Green
Write-Host '  Backend     ' -NoNewline; Write-Host "http://localhost:$PwppBackendPort" -ForegroundColor Green
Write-Host '  GraphQL     ' -NoNewline; Write-Host "http://localhost:$PwppBackendPort/api/graphql" -ForegroundColor Green
Write-Host '  PostgreSQL  ' -NoNewline; Write-Host "localhost:$PwppDbPort  (db $PwppDbName)" -ForegroundColor Green
Write-Host ''
Write-Info 'Stop everything with:  .\stop-dev.ps1'
exit 0
