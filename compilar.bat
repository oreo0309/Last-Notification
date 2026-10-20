@echo off
chcp 65001 >nul
cd /d "%~dp0"
where javac >nul 2>nul
if errorlevel 1 (
    echo No se encontro javac. Instala un JDK 17 o superior ^(por ejemplo Eclipse Temurin 21^).
    exit /b 1
)
if exist bin rmdir /s /q bin
mkdir bin
javac --release 17 -encoding UTF-8 -d bin -sourcepath src src\juego\Principal.java
if errorlevel 1 goto error
javac --release 17 -encoding UTF-8 -d bin -cp bin -sourcepath "test;src" test\pruebas\EjecutarPruebas.java
if errorlevel 1 goto error
echo Compilacion exitosa.
exit /b 0
:error
echo Hubo errores al compilar.
exit /b 1
