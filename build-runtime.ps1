param()
$ErrorActionPreference = "Stop"

Write-Host "================================"
Write-Host "  Building Custom Java Runtime"
Write-Host "================================"
Write-Host ""

$JAR_NAME   = "ganis_town.jar"
$RUNTIME_DIR = "runtime"
$FALLBACK_MODULES = "java.base,java.desktop,java.sql,java.logging,java.management,java.naming,jdk.unsupported,jdk.crypto.ec"

# ── Validate JAR ──────────────────────────────────────────────────────────────
if (-not (Test-Path $JAR_NAME)) {
    Write-Host "[ERROR] $JAR_NAME not found. Run build.bat first."
    exit 1
}

# ── Locate JDK bin dir ────────────────────────────────────────────────────────
# Preferred: derive from the java.exe already on PATH (same bin folder has jlink/jdeps)
$javaCmd = Get-Command java -ErrorAction SilentlyContinue
$javaExe = if ($javaCmd) { $javaCmd.Source } else { $null }
$jdkBin  = $null

if ($javaExe -and (Test-Path $javaExe)) {
    $jdkBin = Split-Path $javaExe
}

# Fallback 1: JAVA_HOME env var
if (-not $jdkBin -and $env:JAVA_HOME -and (Test-Path "$env:JAVA_HOME\bin\jlink.exe")) {
    $jdkBin = "$env:JAVA_HOME\bin"
}

# Fallback 2: scan Eclipse Adoptium and standard Oracle/OpenJDK install paths
if (-not $jdkBin) {
    $searchRoots = @(
        "$env:ProgramFiles\Eclipse Adoptium",
        "$env:ProgramFiles\Java",
        "$env:ProgramFiles\Microsoft",
        "${env:ProgramFiles(x86)}\Java"
    )
    foreach ($root in $searchRoots) {
        if (-not (Test-Path $root)) { continue }
        $found = Get-ChildItem $root -Filter "jlink.exe" -Recurse -ErrorAction SilentlyContinue | Select-Object -First 1
        if ($found) { $jdkBin = $found.DirectoryName; break }
    }
}

if (-not $jdkBin) {
    Write-Host "[ERROR] Could not locate a JDK with jlink. Install a full JDK or set JAVA_HOME."
    exit 1
}

$jdeps = Join-Path $jdkBin "jdeps.exe"
$jlink = Join-Path $jdkBin "jlink.exe"

if (-not (Test-Path $jdeps)) { Write-Host "[ERROR] jdeps.exe not found in: $jdkBin"; exit 1 }
if (-not (Test-Path $jlink)) { Write-Host "[ERROR] jlink.exe not found in: $jdkBin"; exit 1 }

Write-Host "[OK] JDK bin: $jdkBin"

# ── Detect required modules ───────────────────────────────────────────────────
Write-Host "Detecting required modules from $JAR_NAME ..."
$MODULES = (& $jdeps --ignore-missing-deps --print-module-deps $JAR_NAME 2>$null) -join ","

if ([string]::IsNullOrWhiteSpace($MODULES) -or $MODULES -match "^\s*$") {
    Write-Host "[WARN] Auto-detection produced no output. Using fallback modules."
    $MODULES = $FALLBACK_MODULES
}

# Sanitise: remove any 'unnamed' token jdeps sometimes emits for non-modular code
$MODULES = ($MODULES -split "," | Where-Object { $_ -notmatch "unnamed" -and $_ -ne "" }) -join ","
if ([string]::IsNullOrWhiteSpace($MODULES)) { $MODULES = $FALLBACK_MODULES }

Write-Host "[OK] Modules: $MODULES"
Write-Host ""

# ── Remove old runtime ────────────────────────────────────────────────────────
if (Test-Path $RUNTIME_DIR) {
    Write-Host "Removing old $RUNTIME_DIR ..."
    Remove-Item -Recurse -Force $RUNTIME_DIR
}

# ── Build runtime with jlink ──────────────────────────────────────────────────
Write-Host "Building runtime image..."

function Invoke-Jlink([string]$modules) {
    & $jlink `
        --add-modules $modules `
        --strip-debug `
        --no-man-pages `
        --no-header-files `
        --compress=2 `
        --output $RUNTIME_DIR
    return $LASTEXITCODE
}

$exitCode = Invoke-Jlink $MODULES

if ($exitCode -ne 0) {
    Write-Host ""
    Write-Host "[WARN] jlink failed with detected modules. Retrying with fallback set..."
    if (Test-Path $RUNTIME_DIR) { Remove-Item -Recurse -Force $RUNTIME_DIR }
    $exitCode = Invoke-Jlink $FALLBACK_MODULES
}

if ($exitCode -ne 0) {
    Write-Host ""
    Write-Host "[ERROR] Failed to build custom runtime."
    exit 1
}

# ── Done ──────────────────────────────────────────────────────────────────────
$size = (Get-ChildItem $RUNTIME_DIR -Recurse | Measure-Object -Property Length -Sum).Sum
$sizeMB = [math]::Round($size / 1MB, 1)

Write-Host ""
Write-Host "[OK] Custom runtime created in: $RUNTIME_DIR  ($sizeMB MB)"
Write-Host "[OK] Launch4j -> Bundled JRE path: runtime"
Write-Host ""
Write-Host "Test locally:"
Write-Host "  .\$RUNTIME_DIR\bin\java -jar $JAR_NAME"
