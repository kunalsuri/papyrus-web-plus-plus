<#
.SYNOPSIS
    Start the Papyrus-Web frontend dev server (Vite) on port 5173.
.DESCRIPTION
    Runs the React/TypeScript dev server with hot reload. It talks to the
    backend on http://localhost:8080 (cross-origin; the backend already allows
    any origin via CORS), so start the backend separately.

    IMPORTANT codebase fact: the app imports @eclipse-papyrus/papyrus-web-components
    from that package's built ./dist, and turbo's `start` task does NOT build it
    (the components library has no `start` script). So this script builds the
    components library first if its dist/ is missing - otherwise `npm start`
    fails on a fresh checkout.
.PARAMETER Install
    Force a clean dependency install (npm ci) even if node_modules exists.
.PARAMETER Rebuild
    Rebuild the papyrus-web-components library even if its dist/ already exists.
.PARAMETER FixFormat
    Run `npm run format` (prettier --write) first. Useful because turbo gates
    builds behind `format-lint`, which fails hard on unformatted files.
#>
# Copyright (c) 2026 Kunal Suri. SPDX-License-Identifier: EPL-2.0
[CmdletBinding()]
param(
    [switch]$Install,
    [switch]$Rebuild,
    [switch]$FixFormat
)

$ErrorActionPreference = 'Stop'
. "$PSScriptRoot\shared_core_lib.ps1"

Write-Section 'Papyrus-Web :: Frontend'

# --- Toolchain check -----------------------------------------------------
foreach ($tool in 'node', 'npm') {
    if (-not (Test-CommandExists $tool)) {
        Write-Err "$tool not found. Install Node $PwppNodeVersion / npm $PwppNpmVersion (see README)."
        exit 1
    }
}
$nodeVersion = (& node --version).TrimStart('v')
Write-Ok "node $nodeVersion / npm $(& npm --version)"
if ($nodeVersion -ne $PwppNodeVersion) {
    Write-Warn "package.json pins node $PwppNodeVersion; you have $nodeVersion. Usually fine, but mismatches can break native deps."
}

Push-Location $PwppFrontendDir
try {
    # --- Dependencies ----------------------------------------------------
    if ($Install -or -not (Test-Path (Join-Path $PwppFrontendDir 'node_modules'))) {
        Write-Step 'Installing dependencies (npm ci)...'
        npm ci
        if ($LASTEXITCODE -ne 0) {
            Write-Err 'npm ci failed. If it is an auth error, check ~/.npmrc (see setup-dev.ps1).'
            exit 1
        }
    } else {
        Write-Ok 'Dependencies already installed (use -Install to refresh).'
    }

    if ($FixFormat) {
        Write-Step 'Formatting sources (npm run format)...'
        npm run format
    }

    # --- Build the components library if needed --------------------------
    if ($Rebuild -or -not (Test-Path $PwppComponentsDistDir)) {
        Write-Step 'Building @eclipse-papyrus/papyrus-web-components (required before the app can start)...'
        npm run build --workspace @eclipse-papyrus/papyrus-web-components
        if ($LASTEXITCODE -ne 0) {
            Write-Err 'Components build failed.'
            Write-Info 'If it failed on format-lint, re-run with -FixFormat.'
            exit 1
        }
        Write-Ok 'Components library built.'
    } else {
        Write-Ok 'Components library already built (use -Rebuild to refresh).'
    }
} finally {
    Pop-Location
}

# --- Friendly note if the backend is not up ------------------------------
if (-not (Test-TcpPort -Port $PwppBackendPort)) {
    Write-Warn "Backend not detected on port $PwppBackendPort - the UI will load but API calls will fail until it is up."
    Write-Info 'Start it with  .\start-backend.ps1'
}

# --- Run the dev server (foreground) -------------------------------------
$code = 0
Push-Location $PwppFrontendAppDir
try {
    Write-Step "Starting Vite dev server at http://localhost:$PwppFrontendPort  -  press Ctrl+C to stop"
    npm run start
    $code = $LASTEXITCODE
} finally {
    Pop-Location
}
exit $code
