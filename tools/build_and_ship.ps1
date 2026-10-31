param(
    [string]$Message = "feat(armor): secondary armor effects"
)

$ErrorActionPreference = "Stop"

# 仓库路径写死：脚本放在桌面 ai 文件夹里也能直接双击运行
$repo = "C:\Users\1372722790\Documents\GitHub\Radish_Pixel_Dungeon"
if (-not (Test-Path $repo)) { Write-Host "repo not found: $repo"; exit 1 }
Set-Location $repo

Write-Host "[1/4] compile core..."
& .\gradlew.bat :core:compileJava --console=plain
if ($LASTEXITCODE -ne 0) { Write-Host "COMPILE FAILED"; [System.Media.SystemSounds]::Hand.Play(); exit 1 }

Write-Host "[2/4] assemble debug apk..."
& .\gradlew.bat :android:assembleDebug --console=plain
if ($LASTEXITCODE -ne 0) { Write-Host "ASSEMBLE FAILED"; [System.Media.SystemSounds]::Hand.Play(); exit 1 }

Write-Host "[3/4] deliver to Desktop\ai ..."
$apkDir = Join-Path $env:USERPROFILE "Desktop\ai"
if (-not (Test-Path $apkDir)) { New-Item -ItemType Directory -Path $apkDir | Out-Null }
Get-ChildItem $apkDir -Filter "*.apk" -ErrorAction SilentlyContinue | Remove-Item -Force
$stamp = Get-Date -Format "yyyyMMdd-HHmm"
$dest = Join-Path $apkDir ("萝卜地牢_0.8.4-base2.4.2-INDEV_" + $stamp + ".apk")
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

[System.Media.SystemSounds]::Asterisk.Play()
Write-Host ("DONE: " + $dest)
Write-Host "按任意键关闭窗口..."
$null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")
