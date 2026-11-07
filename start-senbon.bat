@echo off
rem ==========================================================
rem  千本测试版：双击直接启动（已打包，不用编译）
rem ==========================================================
cd /d "%~dp0"

echo Starting Shattered Pixel Dungeon (senbon test build)...
echo.

java -jar "SPD-Senbon-test.jar"

if errorlevel 1 (
  echo.
  echo [!] Game exited with an error. See messages above.
  pause
)
