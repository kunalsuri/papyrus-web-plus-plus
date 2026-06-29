<#
.SYNOPSIS
    Start the local PostgreSQL database for papyrus-web-plus-plus.
.DESCRIPTION
    Brings up the postgres:15 container defined in docker-compose.dev.yml, whose
    credentials match backend/papyrus-web/src/main/resources/application.properties
    (port 5439, db papyrus-web-db, user dbuser/dbpwd). Data lives in a named
    volume and survives restarts.
.PARAMETER Reset
    Destroy the existing container AND its data volume before starting fresh.
.PARAMETER TimeoutSec
    How long to wait for the database to accept connections (default 60).
.EXAMPLE
    .\start-db.ps1
.EXAMPLE
    .\start-db.ps1 -Reset
#>
# Copyright (c) 2026 Kunal Suri. SPDX-License-Identifier: EPL-2.0
[CmdletBinding()]
param(
    [switch]$Reset,
    [int]$TimeoutSec = 60
)

$ErrorActionPreference = 'Stop'
. "$PSScriptRoot\shared_core_lib.ps1"

Write-Section 'Papyrus-Web :: Database'

if (-not (Test-DockerRunning)) {
    Write-Err 'Docker is not available or the daemon is not running.'
    Write-Info 'Install Docker Desktop and make sure it is started, then re-run this script.'
    exit 1
}
Write-Ok 'Docker daemon is running.'

if (-not (Test-Path $PwppDevComposeFile)) {
    Write-Err "Compose file not found: $PwppDevComposeFile"
    exit 1
}

if ($Reset) {
    Write-Step 'Removing existing database container and data volume (-Reset)...'
    docker compose -f $PwppDevComposeFile down -v
}

Write-Step 'Starting PostgreSQL container...'
docker compose -f $PwppDevComposeFile up -d
if ($LASTEXITCODE -ne 0) {
    Write-Err 'docker compose failed to start the database.'
    exit 1
}

if (Wait-TcpPort -Port $PwppDbPort -TimeoutSec $TimeoutSec -Name 'PostgreSQL') {
    Write-Ok "Database ready on localhost:$PwppDbPort (db '$PwppDbName', user '$PwppDbUser')."
    Write-Info "Connection string: jdbc:postgresql://localhost:$PwppDbPort/$PwppDbName"
    exit 0
}

Write-Err "Database did not become ready within $TimeoutSec seconds."
Write-Info "Inspect logs with:  docker logs $PwppDbContainer"
exit 1
