@echo off
rem ============================================
rem  双击这个文件即可启动桌面版进行测试
rem  首次运行需要编译，可能要等 1-3 分钟
rem ============================================
cd /d "%~dp0"

echo.
echo [1/2] Compiling desktop module...
echo.

call gradlew.bat --offline --configure-on-demand desktop:debug

if errorlevel 1 (
  echo.
  echo [!] Build or launch failed. See the messages above.
  pause
)
