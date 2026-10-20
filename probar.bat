@echo off
chcp 65001 >nul
cd /d "%~dp0"
call compilar.bat
if errorlevel 1 exit /b 1
java -Djava.awt.headless=true -Dfile.encoding=UTF-8 -Dsun.stdout.encoding=UTF-8 -cp bin pruebas.EjecutarPruebas
