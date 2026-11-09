@echo off
rem ==========================================================
rem  双击这个文件直接启动游戏（已经打包好，不用编译）
rem  如果闪退，窗口会停住显示错误信息
rem ==========================================================
cd /d "%~dp0"

echo Starting Shattered Pixel Dungeon (breakball test build)...
echo.

java -jar "SPD-Desktop-test.jar"

if errorlevel 1 (
  echo.
  echo [!] Game exited with an error. See messages above.
  pause
)
