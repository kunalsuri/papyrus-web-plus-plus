<#
.SYNOPSIS
    Stop the dev stack: frontend, backend and database.
.DESCRIPTION
    Stops the Vite dev server (port 5173) and the backend (port 8080) by killing
    whatever is listening there, then stops the database container.

    The backend is stopped by the PID recorded at launch in
    scripts-pwpp/.logs/backend.pid (precise), falling back to the port owner if
    that PID is gone or stale. The frontend (an npm -> node -> vite tree) is
    stopped by its port owner, since no single recorded PID reliably represents
    it.

    SAFETY: by default we only kill processes whose name matches the expected
    runtime (node for the frontend, java for the backend), and a recorded PID is
    trusted only if it is still a java process - so we never terminate an
    unrelated app via a reused PID or a borrowed port. Pass -Force to override.
    The policy lives in Stop-TrackedService / Stop-ListenerOnPort in shared_core_lib.ps1.
.PARAMETER RemoveData
    Also delete the database data volume (docker compose down -v). Without this,
    the container is merely stopped and your models are preserved.
.PARAMETER Force
    Kill port owners even if their process name is unexpected.
#>
# Copyright (c) 2026 Kunal Suri. SPDX-License-Identifier: EPL-2.0
[CmdletBinding()]
param(
    [switch]$RemoveData,
    [switch]$Force
)

$ErrorActionPreference = 'Stop'
. "$PSScriptRoot\shared_core_lib.ps1"

Write-Section 'Papyrus-Web :: Stop dev stack'

# --- Frontend ------------------------------------------------------------
Write-Step "Stopping frontend (port $PwppFrontendPort)..."
Stop-ListenerOnPort -Port $PwppFrontendPort -AllowedProcessNames @('node') -Name 'frontend' -Force:$Force

# --- Backend (recorded PID first, port fallback) -------------------------
Write-Step "Stopping backend (recorded PID, then port $PwppBackendPort)..."
Stop-TrackedService -Port $PwppBackendPort -PidFile $PwppBackendPidFile -AllowedProcessNames @('java') -Name 'backend' -Force:$Force

# --- Database ------------------------------------------------------------
Write-Step 'Stopping database container...'
if (Test-DockerRunning) {
    if ($RemoveData) {
        docker compose -f $PwppDevComposeFile down -v
        Write-Ok 'Database stopped and data volume removed.'
    } else {
        docker compose -f $PwppDevComposeFile stop
        Write-Ok 'Database stopped (data preserved; use -RemoveData to wipe).'
    }
} else {
    Write-Warn 'Docker not running - skipped database stop.'
}

Write-Section 'Dev stack stopped'
exit 0
