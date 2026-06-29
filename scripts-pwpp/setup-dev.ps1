<#
.SYNOPSIS
    One-shot onboarding: verify the toolchain, check GitHub auth, then build.
.DESCRIPTION
    Run this first on a new machine. It:
      Phase 1 - verifies git, Java 21, Maven, Node, npm and Docker.
      Phase 2 - checks that Maven (~/.m2/settings.xml) and npm (~/.npmrc) have
                the GitHub Packages token the build needs; can scaffold them.
      Phase 3 - runs build-all.ps1 (unless -SkipBuild / -CheckOnly).
.PARAMETER CheckOnly
    Only report environment status; do not build or write anything.
.PARAMETER ConfigureAuth
    Generate ~/.m2/settings.xml and ~/.npmrc from the templates, filling in the
    GITHUB_USERNAME and GITHUB_TOKEN (or GITHUB_AUTH_TOKEN) environment
    variables. Existing files are backed up, never overwritten blindly.
.PARAMETER SkipBuild
    Run the checks but skip the initial build.
.PARAMETER WithTests
    Pass -WithTests through to build-all.ps1.
.EXAMPLE
    $env:GITHUB_USERNAME='me'; $env:GITHUB_TOKEN='ghp_xxx'; .\setup-dev.ps1 -ConfigureAuth
#>
# Copyright (c) 2026 Kunal Suri. SPDX-License-Identifier: EPL-2.0
[CmdletBinding()]
param(
    [switch]$CheckOnly,
    [switch]$ConfigureAuth,
    [switch]$SkipBuild,
    [switch]$WithTests
)

$ErrorActionPreference = 'Stop'
. "$PSScriptRoot\shared_core_lib.ps1"

$problems = New-Object System.Collections.Generic.List[string]

# ===================== Phase 1: toolchain ================================
Write-Section 'Phase 1 - Toolchain'

if (Test-CommandExists 'git') { Write-Ok "git: $((& git --version))" }
else { Write-Err 'git not found.'; $problems.Add('git') }

if (Test-CommandExists 'java') {
    $jv = (& java -version 2>&1) -join "`n"
    if ($jv -match 'version "(\d+)') {
        $maj = [int]$Matches[1]
        if ($maj -ge $PwppJavaMajor) { Write-Ok "Java $maj" }
        else { Write-Err "Java $maj found, need $PwppJavaMajor+."; $problems.Add('java') }
    } else { Write-Warn 'Java present but version unparsed.' }
} else { Write-Err "Java not found (need Temurin $PwppJavaMajor)."; $problems.Add('java') }

if (Test-CommandExists 'mvn') { Write-Ok ((& mvn -version 2>&1 | Select-Object -First 1)) }
else { Write-Err 'Maven not found (need 3.9.9).'; $problems.Add('maven') }

if (Test-CommandExists 'node') {
    $nv = (& node --version).TrimStart('v')
    if ($nv -eq $PwppNodeVersion) { Write-Ok "node $nv" }
    else { Write-Warn "node $nv (package.json pins $PwppNodeVersion)." }
} else { Write-Err "Node not found (need $PwppNodeVersion)."; $problems.Add('node') }

if (Test-CommandExists 'npm') {
    $npmv = (& npm --version)
    if ($npmv -eq $PwppNpmVersion) { Write-Ok "npm $npmv" }
    else { Write-Warn "npm $npmv (package.json pins $PwppNpmVersion)." }
} else { Write-Err "npm not found (need $PwppNpmVersion)."; $problems.Add('npm') }

if (Test-DockerRunning) { Write-Ok 'Docker daemon running.' }
else { Write-Warn 'Docker not running - needed for the dev database and some backend tests.' }

# ===================== Phase 2: GitHub auth =============================
Write-Section 'Phase 2 - GitHub Packages auth'

$mavenSettings = Join-Path $HOME '.m2\settings.xml'
$npmrc         = Join-Path $HOME '.npmrc'
$githubUser    = $env:GITHUB_USERNAME
$githubToken   = if ($env:GITHUB_TOKEN) { $env:GITHUB_TOKEN } elseif ($env:GITHUB_AUTH_TOKEN) { $env:GITHUB_AUTH_TOKEN } else { $env:PASSWORD }

$mavenAuthOk = (Test-Path $mavenSettings) -and (Select-String -Path $mavenSettings -Pattern 'github-sirius-components' -Quiet)
$npmAuthOk   = (Test-Path $npmrc) -and (Select-String -Path $npmrc -Pattern 'npm\.pkg\.github\.com' -Quiet)

