# Copyright (c) 2026 Kunal Suri.
# SPDX-License-Identifier: EPL-2.0
#
# shared_core_lib.ps1 - Shared helpers, paths and facts for the papyrus-web-plus-plus
# developer scripts. Every script in this folder dot-sources this file:
#
#     . "$PSScriptRoot\shared_core_lib.ps1"
#
# It is the SINGLE SOURCE OF TRUTH for ports, paths and DB credentials, so the
# whole script set stays consistent if upstream moves things around.

#Requires -Version 5.1
Set-StrictMode -Version Latest

# --------------------------------------------------------------------------
# Repository layout (derived from this file's own location, so the scripts
# work no matter what the current working directory is).
# --------------------------------------------------------------------------
$PwppScriptsDir = $PSScriptRoot
$PwppRepoRoot   = Split-Path -Parent $PSScriptRoot

$PwppBackendDir        = Join-Path $PwppRepoRoot 'backend'
$PwppBackendModuleDir  = Join-Path $PwppRepoRoot 'backend\papyrus-web'           # runnable Spring Boot module
$PwppBackendTargetDir  = Join-Path $PwppBackendModuleDir 'target'
$PwppStaticResourceDir = Join-Path $PwppRepoRoot 'backend\papyrus-web-frontend\src\main\resources\static'

$PwppFrontendDir       = Join-Path $PwppRepoRoot 'frontend'
$PwppFrontendAppDir    = Join-Path $PwppFrontendDir 'papyrus-web'
$PwppFrontendDistDir   = Join-Path $PwppFrontendAppDir 'dist'
$PwppComponentsDir     = Join-Path $PwppFrontendDir 'papyrus-web-components'
$PwppComponentsDistDir = Join-Path $PwppComponentsDir 'dist'

$PwppDevComposeFile    = Join-Path $PwppScriptsDir 'docker-compose.dev.yml'
$PwppLogDir            = Join-Path $PwppScriptsDir '.logs'
$PwppBackendPidFile    = Join-Path $PwppLogDir 'backend.pid'   # written by start-backend.ps1

# --------------------------------------------------------------------------
# Service facts. The DB values MUST match
# backend/papyrus-web/src/main/resources/application.properties so the JAR
# runs with no datasource arguments. docker-compose.dev.yml uses the same
# values. Change them in one place only.
# --------------------------------------------------------------------------
$PwppDbPort       = 5439
$PwppDbName       = 'papyrus-web-db'
$PwppDbUser       = 'dbuser'
$PwppDbPassword   = 'dbpwd'
$PwppDbContainer  = 'papyrus-web-postgres'
$PwppBackendPort  = 8080
$PwppFrontendPort = 5173       # Vite default; the app talks to the backend on 8080 via CORS

# Toolchain versions required by this codebase (see README.adoc / package.json).
$PwppJavaMajor    = 21
$PwppNodeVersion  = '22.16.0'
$PwppNpmVersion   = '10.9.2'

# --------------------------------------------------------------------------
# Coloured output helpers (Write-Host -ForegroundColor works on both Windows
# PowerShell 5.1 and PowerShell 7+, and needs no ANSI/$PSStyle support).
# --------------------------------------------------------------------------
function Write-Section { param([string]$Text)
    Write-Host ''
    Write-Host ('=' * 70) -ForegroundColor DarkCyan
    Write-Host "  $Text" -ForegroundColor Cyan
    Write-Host ('=' * 70) -ForegroundColor DarkCyan
}
function Write-Step { param([string]$Text) Write-Host "==> $Text" -ForegroundColor Cyan }
function Write-Ok   { param([string]$Text) Write-Host "  [OK]   $Text" -ForegroundColor Green }
function Write-Warn { param([string]$Text) Write-Host "  [WARN] $Text" -ForegroundColor Yellow }
function Write-Err  { param([string]$Text) Write-Host "  [FAIL] $Text" -ForegroundColor Red }
function Write-Info { param([string]$Text) Write-Host "         $Text" -ForegroundColor Gray }

