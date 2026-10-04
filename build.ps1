$ErrorActionPreference = 'Stop'
$projectRoot = $PSScriptRoot
$jdkBin = Join-Path $env:USERPROFILE '.jdks/openjdk-25.0.2/bin'
$javacPath = Join-Path $jdkBin 'javac.exe'
if (-not (Test-Path -LiteralPath $javacPath)) {
    $javacPath = (Get-Command javac -ErrorAction Stop).Source
}
$sourceRoot = Join-Path $projectRoot 'Tetris-master/Tetris-master/src'
$resourceRoot = Join-Path $projectRoot 'Tetris-master/Tetris-master/images'
$outputRoot = Join-Path $projectRoot 'out/production/26-2--'
New-Item -ItemType Directory -Force -Path $outputRoot | Out-Null
$sources = @(Get-ChildItem -LiteralPath $sourceRoot -Filter '*.java' | Select-Object -ExpandProperty FullName)
& $javacPath -encoding UTF-8 -d $outputRoot $sources
if ($LASTEXITCODE -ne 0) {
    throw "Java compilation failed (exit code $LASTEXITCODE)."
}
Get-ChildItem -LiteralPath $resourceRoot | Copy-Item -Destination $outputRoot -Recurse -Force
Write-Host "Build complete: $outputRoot"
