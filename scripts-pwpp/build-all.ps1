<#
.SYNOPSIS
    Full production build: frontend -> copy into backend -> backend fat JAR.
.DESCRIPTION
    Reproduces the exact build sequence from README.adoc:
      1. npm ci + npm run build in frontend/  (turbo builds the components
         library first, then the app, into frontend/papyrus-web/dist).
      2. Copy that dist into
         backend/papyrus-web-frontend/src/main/resources/static so the UI is
         bundled into the server.
      3. mvn clean package in backend/ -> the runnable fat JAR.
.PARAMETER WithTests
    Run the Maven test suite (omitted by default for speed; tests need Docker
    and can take a long time).
.PARAMETER FixFormat
    Run `npm run format` before building (turbo's format-lint gate fails hard
    on unformatted files).
.PARAMETER FrontendOnly
    Build + copy the frontend, skip Maven.
.PARAMETER BackendOnly
    Skip the frontend; only run Maven (uses whatever is already in static/).
#>
# Copyright (c) 2026 Kunal Suri. SPDX-License-Identifier: EPL-2.0
[CmdletBinding()]
param(
    [switch]$WithTests,
    [switch]$FixFormat,
    [switch]$FrontendOnly,
    [switch]$BackendOnly
)

$ErrorActionPreference = 'Stop'
. "$PSScriptRoot\shared_core_lib.ps1"

$started = Get-Date

# ===================== Frontend ==========================================
if (-not $BackendOnly) {
    Write-Section 'Build :: Frontend'
    Push-Location $PwppFrontendDir
    try {
        $needsInstall = $true
        $hashFile = Join-Path 'node_modules' '.package-lock.hash'
        if (Test-Path 'node_modules') {
            if (Test-Path $hashFile) {
                $lockFile = Get-Item 'package-lock.json' -ErrorAction SilentlyContinue
                if ($lockFile) {
                    $currentHash = (Get-FileHash $lockFile.FullName -Algorithm SHA256).Hash
                    $cachedHash = Get-Content $hashFile -ErrorAction SilentlyContinue
                    if ($currentHash -eq $cachedHash) {
                        $needsInstall = $false
                    }
                }
            }
        }

        if ($needsInstall) {
            Write-Step 'Installing dependencies (npm ci --verbose)...'
            npm ci --verbose
            if ($LASTEXITCODE -ne 0) { Write-Err 'npm ci failed (check ~/.npmrc auth?).'; exit 1 }
            
            # Cache the hash of the package-lock.json we just installed
            if (Test-Path 'node_modules') {
                $lockFile = Get-Item 'package-lock.json' -ErrorAction SilentlyContinue
                if ($lockFile) {
                    $currentHash = (Get-FileHash $lockFile.FullName -Algorithm SHA256).Hash
                    Set-Content -Path $hashFile -Value $currentHash -Encoding ASCII
                }
            }
        } else {
            Write-Info 'node_modules is up to date with package-lock.json (hash match); skipping package installation.'
        }

        if ($FixFormat) {
            Write-Step 'Formatting sources (npm run format)...'
            npm run format
        }

        Write-Step 'Building frontend (npm run build -- --log-order=stream)...'
        npm run build -- --log-order=stream
        if ($LASTEXITCODE -ne 0) {
            Write-Err 'Frontend build failed.'
            Write-Info 'If it failed on format-lint, re-run with -FixFormat.'
            exit 1
        }
    } finally {
        Pop-Location
    }

    if (-not (Test-Path $PwppFrontendDistDir)) {
        Write-Err "Expected build output not found at $PwppFrontendDistDir"
        exit 1
    }

    Write-Step 'Copying built UI into the backend static resources...'
    if (-not (Test-Path $PwppStaticResourceDir)) {
        New-Item -ItemType Directory -Path $PwppStaticResourceDir -Force | Out-Null
    } else {
        # Remove stale assets so deleted files do not linger in the JAR.
        Get-ChildItem -Path $PwppStaticResourceDir -Force | Remove-Item -Recurse -Force
    }
    Copy-Item -Path (Join-Path $PwppFrontendDistDir '*') -Destination $PwppStaticResourceDir -Recurse -Force
    Write-Ok "UI copied to $PwppStaticResourceDir"
}

# ===================== Backend ===========================================
if (-not $FrontendOnly) {
    Write-Section 'Build :: Backend'
    if (-not (Test-CommandExists 'mvn')) {
        Write-Err "Maven not found. Install Apache Maven 3.9.9 and ensure 'mvn' is on PATH."
        exit 1
    }

    $mvnArgs = @('clean', 'package', '--errors')
    if (-not $WithTests) { $mvnArgs += '-DskipTests' }

    Write-Step "Running: mvn $($mvnArgs -join ' ')"
    Push-Location $PwppBackendDir
    try {
        mvn @mvnArgs
        if ($LASTEXITCODE -ne 0) { Write-Err 'Maven build failed.'; exit 1 }
    } finally {
        Pop-Location
    }

    $jar = Find-BackendJar
    if ($jar) { Write-Ok "Built runnable JAR: $($jar.FullName)" }
    else { Write-Warn 'Maven succeeded but no fat JAR was found - check the build output.' }
}

$elapsed = [int]((Get-Date) - $started).TotalSeconds
Write-Section "Build complete in ${elapsed}s"
Write-Info 'Run it with:  .\start-db.ps1  then  .\start-backend.ps1   (or .\start-dev.ps1)'
exit 0