# --------------------------------------------------------------------------
# Small utilities.
# --------------------------------------------------------------------------
function Test-CommandExists {
    param([Parameter(Mandatory)][string]$Name)
    return [bool](Get-Command $Name -ErrorAction SilentlyContinue)
}

# TCP probe: returns $true if something is accepting connections on the port.
# Note: a parameter named $Host would clash with PowerShell's automatic $Host,
# so we use $TargetHost throughout.
function Test-TcpPort {
    param([string]$TargetHost = 'localhost', [Parameter(Mandatory)][int]$Port, [int]$TimeoutMs = 1000)
    $client = New-Object System.Net.Sockets.TcpClient
    try {
        $async = $client.BeginConnect($TargetHost, $Port, $null, $null)
        if ($async.AsyncWaitHandle.WaitOne($TimeoutMs) -and $client.Connected) {
            $client.EndConnect($async)
            return $true
        }
        return $false
    } catch {
        return $false
    } finally {
        $client.Close()
    }
}

# Poll a port until it is ready or the timeout elapses. Returns $true/$false.
function Wait-TcpPort {
    param(
        [string]$TargetHost = 'localhost',
        [Parameter(Mandatory)][int]$Port,
        [int]$TimeoutSec = 120,
        [string]$Name = 'service'
    )
    $deadline = (Get-Date).AddSeconds($TimeoutSec)
    Write-Host ("         waiting for {0} on {1}:{2} " -f $Name, $TargetHost, $Port) -NoNewline -ForegroundColor Gray
    while ((Get-Date) -lt $deadline) {
        if (Test-TcpPort -TargetHost $TargetHost -Port $Port) {
            Write-Host ' ready' -ForegroundColor Green
            return $true
        }
        Write-Host '.' -NoNewline -ForegroundColor Gray
        Start-Sleep -Seconds 2
    }
    Write-Host ' timed out' -ForegroundColor Red
    return $false
}

# Locate the runnable Spring Boot "fat JAR". The maven-source-plugin also
# produces a -sources.jar, and Spring Boot leaves a *.jar.original behind, so
# we filter those out and keep the largest remaining match (the fat JAR).
function Find-BackendJar {
    if (-not (Test-Path $PwppBackendTargetDir)) { return $null }
    return Get-ChildItem -Path $PwppBackendTargetDir -Filter 'papyrus-web-*.jar' -File -ErrorAction SilentlyContinue |
        Where-Object { $_.Name -notmatch '-(sources|javadoc)\.jar$' } |
        Sort-Object Length -Descending |
        Select-Object -First 1
}

# True if the Docker CLI exists AND the daemon is responding.
function Test-DockerRunning {
    if (-not (Test-CommandExists 'docker')) { return $false }
    docker info *> $null
    return ($LASTEXITCODE -eq 0)
}

# Path to the best available PowerShell host, for launching child windows.
function Get-PreferredShell {
    $pwsh = Get-Command pwsh -ErrorAction SilentlyContinue
    if ($pwsh) { return $pwsh.Source }
    $ps = Get-Command powershell -ErrorAction SilentlyContinue
    if ($ps) { return $ps.Source }
    return 'powershell'
}

