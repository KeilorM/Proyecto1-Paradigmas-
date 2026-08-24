@echo off
title PolyFlow - Polyglot Data Pipeline
cls

echo ===================================================
echo             COMPILANDO SCRIPT POLYFLOW             
echo ===================================================
if exist bin rmdir /s /q bin
mkdir bin

javac -d bin src/polyflow/PolyFlow.java src/polyflow/RuleEvaluator.java
if %errorlevel% neq 0 (
    echo [ERROR] Error de compilacion en Java.
    pause
    exit /b %errorlevel%
)

cls
echo ===================================================
echo        EJECUTANDO POLYFLOW DATA PIPELINE          
echo ===================================================
echo.

:: Ejecuta el orquestador en silencio para controlar la salida visual si se desea, 
:: o simplemente deja que Java pinte el flujo en tiempo real:
java -cp bin polyflow.PolyFlow

if %errorlevel% equ 0 (
    echo.
    echo ---------------------------------------------------
    echo  PIPELINE COMPLETADO CON EXITO
    echo ---------------------------------------------------
    if exist data\reporte.html start "" "data\reporte.html"
) else (
    echo.
    echo [ERROR] El pipeline se interrumpio en alguna etapa.
)

echo.
pause
