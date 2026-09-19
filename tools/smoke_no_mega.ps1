# No-MEGA startup smoke test.
# Compiles once WITH MegaCells, moves the MegaCells jar out of libs/, launches the
# dev client with -x compileJava (so the no-MEGA classpath is the previous build's
# output plus dependencies), and restores the jar when the client exits.
# processResources must run: it carries the fresh mods.toml, where MegaCells is
# optional. Expected result:
#   1. the game boots without errors (missing-class or registry crashes fail the smoke);
#   2. the creative tab contains NO small bulk storage matrices;
#   3. storage hosts and drives keep working with the regular matrices.
# Close the client window to finish the smoke and restore the jar.
$ErrorActionPreference = 'Stop'
Push-Location (Split-Path $PSCommandPath -Parent | Split-Path -Parent)

$jar = 'libs/megacells-4.11.0.jar'
$bak = 'libs/megacells-4.11.0.jar.smoke-bak'

if (Test-Path $bak) {
    Write-Error "A previous smoke backup exists at $bak - restore it manually first."
    exit 1
}
if (!(Test-Path $jar)) {
    Write-Error "MegaCells jar not found at $jar"
    exit 1
}

# Compile and package with MegaCells first; the smoke only removes it from the
# runtime classpath (-x compileJava reuses these classes).
.\gradlew.bat build -q
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }

# The Gradle daemon keeps a handle on the dependency jar; stop it so the move succeeds.
.\gradlew.bat --stop

Move-Item $jar $bak
try {
    .\gradlew.bat runClient -x compileJava
    Write-Host ''
    Write-Host 'Smoke checklist (verify from the session you just closed):'
    Write-Host '  1. The game booted without missing-class or registry errors.'
    Write-Host '  2. The creative tab contains NO small bulk storage matrices.'
    Write-Host '  3. Storage hosts and drives work with the regular matrices.'
} finally {
    if (Test-Path $bak) {
        Move-Item $bak $jar -Force
        Write-Host 'MegaCells jar restored.'
    }
}
Pop-Location
