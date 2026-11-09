@echo off
cd /d "%~dp0"
java -jar SPD-Sukuna-test.jar
if errorlevel 1 pause