<#
.SYNOPSIS
    Run the Papyrus-Web Spring Boot backend (the fat JAR) on port 8080.
.DESCRIPTION
    Locates backend/papyrus-web/target/papyrus-web-*.jar and launches it with
    `java -jar`. No datasource arguments are needed: application.properties
    already points at the dev database created by start-db.ps1.

    By default the backend runs in the FOREGROUND so you see live logs (Ctrl+C
    stops it). Use -Background for a detached process with file logging, which
    is what CI and start-dev.ps1 use to sequence startup.
.PARAMETER Build
    Compile the backend (mvn -DskipTests clean package) before running. Note
    this produces a JAR WITHOUT the bundled UI; for the all-in-one JAR use
    build-all.ps1. In dev the UI is served separately by start-frontend.ps1.
.PARAMETER Background
    Launch java detached, redirect logs to scripts-pwpp/.logs, poll the port,
    then return (exit 0 when ready, 1 on timeout).
.PARAMETER TimeoutSec
    Readiness timeout for -Background mode (default 180).
.PARAMETER ExtraArgs
    Extra arguments passed straight through to the JAR (e.g. --server.port=9090).
#>
# Copyright (c) 2026 Kunal Suri. SPDX-License-Identifier: EPL-2.0
[CmdletBinding()]
param(
    [switch]$Build,
    [switch]$Background,
    [int]$TimeoutSec = 180,
    [Parameter(ValueFromRemainingArguments = $true)]
    [string[]]$ExtraArgs
)

$ErrorActionPreference = 'Stop'
. "$PSScriptRoot\shared_core_lib.ps1"

Write-Section 'Papyrus-Web :: Backend'

# --- Java present and recent enough? ------------------------------------
if (-not (Test-CommandExists 'java')) {
    Write-Err "Java not found. Install Temurin JDK $PwppJavaMajor and ensure 'java' is on PATH."
    exit 1
}
$javaVersionText = (& java -version 2>&1) -join "`n"
if ($javaVersionText -match 'version "(\d+)') {
    $javaMajor = [int]$Matches[1]
    if ($javaMajor -lt $PwppJavaMajor) {
        Write-Err "Java $javaMajor detected, but this project requires Java $PwppJavaMajor or newer."
        exit 1
    }
    Write-Ok "Java $javaMajor detected."
} else {
    Write-Warn 'Could not parse the Java version; continuing anyway.'
}

# --- Optional build ------------------------------------------------------
if ($Build) {
    Write-Step 'Building the backend (mvn -DskipTests clean package)...'
    Push-Location $PwppBackendDir
    try {
        mvn -DskipTests clean package
        if ($LASTEXITCODE -ne 0) { Write-Err 'Maven build failed.'; exit 1 }
    } finally {
        Pop-Location
    }
}

# --- Locate the runnable JAR --------------------------------------------
$jar = Find-BackendJar
if (-not $jar) {
    Write-Err "No runnable JAR found in $PwppBackendTargetDir."
    Write-Info 'Build it first:  .\build-all.ps1      (full app, UI bundled in)'
    Write-Info '            or:  .\start-backend.ps1 -Build   (backend only)'
    exit 1
}
Write-Ok "Using JAR: $($jar.Name)"

# --- Friendly warning if the database is not up -------------------------
if (-not (Test-TcpPort -Port $PwppDbPort)) {
    Write-Warn "Nothing is listening on the database port $PwppDbPort."
    Write-Info 'Start it with  .\start-db.ps1  or the backend will fail to connect.'
}

# --- Launch --------------------------------------------------------------
# In both modes we start java via Start-Process -PassThru so we can record its
# real PID to $PwppBackendPidFile. stop-dev.ps1 reads that file to stop exactly
# this process (with a port-based fallback). See Stop-TrackedService.
if (-not (Test-Path $PwppLogDir)) { New-Item -ItemType Directory -Path $PwppLogDir -Force | Out-Null }
$argList = @('-jar', $jar.FullName)
if ($ExtraArgs) { $argList += $ExtraArgs }

if ($Background) {
    $outLog = Join-Path $PwppLogDir 'backend.out.log'
    $errLog = Join-Path $PwppLogDir 'backend.err.log'

    Write-Step 'Starting backend (background)...'
    $proc = Start-Process -FilePath 'java' -ArgumentList $argList `
        -RedirectStandardOutput $outLog -RedirectStandardError $errLog -PassThru
    Save-ServicePid -PidFile $PwppBackendPidFile -ProcessId $proc.Id
    Write-Info "PID $($proc.Id); logs -> $outLog"

    if (Wait-TcpPort -Port $PwppBackendPort -TimeoutSec $TimeoutSec -Name 'backend') {
        Write-Ok "Backend ready at http://localhost:$PwppBackendPort"
        exit 0
    }
    Write-Err "Backend did not become ready within $TimeoutSec seconds. See $errLog"
    exit 1
}

# Foreground: -NoNewWindow keeps java's logs inline in this console, while
# -PassThru still gives us the PID. We then block on the process.
Write-Step "Starting backend (foreground) at http://localhost:$PwppBackendPort  -  press Ctrl+C to stop"
$proc = Start-Process -FilePath 'java' -ArgumentList $argList -NoNewWindow -PassThru
Save-ServicePid -PidFile $PwppBackendPidFile -ProcessId $proc.Id
try {
    $proc.WaitForExit()
} finally {
    Remove-Item $PwppBackendPidFile -ErrorAction SilentlyContinue
}
exit $proc.ExitCode