if ($mavenAuthOk) { Write-Ok "Maven settings look configured: $mavenSettings" }
else { Write-Warn "Maven settings missing GitHub servers: $mavenSettings" }
if ($npmAuthOk) { Write-Ok "npm settings look configured: $npmrc" }
else { Write-Warn "npm settings missing GitHub auth line: $npmrc" }

if ($ConfigureAuth -and -not $CheckOnly) {
    if ($mavenAuthOk -and $npmAuthOk) {
        Write-Ok 'GitHub auth already configured - nothing to change.'
    } else {
        # Take credentials from env vars, or prompt for them when interactive.
        # The prompt is what lets setup.bat collect them on a double-click,
        # with the token hidden.
        if (-not $githubUser -and [Environment]::UserInteractive) {
            $githubUser = Read-Host 'GitHub username'
        }
        if (-not $githubToken -and [Environment]::UserInteractive) {
            $secureToken = Read-Host 'GitHub token (scope read:package; input hidden)' -AsSecureString
            $githubToken = [System.Net.NetworkCredential]::new('', $secureToken).Password
        }

        if (-not $githubUser -or -not $githubToken) {
            Write-Err 'GitHub username and token are required (set $env:GITHUB_USERNAME / $env:GITHUB_TOKEN, or run interactively).'
            $problems.Add('github-auth')
        } else {
            # ----- Maven settings.xml -----
            if (-not $mavenAuthOk) {
                $stamp = Get-Date -Format 'yyyyMMdd-HHmmss'
                $m2Dir = Split-Path -Parent $mavenSettings
                if (-not (Test-Path $m2Dir)) { New-Item -ItemType Directory -Path $m2Dir -Force | Out-Null }
                if (Test-Path $mavenSettings) { Copy-Item $mavenSettings "$mavenSettings.bak-$stamp" -Force; Write-Info "Backed up to settings.xml.bak-$stamp" }
                $tpl = Get-Content (Join-Path $PwppScriptsDir 'templates\settings.xml.template') -Raw
                $tpl = $tpl.Replace('__GITHUB_USERNAME__', $githubUser).Replace('__GITHUB_TOKEN__', $githubToken)
                Set-Content -Path $mavenSettings -Value $tpl -Encoding UTF8
                Write-Ok "Wrote $mavenSettings (token hidden)."
            }
            # ----- npm .npmrc -----
            if (-not $npmAuthOk) {
                $line = "//npm.pkg.github.com/:_authToken=$githubToken"
                if (Test-Path $npmrc) { Add-Content -Path $npmrc -Value $line; Write-Ok "Appended GitHub auth line to $npmrc." }
                else { Set-Content -Path $npmrc -Value $line -Encoding UTF8; Write-Ok "Wrote $npmrc (token hidden)." }
            }
        }
    }
} elseif (-not $mavenAuthOk -or -not $npmAuthOk) {
    Write-Info 'Configure GitHub Packages auth by double-clicking setup.bat, or:'
    Write-Info '  .\setup-dev.ps1 -ConfigureAuth        (prompts for username + token)'
    Write-Info '  or set $env:GITHUB_USERNAME / $env:GITHUB_TOKEN first for non-interactive use.'
}

# ===================== Phase 3: build ===================================
if ($CheckOnly) {
    Write-Section 'Check complete'
    if ($problems.Count) { Write-Err ("Blocking issues: {0}" -f ($problems -join ', ')); exit 1 }
    Write-Ok 'Environment looks ready.'
    exit 0
}

if ($problems.Count) {
    Write-Section 'Setup blocked'
    Write-Err ("Fix these first: {0}" -f ($problems -join ', '))
    exit 1
}

if ($SkipBuild) {
    Write-Section 'Setup complete (build skipped)'
    Write-Info 'Build when ready with  .\build-all.ps1  then run  .\start-dev.ps1'
    exit 0
}

Write-Section 'Phase 3 - Initial build'
$buildArgs = @()
if ($WithTests) { $buildArgs += '-WithTests' }
& (Join-Path $PwppScriptsDir 'build-all.ps1') @buildArgs
if ($LASTEXITCODE -ne 0) { Write-Err 'Initial build failed - see output above.'; exit 1 }

Write-Section 'Setup complete'
Write-Info 'Start the whole stack with:  .\start-dev.ps1'
exit 0
