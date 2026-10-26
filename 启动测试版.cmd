@echo off
chcp 65001 >nul
cd /d "%~dp0"
title 萝卜地牢 测试版
echo 正在编译并启动桌面版...
call gradlew.bat desktop:debug --console=plain
echo.
echo 启动器已退出（按任意键关闭窗口）
pause >nul