# Stop whatever is LISTENing on a port, with a safety net: by default we only
# kill processes whose name is in $AllowedProcessNames, so we never nuke an
# unrelated app that happens to hold the port. Pass -Force to skip the filter.
function Stop-ListenerOnPort {
    param(
        [Parameter(Mandatory)][int]$Port,
        [string[]]$AllowedProcessNames = @('java', 'node'),
        [string]$Name = 'service',
        [switch]$Force
    )
    $owningPids = @()
    try {
        $owningPids = Get-NetTCPConnection -LocalPort $Port -State Listen -ErrorAction Stop |
            Select-Object -ExpandProperty OwningProcess -Unique
    } catch {
        # Fallback for hosts without Get-NetTCPConnection: parse netstat output.
        $owningPids = netstat -ano |
            Select-String -Pattern ("\s127\.0\.0\.1:{0}\s|\s0\.0\.0\.0:{0}\s|\s\[::\]:{0}\s" -f $Port) |
            ForEach-Object { ($_ -split '\s+')[-1] } |
            Where-Object { $_ -match '^\d+$' } |
            Select-Object -Unique
    }

    if (-not $owningPids) {
        Write-Info "Nothing is listening on port $Port ($Name already stopped)."
        return
    }

    foreach ($owningPid in $owningPids) {
        $proc = Get-Process -Id $owningPid -ErrorAction SilentlyContinue
        if (-not $proc) { continue }
        if (-not $Force -and ($proc.ProcessName -notin $AllowedProcessNames)) {
            Write-Warn "Skipped PID $owningPid ($($proc.ProcessName)) on port $Port - not an expected $Name process. Use -Force to override."
            continue
        }
        try {
            Stop-Process -Id $owningPid -Force -ErrorAction Stop
            Write-Ok "Stopped $Name (PID $owningPid, $($proc.ProcessName)) on port $Port."
        } catch {
            Write-Err "Could not stop PID $owningPid on port ${Port}: $($_.Exception.Message)"
        }
    }
}

# Write a process id to a PID file, creating the log directory if needed. Best
# effort: a failure here must never take down a service that already started.
function Save-ServicePid {
    param([Parameter(Mandatory)][string]$PidFile, [Parameter(Mandatory)][int]$ProcessId)
    try {
        $logDir = Split-Path -Parent $PidFile
        if (-not (Test-Path $logDir)) { New-Item -ItemType Directory -Path $logDir -Force | Out-Null }
        Set-Content -Path $PidFile -Value $ProcessId -Encoding ASCII
    } catch {
        Write-Warn "Could not write PID file ${PidFile}: $($_.Exception.Message)"
    }
}

# Stop a service precisely: first try the PID we recorded at launch, then fall
# back to whoever is holding the port. The recorded PID is only trusted if it
# is still alive AND its process name is expected - this defends against PID
# reuse (the OS handing that number to an unrelated app after our process died).
function Stop-TrackedService {
    param(
        [Parameter(Mandatory)][int]$Port,
        [string]$PidFile,
        [string[]]$AllowedProcessNames = @('java', 'node'),
        [string]$Name = 'service',
        [switch]$Force
    )
    $killedByPid = $false

    if ($PidFile -and (Test-Path $PidFile)) {
        $recordedPid = 0
        $raw = (Get-Content $PidFile -ErrorAction SilentlyContinue | Select-Object -First 1)
        if ([int]::TryParse((($raw -as [string]).Trim()), [ref]$recordedPid) -and $recordedPid -gt 0) {
            $proc = Get-Process -Id $recordedPid -ErrorAction SilentlyContinue
            if (-not $proc) {
                Write-Info "Recorded $Name PID $recordedPid is no longer running."
            } elseif (-not $Force -and ($proc.ProcessName -notin $AllowedProcessNames)) {
                Write-Warn "Recorded PID $recordedPid is now '$($proc.ProcessName)', not $Name - ignoring (likely PID reuse). Use -Force to override."
            } else {
                try {
                    Stop-Process -Id $recordedPid -Force -ErrorAction Stop
                    Write-Ok "Stopped $Name by recorded PID $recordedPid ($($proc.ProcessName))."
                    $killedByPid = $true
                } catch {
                    Write-Err "Could not stop recorded PID ${recordedPid}: $($_.Exception.Message)"
                }
            }
        } else {
            Write-Warn "PID file $PidFile did not contain a valid id - ignoring it."
        }
        Remove-Item $PidFile -ErrorAction SilentlyContinue
    }

    # Always reconcile against the port: covers the no-PID-file case, and the
    # case where the recorded process died but left an orphan on the port.
    if (-not $killedByPid -or (Test-TcpPort -Port $Port)) {
        Stop-ListenerOnPort -Port $Port -AllowedProcessNames $AllowedProcessNames -Name $Name -Force:$Force
    }
}
