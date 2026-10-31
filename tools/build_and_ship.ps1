param(
    [string]$Message = "feat(armor): secondary armor effects"
)

$ErrorActionPreference = "Stop"
Set-Location (Join-Path $PSScriptRoot "..")

Write-Host "[1/4] compile core..."
& .\gradlew.bat :core:compileJava --console=plain
if ($LASTEXITCODE -ne 0) { Write-Host "compile failed"; exit 1 }

Write-Host "[2/4] assemble debug apk..."
& .\gradlew.bat :android:assembleDebug --console=plain
if ($LASTEXITCODE -ne 0) { Write-Host "assemble failed"; exit 1 }

Write-Host "[3/4] deliver to Desktop\ai ..."
$apkDir = Join-Path $env:USERPROFILE "Desktop\ai"
if (-not (Test-Path $apkDir)) { New-Item -ItemType Directory -Path $apkDir | Out-Null }
Get-ChildItem $apkDir -Filter "*.apk" -ErrorAction SilentlyContinue | Remove-Item -Force
$stamp = Get-Date -Format "yyyyMMdd-HHmm"
$dest = Join-Path $apkDir ("radish_0.8.4-base2.4.2-INDEV_" + $stamp + ".apk")
Copy-Item "android\build\outputs\apk\debug\android-debug.apk" $dest -Force
Write-Host ("   -> " + $dest)

Write-Host "[4/4] commit and push..."
git add -A core/src/main/java core/src/main/assets tools
$changed = git status --porcelain
if ($changed) {
    git commit -m $Message | Out-Null
    git -c http.proxy=http://127.0.0.1:10090 push origin feat/peddler-npc
} else {
    Write-Host "   no source change"
}

Write-Host ("done: " + $dest)
